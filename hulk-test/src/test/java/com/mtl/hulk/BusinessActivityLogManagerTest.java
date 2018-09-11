package com.mtl.hulk;

import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.model.*;
import com.mtl.hulk.context.RuntimeContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@SpringBootTest
public class BusinessActivityLogManagerTest extends AbstractHulkTest {

    @Test
    public void testBusinessActivityLogCreate() throws SQLException {
        AtomicAction atomicAction = new AtomicAction();
        atomicAction.setId("2018090301");
        atomicAction.setCallType(AtomicActionCallType.RequestResponse);

        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setName("Sjtu_zdy");
        serviceOperation.setType(ServiceOperationType.Compensated);
        atomicAction.setServiceOperation(serviceOperation);

        AtomicAction atomicAction1 = new AtomicAction();
        atomicAction1.setId("2018090302");
        atomicAction1.setCallType(AtomicActionCallType.Async);
        ServiceOperation serviceOperation1 = new ServiceOperation();
        serviceOperation1.setName("Hust_zdy");
        serviceOperation1.setType(ServiceOperationType.TCC);
        atomicAction1.setServiceOperation(serviceOperation1);

        List<AtomicAction> atomicActions = new ArrayList<>();
        atomicActions.add(atomicAction);
        atomicActions.add(atomicAction1);

        BusinessActivityId businessActivityPid = new BusinessActivityId();
        businessActivityPid.setBusinessDomain("Wh_zdy");
        businessActivityPid.setBusinessActivity("Wh_zdy");
        businessActivityPid.setEntityId("Wh_zdy");

        BusinessActivity activity = new BusinessActivity();
        activity.setStartTime(new Date());

        BusinessActivityId businessActivityId = new BusinessActivityId();
        businessActivityId.setBusinessDomain("zdy");
        businessActivityId.setBusinessActivity("zdy");
        businessActivityId.setEntityId("zdy");
        businessActivityId.setSequence("20180905172218");

        activity.setId(businessActivityId);

        activity.setAtomicCommitActions(atomicActions);
        activity.setAtomicRollbackActions(atomicActions);
        activity.setStatus(BusinessActivityStatus.COMMITING_FAILED);

        RuntimeContext context = new RuntimeContext();
        context.setActivity(activity);
        BusinessActivityLogger logger = BusinessActivityLoggerFactory.getStorage(hulkDataSource, properties);
        BusinessActivityContext businessActivityContext = new BusinessActivityContext();
        Map<String, Object[]> params = new ConcurrentHashMap<String, Object[]>();
        Object[] objects1 = {"wust_zdy", "hust_zdy"};
        Object[] objects2 = {"wust_zdy"};
        params.put("123", objects1);
        params.put("124", objects2);
        businessActivityContext.setParams(params);
        logger.write(context, businessActivityContext);
    }

    @Test
    public void testBusinessActivityExceptionLogCreate() throws SQLException {
        BusinessActivityLogger logger = BusinessActivityLoggerFactory.getStorage(hulkDataSource, properties);
        BusinessActivityException ex = new BusinessActivityException();
        BusinessActivityId businessActivityId = new BusinessActivityId();
        businessActivityId.setBusinessDomain("ceshi1");
        businessActivityId.setBusinessActivity("ceshi1");
        businessActivityId.setEntityId("ceshi1");
        ex.setId(businessActivityId);
        ex.setException("testException");
        logger.writeEx(ex);
    }

    @Test
    public void testBusinessActivityLogRemove() throws SQLException {
        BusinessActivityLogger logger = BusinessActivityLoggerFactory.getStorage(hulkDataSource, properties);
        List<String> BusinessActivityIdStr = new ArrayList<>();
        BusinessActivityId businessActivityId = new BusinessActivityId();
        businessActivityId.setBusinessDomain("ceshi2");
        businessActivityId.setBusinessActivity("ceshi2");
        businessActivityId.setEntityId("ceshi2");
        BusinessActivityIdStr.add(businessActivityId.formatString());
        logger.remove(BusinessActivityIdStr);
    }

    @Test
    public void testFindBusinessActivityLog() throws SQLException {
        BusinessActivityLogger logger = BusinessActivityLoggerFactory.getStorage(hulkDataSource, properties);
        List<HulkTransactionActivity> hulkTransactionActivityList = logger.read(10);
        System.out.println(hulkTransactionActivityList);
    }

    @Test
    public void getTranactionBusinessActivityList() {
        BusinessActivityLogger logger = BusinessActivityLoggerFactory.getStorage(hulkDataSource, properties);
        BusinessActivityId businessActivityId = new BusinessActivityId();
        businessActivityId.setSequence("1013652504641536");
        businessActivityId.setBusinessDomain("mtl");
        businessActivityId.setBusinessActivity("test");
        businessActivityId.setEntityId("a");
        HulkTransactionActivity hulkTransactionActivity = logger.getTranactionBusinessActivity(businessActivityId);
        System.out.println(hulkTransactionActivity);
    }

    @Test
    public void updateBusinessActivityStateTest() throws SQLException {
        String businessActivityId = "1013652504641536_mtl_test_a";
        BusinessActivity activity = new BusinessActivity();
        BusinessActivityLogger logger = BusinessActivityLoggerFactory.getStorage(hulkDataSource, properties);
        int count = logger.updateBusinessActivityState(businessActivityId, activity.getStatus());
        Assert.assertTrue(count == 2);
    }

}
