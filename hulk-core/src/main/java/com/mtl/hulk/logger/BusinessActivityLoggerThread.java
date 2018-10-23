package com.mtl.hulk.logger;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.HulkResourceManager;
import com.mtl.hulk.common.Constants;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.serializer.HulkSerializer;
import com.mtl.hulk.serializer.kryo.KryoSerializer;
import com.mtl.hulk.snapshot.SnapShotHeader;
import com.mtl.hulk.snapshot.SnapShotRule;
import com.mtl.hulk.snapshot.io.FastFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class BusinessActivityLoggerThread extends AbstractHulk implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerThread.class);

    private HulkContext ctx;
    private final Object writeSnapshotLock = new Object();

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
        FastFile logFile = null;
        synchronized (writeSnapshotLock) {
            File file = getCurrentFile();
            logFile = new FastFile(file, "rw", HulkResourceManager.getSnapShot()
                                                    .getProperties().getBufferSize());
        }
        try {
            HulkSerializer serializer = new KryoSerializer();
            byte[] ctxLog = serializer.serialize(ctx);
            ctx.getRc().setException(new HulkException());
            logFile.write(ctxLog, logFile.getFile().length());
            logger.info("Writing Transaction SnapShot End！");
        } catch (Exception e) {
            logger.error("Hulk Log Write Exception", e);
        } finally {
            logFile.close();
        }
    }

    private synchronized File getCurrentFile() {
        String[] transaction = ctx.getRc().getActivity().getId().formatString().split("_");
        SnapShotHeader header = new SnapShotHeader(HulkResourceManager.getSnapShot().getProperties().getDir()
                        , Constants.TX_LOG_FILE_PREFIX + "." + transaction[0] + "_" + transaction[1]);
        SnapShotRule rule = HulkResourceManager.getSnapShot().getRule();
        return rule.run(header);
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
