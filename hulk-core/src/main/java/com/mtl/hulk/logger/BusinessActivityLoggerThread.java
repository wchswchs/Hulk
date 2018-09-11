package com.mtl.hulk.logger;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.RuntimeContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class BusinessActivityLoggerThread extends AbstractHulk implements Runnable {

    private Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerThread.class);

    public BusinessActivityLoggerThread(HulkProperties properties, HulkDataSource ds) {
        super(ds, properties);
    }

    @Override
    public void run() {
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(dataSource, properties);
        try {
            bal.write(RuntimeContextHolder.getContext(), BusinessActivityContextHolder.getContext());
        } catch (SQLException e) {
            logger.error("Hulk Log Write Exception", e);
        }
    }

}
