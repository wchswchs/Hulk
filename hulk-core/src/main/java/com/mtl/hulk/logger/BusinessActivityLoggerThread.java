package com.mtl.hulk.logger;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.common.Constants;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.io.FastFile;
import com.mtl.hulk.serializer.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class BusinessActivityLoggerThread extends AbstractHulk implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerThread.class);

    private HulkContext ctx;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger logSuffix = new AtomicInteger(0);

    public BusinessActivityLoggerThread(HulkProperties properties, HulkContext ctx) {
        super(properties);
        this.ctx = ctx;
    }

    /**
     * 异步记录事务日志
     */
    @Override
    public void run() {
        logger.info("Writing Transaction SnapShot......");
        lock.lock();
        File file = getCurrentFile();
        FastFile logFile = new FastFile(file, "rw", properties.getTxLogBufferSize());
        try {
            byte[] ctxLog = KryoSerializer.serialize(ctx);
            int len = logFile.write(ctxLog, logFile.getStartPosition());
            logger.info("Writing Transaction SnapShot End！");
        } catch (Exception e) {
            logger.error("Hulk Log Write Exception", e);
        } finally {
            try {
                logFile.getFile().close();
            } catch (IOException ex) {
                logger.error("Close File Error", ex);
            }
            lock.unlock();
        }
    }

    private File getCurrentFile() {
        String[] transaction = ctx.getRc().getActivity().getId().formatString().split("_");
        File logFileWriter = new File(properties.getSnapShotLogDir(), Constants.TX_LOG_FILE_PREFIX + "." +
                transaction[0] + "_" + transaction[1]);
        if (logFileWriter.length() >= properties.getTxLogSizeLimit()) {
            if (logFileWriter.getName() == Constants.TX_LOG_FILE_PREFIX + "." +
                    transaction[0] + "_" + transaction[1]) {
                logFileWriter.renameTo(new File(properties.getSnapShotLogDir(), Constants.TX_LOG_FILE_PREFIX + "." +
                        transaction[0] + "_" + transaction[1] + "." + logSuffix.incrementAndGet()));
            }
            logFileWriter = new File(properties.getSnapShotLogDir(), Constants.TX_LOG_FILE_PREFIX + "." +
                    transaction[0] + "_" + transaction[1] + "." + logSuffix.incrementAndGet());
        }
        return logFileWriter;
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
