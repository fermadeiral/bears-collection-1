package org.ovirt.engine.ui.frontend.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;

@RunWith(MockitoJUnitRunner.class)
public class GWTRPCCommunicationProviderTest {

    @Mock
    GenericApiGWTServiceAsync mockService;
    @Mock
    XsrfTokenServiceAsync mockXsrfService;
    @Mock
    VdcOperationCallback mockOperationCallbackSingle1;
    @Mock
    VdcOperationCallback mockOperationCallbackSingle2;
    @Mock
    VdcOperationCallbackList mockOperationCallbackList1;
    @Mock
    VdcOperationCallbackList mockOperationCallbackList2;
    @Mock
    EventBus mockEventBus;

    XsrfRpcRequestBuilder mockXsrfRpcRequestBuilder;

    @Captor
    ArgumentCaptor<AsyncCallback<VdcReturnValueBase>> actionCallback;
    @Captor
    ArgumentCaptor<AsyncCallback<List<VdcReturnValueBase>>> actionCallbackList;
    @Captor
    ArgumentCaptor<AsyncCallback<QueryReturnValue>> queryCallback;
    @Captor
    ArgumentCaptor<AsyncCallback<ArrayList<QueryReturnValue>>> queryCallbackList;

    /**
     * The provider under test.
     */
    GWTRPCCommunicationProvider testProvider;

    @Before
    public void setUp() throws Exception {
        mockXsrfRpcRequestBuilder = new XsrfRpcRequestBuilder();
        testProvider = new GWTRPCCommunicationProvider(mockService, mockXsrfService, mockXsrfRpcRequestBuilder);
        mockXsrfRpcRequestBuilder.setXsrfToken(new XsrfToken("Something")); //$NON-NLS-1$
    }

    @Test
    public void testTransmitOperationAction_success() {
        ActionParametersBase testParameters = new ActionParametersBase();
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        final List<VdcOperation<ActionType, ActionParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddDisk, testParameters,
                new VdcOperationCallback<VdcOperation<ActionType, ActionParametersBase>, VdcReturnValueBase>() {

            @Override
            public void onSuccess(VdcOperation<ActionType, ActionParametersBase> operation,
                    VdcReturnValueBase result) {
                assertEquals("Test results should match", testResult, result); //$NON-NLS-1$
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<ActionType, ActionParametersBase> operation, Throwable caught) {
                fail("Should not get here"); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters), actionCallback.capture());
        actionCallback.getValue().onSuccess(testResult);
    }

    @Test
    public void testTransmitOperationAction_failure() {
        ActionParametersBase testParameters = new ActionParametersBase();
        final List<VdcOperation<ActionType, ActionParametersBase>> operationList = new ArrayList<>();
        final Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        final VdcOperation<ActionType, ActionParametersBase> testOperation =
                new VdcOperation<>(ActionType.AddDisk, testParameters,
                new VdcOperationCallback<VdcOperation<ActionType, ActionParametersBase>, VdcReturnValueBase>() {

            @Override
            public void onSuccess(VdcOperation<ActionType, ActionParametersBase> operation,
                    VdcReturnValueBase result) {
                fail("Should not get here"); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<ActionType, ActionParametersBase> operation, Throwable exception) {
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
                assertEquals("Exceptions should match", testException, exception); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runAction(eq(ActionType.AddDisk), eq(testParameters), actionCallback.capture());
        actionCallback.getValue().onFailure(testException);
    }

    @Test
    public void testTransmitOperationQuery_success() {
        QueryParametersBase testParameters = new QueryParametersBase();
        final QueryReturnValue testResult = new QueryReturnValue();
        final List<VdcOperation<QueryType, QueryParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<QueryType, QueryParametersBase> testOperation =
                new VdcOperation<>(QueryType.Search, testParameters,
                new VdcOperationCallback<VdcOperation<QueryType, QueryParametersBase>, QueryReturnValue>() {

            @Override
            public void onSuccess(VdcOperation<QueryType, QueryParametersBase> operation,
                    QueryReturnValue result) {
                assertEquals("Test results should match", testResult, result); //$NON-NLS-1$
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<QueryType, QueryParametersBase> operation, Throwable caught) {
                fail("Should not get here"); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runQuery(eq(QueryType.Search), eq(testParameters), queryCallback.capture());
        queryCallback.getValue().onSuccess(testResult);
    }

    @Test
    public void testTransmitOperationQuery_failure() {
        QueryParametersBase testParameters = new QueryParametersBase();
        final Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        final List<VdcOperation<QueryType, QueryParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<QueryType, QueryParametersBase> testOperation =
                new VdcOperation<>(QueryType.Search, testParameters,
                new VdcOperationCallback<VdcOperation<QueryType, QueryParametersBase>, QueryReturnValue>() {

            @Override
            public void onSuccess(VdcOperation<QueryType, QueryParametersBase> operation,
                    QueryReturnValue result) {
                fail("Should not get here"); //$NON-NLS-1$
            }

            @Override
            public void onFailure(VdcOperation<QueryType, QueryParametersBase> operation, Throwable exception) {
                assertEquals("Operations should match", operationList.get(0), operation); //$NON-NLS-1$
                assertEquals("Exceptions should match", testException, exception); //$NON-NLS-1$
            }
        });
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockService).runQuery(eq(QueryType.Search), eq(testParameters), queryCallback.capture());
        queryCallback.getValue().onFailure(testException);
    }

    @Test
    public void testGetOperationResult_Empty() {
        List<VdcOperation<?, ?>> testOperationList = new ArrayList<>();
        List<VdcOperation<?, ?>> allOperationList = new ArrayList<>();
        List<?> allResults = new ArrayList<>();
        List<?> result = testProvider.getOperationResult(testOperationList, allOperationList, allResults);
        assertEquals("Result should have no results", 0, result.size()); //$NON-NLS-1$
    }

    @Test
    public void testGetOperationResult_One() {
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, new ActionParametersBase(), null);
        List<VdcOperation<?, ?>> testOperationList = new ArrayList<>();
        testOperationList.add(testOperation1);
        List<VdcOperation<?, ?>> allOperationList = new ArrayList<>();
        allOperationList.add(testOperation1);
        VdcReturnValueBase testResult1 = new VdcReturnValueBase();
        List<VdcReturnValueBase> allResults = new ArrayList<>();
        allResults.add(testResult1);
        List<?> result = testProvider.getOperationResult(testOperationList, allOperationList, allResults);
        assertEquals("Result should have one results", 1, result.size()); //$NON-NLS-1$
    }

    @Test
    public void testGetOperationResult_One_of_Two() {
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, new ActionParametersBase(), null);
        VdcOperation<ActionType, ActionParametersBase> testOperation2 =
                new VdcOperation<>(ActionType.AddBookmark, new ActionParametersBase(), null);
        List<VdcOperation<?, ?>> testOperationList = new ArrayList<>();
        testOperationList.add(testOperation2);
        List<VdcOperation<?, ?>> allOperationList = new ArrayList<>();
        allOperationList.add(testOperation1);
        allOperationList.add(testOperation2);
        VdcReturnValueBase testResult1 = new VdcReturnValueBase();
        VdcReturnValueBase testResult2 = new VdcReturnValueBase();
        List<VdcReturnValueBase> allResults = new ArrayList<>();
        allResults.add(testResult1);
        allResults.add(testResult2);
        List<?> result = testProvider.getOperationResult(testOperationList, allOperationList, allResults);
        assertEquals("Result should have one results", 1, result.size()); //$NON-NLS-1$
        assertEquals("Result should match", testResult2, result.get(0)); //$NON-NLS-1$
    }

    @Test
    public void testTransmitOperationList_oneAction_success() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        ActionParametersBase testParameters = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runAction(eq(ActionType.ActivateVds), eq(testParameters), actionCallback.capture());
        actionCallback.getValue().onSuccess(testResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testResult);
    }

    @Test
    public void testTransmitOperationList_oneAction_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        ActionParametersBase testParameters = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runAction(eq(ActionType.ActivateVds), eq(testParameters), actionCallback.capture());
        Exception testException = new Exception("Failure"); //$NON-NLS-1$
        actionCallback.getValue().onFailure(testException);
        verify(mockOperationCallbackSingle1).onFailure(testOperation1, testException);
    }

    @Test
    public void testTransmitOperationList_twoItems_success() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        ActionParametersBase testParameters = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<ActionType, ActionParametersBase> testOperation2 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        List<ActionParametersBase> testParameterList = createActionParameterList(testParameters, 2);
        List<VdcReturnValueBase> testResultList = createActionResultList(testResult, 2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(ActionType.ActivateVds),
                (ArrayList<ActionParametersBase>) eq(testParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        actionCallbackList.getValue().onSuccess((ArrayList<VdcReturnValueBase>) testResultList);
        verify(mockOperationCallbackList1).onSuccess(eq(testList), eq(testResultList));
    }

    @Test
    public void testTransmitOperationList_twoItems_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        ActionParametersBase testParameters = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<ActionType, ActionParametersBase> testOperation2 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        List<ActionParametersBase> testParameterList = createActionParameterList(testParameters, 2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(ActionType.ActivateVds),
                (ArrayList<ActionParametersBase>) eq(testParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        Exception testException = new Exception("Failure"); //$NON-NLS-1$
        actionCallbackList.getValue().onFailure(testException);
        verify(mockOperationCallbackList1).onFailure(eq(testList), eq(testException));
    }

    @Test
    public void testTransmitOperationList_threeItems_twoActionTypes_success() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> activateVdsList = new ArrayList<>();
        ActionParametersBase testParameters = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<ActionType, ActionParametersBase> testOperation2 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<ActionType, ActionParametersBase> testOperation3 =
                new VdcOperation<>(ActionType.ActivateStorageDomain, testParameters, mockOperationCallbackSingle2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testList.add(testOperation3);
        activateVdsList.add(testOperation1);
        activateVdsList.add(testOperation2);
        List<ActionParametersBase> activateVdsParameterList = createActionParameterList(testParameters, 2);
        List<VdcReturnValueBase> testResultList = createActionResultList(testResult, 3);
        List<VdcReturnValueBase> activateVdsResultList = createActionResultList(testResult, 2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(ActionType.ActivateVds),
                (ArrayList<ActionParametersBase>) eq(activateVdsParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        verify(mockService).runAction(eq(ActionType.ActivateStorageDomain), eq(testParameters),
                actionCallback.capture());
        actionCallbackList.getValue().onSuccess((ArrayList<VdcReturnValueBase>) testResultList);
        actionCallback.getValue().onSuccess(testResult);
        verify(mockOperationCallbackList1).onSuccess(eq(activateVdsList), eq(activateVdsResultList));
        verify(mockOperationCallbackSingle2).onSuccess(testOperation3, testResultList.get(2));
    }

    @Test
    public void testTransmitOperationList_threeItems_twoActionTypes_one_success_one_failure() {
        final VdcReturnValueBase testResult = new VdcReturnValueBase();
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> activateVdsList = new ArrayList<>();
        ActionParametersBase testParameters = new ActionParametersBase();
        VdcOperation<ActionType, ActionParametersBase> testOperation1 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<ActionType, ActionParametersBase> testOperation2 =
                new VdcOperation<>(ActionType.ActivateVds, testParameters, mockOperationCallbackList1);
        VdcOperation<ActionType, ActionParametersBase> testOperation3 =
                new VdcOperation<>(ActionType.ActivateStorageDomain, testParameters, mockOperationCallbackSingle2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        testList.add(testOperation3);
        activateVdsList.add(testOperation1);
        activateVdsList.add(testOperation2);
        List<ActionParametersBase> activateVdsParameterList = createActionParameterList(testParameters, 2);
        List<VdcReturnValueBase> testResultList = createActionResultList(testResult, 3);
        List<VdcReturnValueBase> activateVdsResultList = createActionResultList(testResult, 2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleActions(eq(ActionType.ActivateVds),
                (ArrayList<ActionParametersBase>) eq(activateVdsParameterList), eq(false), eq(true),
                actionCallbackList.capture());
        verify(mockService).runAction(eq(ActionType.ActivateStorageDomain), eq(testParameters),
                actionCallback.capture());
        actionCallbackList.getValue().onSuccess((ArrayList<VdcReturnValueBase>) testResultList);
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        actionCallback.getValue().onFailure(testException);
        verify(mockOperationCallbackList1).onSuccess(eq(activateVdsList), eq(activateVdsResultList));
        verify(mockOperationCallbackSingle2).onFailure(testOperation3, testException);
    }

    @Test
    public void testTransmitOperationList_oneQuery_success() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        QueryParametersBase testParameters = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runQuery(eq(QueryType.Search), eq(testParameters), queryCallback.capture());
        QueryReturnValue testResult = new QueryReturnValue();
        queryCallback.getValue().onSuccess(testResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testResult);
    }

    @Test
    public void testTransmitOperationList_oneQuery_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        QueryParametersBase testParameters = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testParameters, mockOperationCallbackSingle1);
        testList.add(testOperation1);
        testProvider.transmitOperationList(testList);
        verify(mockService).runQuery(eq(QueryType.Search), eq(testParameters), queryCallback.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        queryCallback.getValue().onFailure(testException);
        verify(mockOperationCallbackSingle1).onFailure(testOperation1, testException);
    }

    @Test
    public void testTransmitOperationList_multipleQuery_different_callback_success() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> operation1List = new ArrayList<>();
        List<VdcOperation<?, ?>> operation2List = new ArrayList<>();
        QueryParametersBase testParameters = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testParameters, mockOperationCallbackList1);
        VdcOperation<QueryType, QueryParametersBase> testOperation2 =
                new VdcOperation<>(QueryType.Search, testParameters, mockOperationCallbackList2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        operation1List.add(testOperation1);
        operation2List.add(testOperation2);
        List<QueryParametersBase> testParameterList = createQueryParameterList(testParameters, 2);
        List<QueryType> testQueryList = createQueryList(QueryType.Search, 2);
        testProvider.transmitOperationList(testList);
        QueryReturnValue returnValue = new QueryReturnValue();
        List<QueryReturnValue> resultList = createQueryResultList(returnValue, 2);
        List<QueryReturnValue> return1List = createQueryResultList(returnValue, 1);
        List<QueryReturnValue> return2List = createQueryResultList(returnValue, 1);
        verify(mockService).runMultipleQueries(eq((ArrayList<QueryType>) testQueryList),
                (ArrayList<QueryParametersBase>) eq(testParameterList), queryCallbackList.capture());
        queryCallbackList.getValue().onSuccess((ArrayList<QueryReturnValue>) resultList);
        verify(mockOperationCallbackList1).onSuccess(eq(operation1List), eq(return1List));
        verify(mockOperationCallbackList2).onSuccess(eq(operation2List), eq(return2List));
    }

    @Test
    public void testTransmitOperationList_multipleQuery_different_callback_failure() {
        List<VdcOperation<?, ?>> testList = new ArrayList<>();
        List<VdcOperation<?, ?>> operation1List = new ArrayList<>();
        List<VdcOperation<?, ?>> operation2List = new ArrayList<>();
        QueryParametersBase testParameters = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testParameters, mockOperationCallbackList1);
        VdcOperation<QueryType, QueryParametersBase> testOperation2 =
                new VdcOperation<>(QueryType.Search, testParameters, mockOperationCallbackList2);
        testList.add(testOperation1);
        testList.add(testOperation2);
        operation1List.add(testOperation1);
        operation2List.add(testOperation2);
        List<QueryParametersBase> testParameterList = createQueryParameterList(testParameters, 2);
        List<QueryType> testQueryList = createQueryList(QueryType.Search, 2);
        testProvider.transmitOperationList(testList);
        verify(mockService).runMultipleQueries(eq((ArrayList<QueryType>) testQueryList),
                (ArrayList<QueryParametersBase>) eq(testParameterList), queryCallbackList.capture());
        Exception testException = new Exception("This is an exception"); //$NON-NLS-1$
        queryCallbackList.getValue().onFailure(testException);
        verify(mockOperationCallbackList1).onFailure(eq(operation1List), eq(testException));
        verify(mockOperationCallbackList2).onFailure(eq(operation2List), eq(testException));
    }

    @Test
    public void testTransmitOperationList_query_and_action_success() {
        QueryParametersBase testQueryParameters = new QueryParametersBase();
        ActionParametersBase testActionParameters = new ActionParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testQueryParameters, mockOperationCallbackSingle1);
        VdcOperation<ActionType, ActionParametersBase> testOperation2 =
                new VdcOperation<>(ActionType.ActivateVds, testActionParameters, mockOperationCallbackSingle2);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        operationList.add(testOperation2);
        testProvider.transmitOperationList(operationList);
        verify(mockService).runQuery(eq(QueryType.Search), eq(testQueryParameters), queryCallback.capture());
        QueryReturnValue testQueryResult = new QueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testQueryResult);
        VdcReturnValueBase testActionResult = new VdcReturnValueBase();
        verify(mockService).runAction(eq(ActionType.ActivateVds), eq(testActionParameters),
                actionCallback.capture());
        actionCallback.getValue().onSuccess(testActionResult);
        verify(mockOperationCallbackSingle2).onSuccess(testOperation2, testActionResult);
    }

    @Test
    public void testTransmitPublicOperationList_success() {
        QueryParametersBase testQueryParameters = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testQueryParameters, true, false, mockOperationCallbackSingle1);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        testProvider.transmitOperationList(operationList);
        verify(mockService).runPublicQuery(eq(QueryType.Search), eq(testQueryParameters), queryCallback.capture());
        QueryReturnValue testQueryResult = new QueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testQueryResult);
    }

    @Test
    public void testTransmitPublicOperationList_two_public_success() {
        QueryParametersBase testQueryParameters = new QueryParametersBase();
        VdcOperation<QueryType, QueryParametersBase> testOperation1 =
                new VdcOperation<>(QueryType.Search, testQueryParameters, true, false, mockOperationCallbackSingle1);
        VdcOperation<QueryType, QueryParametersBase> testOperation2 =
                new VdcOperation<>(QueryType.GetConfigurationValues, testQueryParameters, true, false, mockOperationCallbackSingle2);
        List<VdcOperation<?, ?>> operationList = new ArrayList<>();
        operationList.add(testOperation1);
        operationList.add(testOperation2);
        testProvider.transmitOperationList(operationList);
        verify(mockService).runPublicQuery(eq(QueryType.Search), eq(testQueryParameters), queryCallback.capture());
        QueryReturnValue testQueryResult = new QueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle1).onSuccess(testOperation1, testQueryResult);

        verify(mockService).runPublicQuery(eq(QueryType.GetConfigurationValues),
                eq(testQueryParameters),
                queryCallback.capture());
        testQueryResult = new QueryReturnValue();
        queryCallback.getValue().onSuccess(testQueryResult);
        verify(mockOperationCallbackSingle2).onSuccess(testOperation2, testQueryResult);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMissingXsrfToken() {
        //Remove token so there should be a request for it.
        mockXsrfRpcRequestBuilder.setXsrfToken(null);
        QueryParametersBase testParameters = new QueryParametersBase();
        final List<VdcOperation<QueryType, QueryParametersBase>> operationList = new ArrayList<>();
        final VdcOperation<QueryType, QueryParametersBase> testOperation =
                new VdcOperation<>(QueryType.Search, testParameters, null);
        operationList.add(testOperation);
        testProvider.transmitOperation(testOperation);
        verify(mockXsrfService).getNewXsrfToken((AsyncCallback<XsrfToken>) any());
    }

    // ********************************************************************************************************
    // * Helper functions
    // ********************************************************************************************************
    private List<QueryType> createQueryList(final QueryType queryType, final int count) {
        List<QueryType> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(queryType);
        }
        return result;
    }

    private List<QueryParametersBase> createQueryParameterList(final QueryParametersBase parameters,
            final int count) {
        ArrayList<QueryParametersBase> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(parameters);
        }
        return result;
    }

    private List<QueryReturnValue> createQueryResultList(final QueryReturnValue resultValue, int count) {
        List<QueryReturnValue> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(resultValue);
        }
        return result;
    }

    private List<ActionParametersBase> createActionParameterList(final ActionParametersBase parameters,
            final int count) {
        List<ActionParametersBase> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(parameters);
        }
        return result;
    }

    private List<VdcReturnValueBase> createActionResultList(final VdcReturnValueBase resultValue, final int count) {
        List<VdcReturnValueBase> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(resultValue);
        }
        return result;
    }
}
