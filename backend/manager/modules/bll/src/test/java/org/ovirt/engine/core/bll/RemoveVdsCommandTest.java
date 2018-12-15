package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class RemoveVdsCommandTest extends BaseCommandTest {
    @Mock
    private VdsDynamicDao vdsDynamicDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VmStaticDao vmStaticDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private Cluster cluster;

    @Mock
    private GlusterBrickDao glusterBrickDao;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private ClusterUtils clusterUtils;

    @Mock
    private GlusterUtil glusterUtils;

    @Mock
    private GlusterVolumeDao volumeDao;

    @Mock
    private GlusterHooksDao hooksDao;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private VdsStaticDao vdsStaticDao;

    @Mock
    private VdsStatisticsDao vdsStatisticsDao;

    /**
     * The command under test.
     */
    @Spy
    @InjectMocks
    private RemoveVdsCommand<RemoveVdsParameters> command =
            new RemoveVdsCommand<>(new RemoveVdsParameters(Guid.newGuid(), false), null);

    private Guid clusterId;

    @Before
    public void setUp() {
        clusterId = Guid.newGuid();
        doReturn(cluster).when(clusterDao).get(any(Guid.class));
        doReturn(clusterUtils).when(command).getClusterUtils();
        when(glusterUtils.getUpServer(clusterId)).thenReturn(getVds(VDSStatus.Up));
    }

    private void mockHasMultipleClusters(Boolean isMultiple) {
        when(command.getClusterId()).thenReturn(clusterId);
        when(clusterUtils.hasMultipleServers(clusterId)).thenReturn(isMultiple);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(clusterId);
        vds.setStatus(status);
        return vds;
    }


    @Test
    public void validateSucceeds() throws Exception {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(false);
        mockHasVolumeOnServer(false);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailsWhenGlusterHostHasVolumes() throws Exception {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
    }

    @Test
    public void validateFailsWhenGlusterMultipleHostHasVolumesWithForce() throws Exception {
        command.getParameters().setForceAction(true);
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockHasMultipleClusters(true);
        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VDS_CANNOT_REMOVE_HOST_HAVING_GLUSTER_VOLUME);
    }

    @Test
    public void validateSucceedsWithForceOption() throws Exception {
        command.getParameters().setForceAction(true);
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockVmsPinnedToHost(Collections.emptyList());

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateFailsWhenVMsPinnedToHost() throws Exception {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();

        mockIsGlusterEnabled(true);
        mockHasVolumeOnServer(true);

        String vmName = "abc";
        mockVmsPinnedToHost(Collections.singletonList(vmName));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DETECTED_PINNED_VMS);

        boolean foundMessage = false;
        for (String message : command.getReturnValue().getValidationMessages()) {
            foundMessage |= message.contains(vmName);
        }

        assertTrue("Can't find VM name in can do action messages", foundMessage);
    }

    @Test
    public void removeWhenMultipleHosts() {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(true);
        command.executeCommand();
        assertEquals(AuditLogType.USER_REMOVE_VDS, command.getAuditLogTypeValue());
        verify(vdsDynamicDao, times(1)).remove(any(Guid.class));
        verify(vdsStatisticsDao, times(1)).remove(any(Guid.class));
        verify(volumeDao, never()).removeByClusterId(any(Guid.class));
        verify(hooksDao, never()).removeAllInCluster(any(Guid.class));
    }

    @Test
    public void removeLastHost() {
        mockVdsWithStatus(VDSStatus.Maintenance);
        mockVdsDynamic();
        mockIsGlusterEnabled(true);
        mockHasMultipleClusters(false);
        command.executeCommand();
        assertEquals(AuditLogType.USER_REMOVE_VDS, command.getAuditLogTypeValue());
        verify(vdsDynamicDao, times(1)).remove(any(Guid.class));
        verify(vdsStatisticsDao, times(1)).remove(any(Guid.class));
        verify(volumeDao, times(1)).removeByClusterId(any(Guid.class));
        verify(hooksDao, times(1)).removeAllInCluster(any(Guid.class));
    }

    /**
     * Mocks that a valid {@link VdsDynamic} gets returned.
     */
    private void mockVdsDynamic() {
        when(vdsDynamicDao.get(command.getParameters().getVdsId())).thenReturn(new VdsDynamic());
    }

    /**
     * Mocks that the given VMs are pinned to the host (List can be empty, but by the API contract can't be
     * <code>null</code>).
     *
     * @param emptyList
     *            The list of VM names.
     */
    private void mockVmsPinnedToHost(List<String> emptyList) {
        when(vmStaticDao.getAllNamesPinnedToHost(command.getParameters().getVdsId())).thenReturn(emptyList);
    }

    /**
     * Mocks that a {@link VDS} with the given status is returned.
     *
     * @param status
     *            The status of the VDS.
     */
    private void mockVdsWithStatus(VDSStatus status) {
        VDS vds = new VDS();
        vds.setStatus(status);
        when(vdsDao.get(command.getParameters().getVdsId())).thenReturn(vds);
    }

    /**
     * Mock that {@link org.ovirt.engine.core.common.businessentities.Cluster} with the given glusterservice status is returned
     *
     * @param glusterService
     *              The Cluster with the given glusterservice status
     */
    private void mockIsGlusterEnabled(boolean glusterService) {
        when(cluster.supportsGlusterService()).thenReturn(glusterService);
    }

    /**
     * Mock that whether the VDS configured with gluster volume. This will return the given volume count
     */
    private void mockHasVolumeOnServer(boolean isBricksRequired) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        if (isBricksRequired) {
            GlusterBrickEntity brick = new GlusterBrickEntity();
            brick.setVolumeId(Guid.newGuid());
            brick.setServerId(command.getVdsId());
            bricks.add(brick);
        }
        when(glusterBrickDao.getGlusterVolumeBricksByServerId(command.getVdsId())).thenReturn(bricks);
    }
}
