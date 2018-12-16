package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * A test case for {@link GetVmTemplatesWithPermittedActionQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetVmTemplatesWithPermittedActionQueryTest
        extends AbstractGetEntitiesWithPermittedActionParametersQueryTest
        <GetEntitiesWithPermittedActionParameters, GetVmTemplatesWithPermittedActionQuery<GetEntitiesWithPermittedActionParameters>> {

    @Mock
    private VmTemplateDao vmTemplateDaoMock;

    @Test
    public void testQueryExecution() {
        // Set up the expected data
        VmTemplate expected = new VmTemplate();

        // Mock the Dao
        when(vmTemplateDaoMock.getTemplatesWithPermittedAction(getUser().getId(), getActionGroup())).thenReturn(Collections.singletonList(expected));

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<VmTemplate> actual = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of templates", 1, actual.size());
        assertEquals("Wrong templates", expected, actual.get(0));
    }
}
