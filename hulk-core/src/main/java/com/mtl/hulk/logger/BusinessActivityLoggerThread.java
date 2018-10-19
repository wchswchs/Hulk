package com.mtl.hulk.logger;

import com.esotericsoftware.kryo.io.Output;
import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.common.Constants;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.serializer.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

public class BusinessActivityLoggerThread extends AbstractHulk implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerThread.class);

    private HulkContext ctx;

    public BusinessActivityLoggerThread(HulkProperties properties, HulkContext ctx) {
        super(properties);
        this.ctx = ctx;
    }

    /**
     * 异步记录事务日志
     */
    @Override
    public void run() {
        logger.info("Writing Transaction Log......");
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(properties);
        try {
            KryoSerializer serializer = new KryoSerializer();
            String[] transaction = ctx.getRc().getActivity().getId().formatString().split("_");
            File logFileWriteDir = new File(properties.getSnapShotLogDir(), Constants.LOG_FILE_PREFIX + "." +
                    transaction[0] + "_" + transaction[1]);
            File logFileWriter = new File(logFileWriteDir.getAbsolutePath(),
                    ctx.getRc().getActivity().getId().getSequence());
            Output fos = new Output(new FileOutputStream(logFileWriter));
            serializer.write(ctx, HulkContext.class, fos);
            if (bal.write(ctx.getRc(), ctx.getBac())) {
                logFileWriter.delete();
            }
            logger.info("Writing Transaction Log End！");
        } catch (Exception e) {
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
