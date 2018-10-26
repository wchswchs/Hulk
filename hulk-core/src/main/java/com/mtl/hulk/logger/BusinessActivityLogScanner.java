package com.mtl.hulk.logger;

import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.HulkResourceManager;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.HulkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class BusinessActivityLogScanner implements Runnable {

    private HulkProperties properties;

    private static final Logger logger = LoggerFactory.getLogger(BusinessActivityLogScanner.class);

    public BusinessActivityLogScanner(HulkProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        if (HulkResourceManager.getClients().size() == 0) {
            return;
        }
        logger.info("Scanning Transaction Log......");
        File logFileWriteDir = new File(HulkResourceManager.getSnapShot().getHeader().getDir());
        File[] logFiles = logFileWriteDir.listFiles();
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(properties);
        File runFile = null;
        try {
            if (logFiles != null) {
                int i = 0;
                for (File f : logFiles) {
                    Thread.sleep(1000);
                    logger.info("Scanning File: {}", f.getName());
                    runFile = f;
                    List<HulkContext> datas = HulkResourceManager.getSnapShot().read(f, HulkContext.class);
                    if (datas != null) {
                        if (bal.write(datas)) {
                            f.delete();
                        }
                    }
                    i ++;
                }
            }
        } catch (Exception ex) {
            logger.error("Flush Transaction Log to DB Exception", ex);
            logger.error("Flush Transaction Log to DB Exception File: {}", runFile.getName());
        }
    }

}
