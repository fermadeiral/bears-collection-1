package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DrMappingHelper is a helper class that encapsulates the bll mapping logic that needs to be done related to Disaster
 * Recovery scenario.
 */
@Singleton
public class DrMappingHelper {
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private DbUserDao dbUserDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private RoleDao roleDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;

    protected static final Logger log = LoggerFactory.getLogger(DrMappingHelper.class);

    private static <T, Q extends Nameable> Supplier<Q> getEntityByVal(Function<T, Q> fn, T val) {
        return () -> fn.apply(val);
    }

    /**
     * The function is mainly used for DR purposes, the functionality can be described by the following steps:
     * <ul>
     * <li>1. Check if a mapped value exists for the key <code>String</code> value</li>
     * <li>2. If it does, fetch an entity by the alternative name</li>
     * <li>3. If it doesn't, fetch an entity by the original name</li>
     * <li>4. If the alternative BE exists return it, if it doesn't and the original BE exists return the original, if
     * non exists return null</li>
     * </ul>
     *
     * @param entityMap
     *            - The mapping of the BE, for example Cluster to Cluster or AffinityGroups.
     * @param originalEntityName
     *            - The entity name which is about to be added to the return list map so the VM can be registered with
     *            it.
     * @param getterFunction
     *            - The getter function to be used to fetch the entity by its name.
     * @param <R>
     *            - This is the BE which is about to be added to the registered entity
     * @return - A list containing the entities to apply to the map. Null if none exists.
     */
    private <R extends String, S extends Nameable> S getRelatedEntity(Map<R, R> entityMap,
            R originalEntityName,
            Function<R, S> getterFunction) {
        // Try to fetch the entity from the DAO (usually by name).
        // The entity which is being used is usually indicated in the entity's OVF.
        Supplier<S> sup = getEntityByVal(getterFunction, originalEntityName);
        S original = sup.get();

        // Check if a map was sent by the user for DR purposes to cast the original BE with the alternative BE.
        if (entityMap != null) {
            R destName = entityMap.get(originalEntityName);
            // If an alternative entity appears in the DR mapping sent by the user, try to fetch the alternative entity
            // from the DAO to check if it exists.
            if (destName != null) {
                // Try to fetch the entity from the DAO (usually by name).
                // The entity which is being used is the mapped entity.
                Supplier<S> supplier = getEntityByVal(getterFunction, destName);
                S dest = supplier.get();

                // If the alternative entity exists add it, if not, try to add the original entity (if exists), if both
                // are null, do not add anything.
                return addBusinessEntityToList(dest, original);
            } else if (original != null) {
                // If the mapping destination was not found in the DB, try to add the original entity
                return addBusinessEntityToList(original, null);
            }
        } else if (original != null) {
            // If there is no mapping, only add the original entity
            return addBusinessEntityToList(original, null);
        }
        return null;
    }

    /**
     * If the original BE exists, add it to the list. If the original BE is null and the alternative BE exists add it to
     * the list. If both are null, don't add anything.
     *
     * @param primaryEntity
     *            - The BE which should be added to the list
     * @param alternativeEntity
     *            - The BE which should be added to the list if originalVal is null
     * @param <S>
     *            - The BE to be added
     */
    private static <S extends Nameable> S addBusinessEntityToList(S primaryEntity,
            S alternativeEntity) {
        if (primaryEntity != null) {
            return primaryEntity;
        } else if (alternativeEntity != null) {
            return alternativeEntity;
        } else {
            log.warn("Nor primary entity of alternative entity were found. Not adding anything to the return list");
            return null;
        }
    }

    public Cluster getMappedCluster(String clusterName, Guid vmId, Map<String, String> clusterMap) {
        log.info("Mapping cluster '{}' for vm '{}'.",
                clusterName,
                vmId);
        return getRelatedEntity(clusterMap,
                clusterName,
                val -> clusterDao.getByName((String) val));
    }

    public void mapExternalLunDisks(List<LunDisk> luns, Map<String, Object> externalLunMap) {
        luns.forEach(lunDisk -> {
            if (externalLunMap != null) {
                LunDisk targetLunDisk = (LunDisk) externalLunMap.get(lunDisk.getId().toString());
                if (targetLunDisk != null) {
                    lunDisk.setLun(targetLunDisk.getLun());
                    lunDisk.getLun()
                            .getLunConnections()
                            .forEach(conn -> conn.setStorageType(lunDisk.getLun().getLunType()));
                }
            }
        });
    }

    private List<AffinityGroup> mapAffinityGroups(Map<String, String> affinityGroupMap,
            List<AffinityGroup> affinityGroupsFromParam,
            Guid vmId) {
        if (affinityGroupsFromParam == null) {
            return Collections.emptyList();
        }
        List<AffinityGroup> affinityGroups = new ArrayList<>();
        affinityGroupsFromParam.forEach(affinityGroup -> {
            log.info("Mapping affinity group '{}/{} for vm '{}'.", affinityGroup.getId(), affinityGroup.getName(), vmId);
            AffinityGroup affGroup =
                    getRelatedEntity(affinityGroupMap,
                            affinityGroup.getName(),
                            val -> affinityGroupDao.getByName((String) val));
            if (affGroup != null) {
                affinityGroups.add(affGroup);
            }
        });
        return affinityGroups;
    }

    public void addVmToAffinityGroups(Guid clusterId,
            Guid vmId,
            Map<String, String> affinityGroupMap,
            List<AffinityGroup> affinityGroupsFromParam) {
        mapAffinityGroups(affinityGroupMap, affinityGroupsFromParam, vmId).forEach(affinityGroup -> {
            affinityGroup.setClusterId(clusterId);
            Set<Guid> vmIds = new HashSet<>(affinityGroup.getVmIds());
            vmIds.add(vmId);
            affinityGroup.setVmIds(new ArrayList<>(vmIds));
            affinityGroupDao.update(affinityGroup);
        });
    }

    private List<Label> mapAffinityLabels(Map<String, String> affinityLabelMap,
            Guid vmId,
            List<String> affinityLabelsFromParam) {
        if (affinityLabelsFromParam == null) {
            return Collections.emptyList();
        }
        List<Label> affinityLabels = new ArrayList<>();
        affinityLabelsFromParam.forEach(affinityLabel -> {
            log.info("Mapping affinity label '{}' for vm '{}'.",
                    affinityLabel,
                    vmId);
            Label affLabel = getRelatedEntity(affinityLabelMap,
                    affinityLabel,
                    val -> labelDao.getByName((String) val));
            if (affLabel != null) {
                affinityLabels.add(affLabel);
            }
        });
        return affinityLabels;
    }

    public void addVmToAffinityLabels(Map<String, String> affinityLabelMap, VM vm, List<String> affinityLabelsFromParam) {
        mapAffinityLabels(affinityLabelMap, vm.getId(), affinityLabelsFromParam).forEach(affinityLabel -> {
            affinityLabel.addVm(vm);
            labelDao.update(affinityLabel);
        });
    }

    public void mapDbUsers(Map<String, String> userDomainsMap,
            Set<DbUser> dbUsersFromParams,
            Map<String, Set<String>> userToRolesFromParams,
            Guid entityId,
            VdcObjectType objectType,
            Map<String, Object> roleMap) {
        if (dbUsersFromParams == null || userToRolesFromParams == null) {
            return;
        }
        dbUsersFromParams.forEach(dbUser -> {
            DbUser originalDbUser = dbUserDao.getByUsernameAndDomain(dbUser.getLoginName(), dbUser.getDomain());

            if (userDomainsMap != null) {
                String destDomain = userDomainsMap.get(dbUser.getDomain());

                if (destDomain != null) {
                    DbUser destDbUser = dbUserDao.getByUsernameAndDomain(dbUser.getLoginName(), destDomain);
                    if (destDbUser != null) {
                        addPermissionsForUser(destDbUser, userToRolesFromParams, entityId, objectType, roleMap);
                    }
                } else if (originalDbUser != null) {
                    addPermissionsForUser(originalDbUser, userToRolesFromParams, entityId, objectType, roleMap);
                }
            } else if (originalDbUser != null) {
                addPermissionsForUser(originalDbUser, userToRolesFromParams, entityId, objectType, roleMap);
            }
        });
    }

    private void addPermissionsForUser(DbUser dbUser,
            Map<String, Set<String>> userToRoles,
            Guid entityId,
            VdcObjectType objectType,
            Map<String, Object> roleMap) {
        addPermissions(dbUser,
                userToRoles.getOrDefault(dbUser.getLoginName(), Collections.<String> emptySet()),
                entityId,
                objectType,
                roleMap);
    }

    private void addPermissions(DbUser dbUser,
            Set<String> roles,
            Guid entityId,
            VdcObjectType objectType,
            Map<String, Object> roleMap) {
        roles.forEach(roleName -> {
            Permission permission = null;
            Role originalRole = roleDao.getByName(roleName);
            if (roleMap != null) {
                Role destRoleName = (Role) roleMap.get(roleName);

                if (destRoleName != null) {
                    Role destRole = roleDao.getByName(destRoleName.getName());
                    permission = new Permission(dbUser.getId(), destRole.getId(), entityId, objectType);
                } else if (originalRole != null) {
                    permission = new Permission(dbUser.getId(), originalRole.getId(), entityId, objectType);
                }
            } else if (originalRole != null) {
                permission = new Permission(dbUser.getId(), originalRole.getId(), entityId, objectType);
            }

            if (permission != null) {
                permissionDao.save(permission);
            }
        });
    }
}
