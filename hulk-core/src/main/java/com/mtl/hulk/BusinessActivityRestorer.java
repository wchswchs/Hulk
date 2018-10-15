package com.mtl.hulk;

import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.*;
import com.mtl.hulk.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mtl.hulk.BusinessActivityLogger.getBusinessActivityIdStr;

@Component
public class BusinessActivityRestorer {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityRestorer.class);

    @Autowired
    private BusinessActivityManagerImpl bam;
    @Autowired
    private HulkProperties properties;
    private final Map<String, AtomicInteger> map = new HashMap<String, AtomicInteger>();

    /**
     * 事务补偿
     */
    public void run() {
        BusinessActivityLogger businessActivityLogger = BusinessActivityLoggerFactory.getStorage(properties);
        int retryTranactionCount = properties.getRetryTranactionCount();
        List<String> businessActivityIds = new ArrayList<>();
        try {
            List<HulkTransactionActivity> hulkTransactionActivityList = businessActivityLogger.read(properties.getRecoverySize());
            if (CollectionUtils.isEmpty(hulkTransactionActivityList)) {
                logger.info("HulkJob businessActivityList is null");
                return;
            }
            for (HulkTransactionActivity hulkTransactionActivity : hulkTransactionActivityList) {
                businessActivityIds.add(getBusinessActivityIdStr(hulkTransactionActivity.getBusinessActivity().getId()));
                BusinessActivityContextHolder.setContext(hulkTransactionActivity.getHulkContext().getBac());
                RuntimeContextHolder.setContext(hulkTransactionActivity.getHulkContext().getRc());
                String businessActivityIdStr = getBusinessActivityIdStr(hulkTransactionActivity.getBusinessActivity().getId());
                int retryCount = getRetryCount(businessActivityIdStr);
                if (retryCount > retryTranactionCount) {
                    logger.error(String.format("recover failed with max retry count,will not try again" + "retried count:%d", retryCount));
                    continue;
                }
                if (hulkTransactionActivity.getBusinessActivity().getStatus() ==  BusinessActivityStatus.COMMITTING
                    || hulkTransactionActivity.getBusinessActivity().getStatus() == BusinessActivityStatus.COMMITING_FAILED
                    || hulkTransactionActivity.getBusinessActivity().getStatus() == BusinessActivityStatus.ROLLBACKED) {
                    bam.commit();
                } else if (hulkTransactionActivity.getBusinessActivity().getStatus() == BusinessActivityStatus.ROLLBACKING
                            || hulkTransactionActivity.getBusinessActivity().getStatus() == BusinessActivityStatus.ROLLBACKING_FAILED) {
                    bam.rollback();
                }
            }
            businessActivityLogger.remove(businessActivityIds);
        } catch (SQLException ex) {
            logger.error("Hulk Retry Exception", ex);
        } catch (Exception ex) {
            logger.error("Hulk Retry Exception", ex);
        }
    }

    private int getRetryCount(String businessActivityIdStr) {
        if (!map.containsKey(businessActivityIdStr)) {
            map.put(businessActivityIdStr, new AtomicInteger(0));
        }
        return map.get(businessActivityIdStr).incrementAndGet();
    }

}
