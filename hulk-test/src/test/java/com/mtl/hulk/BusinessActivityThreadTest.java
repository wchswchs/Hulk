package com.mtl.hulk;

import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.model.*;
import com.mtl.hulk.logger.BusinessActivityLoggerExceptionThread;
import com.mtl.hulk.logger.BusinessActivityLoggerThread;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BusinessActivityThreadTest extends AbstractHulkTest {

    @Test
    public void testBusinessActivityLoggerExceptionThread() {
        BusinessActivityId businessActivityId = new BusinessActivityId();
        BusinessActivityException bae = new BusinessActivityException();
        businessActivityId.setBusinessDomain("ceshi1");
        businessActivityId.setBusinessActivity("ceshi1");
        businessActivityId.setEntityId("ceshi1");
        bae.setId(businessActivityId);
        bae.setException("testException");
        Thread loggerThread = new Thread(new BusinessActivityLoggerExceptionThread(properties, hulkDataSource));
        loggerThread.run();
    }

    @Test
    public void testBusinessActivityLoggerThread() {
        AtomicAction atomicAction = new AtomicAction();
        atomicAction.setId("11");
        atomicAction.setCallType(AtomicActionCallType.RequestResponse);
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setName("ceshi");
        serviceOperation.setType(ServiceOperationType.Compensated);
        atomicAction.setServiceOperation(serviceOperation);
        List<AtomicAction> atomicActions = new ArrayList<>();
        BusinessActivityId businessActivityPid = new BusinessActivityId();
        businessActivityPid.setBusinessDomain("ceshi");
        businessActivityPid.setBusinessActivity("ceshi");
//        businessActivityPid.setEntityId("ceshi");

        BusinessActivity activity = new BusinessActivity();
        activity.setStartTime(new Date());
        BusinessActivityId businessActivityId = new BusinessActivityId();
        businessActivityId.setBusinessDomain("ceshi1");
        businessActivityId.setBusinessActivity("ceshi1");
        businessActivityId.setEntityId("ceshi1");
        activity.setId(businessActivityId);
        activity.setPid(businessActivityPid);
        activity.setAtomicCommitActions(atomicActions);
        activity.setAtomicRollbackActions(atomicActions);
        activity.setStatus(BusinessActivityStatus.COMMITTING);

        RuntimeContext context = new RuntimeContext();
        context.setActivity(activity);

        Thread loggerThread = new Thread(new BusinessActivityLoggerThread(properties, hulkDataSource));
        loggerThread.run();
    }

}
