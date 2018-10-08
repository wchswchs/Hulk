package com.mtl.hulk.logger;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class BusinessActivityLoggerExceptionThread extends AbstractHulk implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerExceptionThread.class);

    private AtomicReference<BusinessActivityException> ex = new AtomicReference<BusinessActivityException>();
    private HulkContext ctx;

    public BusinessActivityLoggerExceptionThread(HulkProperties properties, HulkContext ctx){
        super(properties);
        this.ctx = ctx;
    }

    @Override
    public void run() {
        logger.info("Writing Exception log.");
        RuntimeContextHolder.setContext(ctx.getRc());
        BusinessActivityContextHolder.setContext(ctx.getBac());
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(properties);
        if(null == bal) {
            logger.warn("businessActivityLogger获取为空，storage={}", properties.getLoggerStorage());
            return;
        }
        try {
            bal.writeEx(ex.get());
            logger.info("Written Exception log.");
        } catch (SQLException e) {
            logger.error("Hulk Log WriteEx Exception", e);
        }
    }

    public void setEx(BusinessActivityException ex) {
        this.ex.set(ex);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void destroyNow() {
    }

}
