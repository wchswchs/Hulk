package com.mtl.hulk.logger;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.HulkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class BusinessActivityLoggerThread extends AbstractHulk implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerThread.class);
    private HulkContext ctx;

    public BusinessActivityLoggerThread(HulkProperties properties, HulkContext ctx) {
        super(properties);
        this.ctx = ctx;
    }

    @Override
    public void run() {
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(properties);
        try {
            bal.write(ctx.getRc(), ctx.getBac());
        } catch (SQLException e) {
            logger.error("Hulk Log Write Exception", e);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void destroyNow() {
    }

    @Override
    public void closeFuture() {
    }

}
