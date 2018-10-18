package com.mtl.hulk.logger;

import com.esotericsoftware.kryo.io.Input;
import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.serializer.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

public class BusinessActivityLogScanner implements Runnable {

    private HulkProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(BusinessActivityLogScanner.class);

    public BusinessActivityLogScanner(HulkProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        logger.info("Scanning Transaction Log......");
        File logFileWriteDir = new File(properties.getSnapShotLogDir());
        File[] logDirs = logFileWriteDir.listFiles();
        KryoSerializer serializer = new KryoSerializer();
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(properties);
        try {
            if (logDirs != null) {
                int i = 0;
                for (File d : logDirs) {
                    for (File f : d.listFiles()) {
                        if (i > 200) {
                            Thread.sleep(2);
                        }
                        Input input = new Input(new FileInputStream(f));
                        HulkContext ctx = serializer.read(HulkContext.class, input);
                        if (bal.write(ctx.getRc(), ctx.getBac())) {
                            f.delete();
                        }
                        i ++;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Scan Transaction Log to DB Exception: {}", ex);
        }
    }

}
