package com.mtl.hulk.logger;

import com.mtl.hulk.BusinessActivityLogger;
import com.mtl.hulk.BusinessActivityLoggerFactory;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.snapshot.FastFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
        File[] logFiles = logFileWriteDir.listFiles();
        BusinessActivityLogger bal = BusinessActivityLoggerFactory.getStorage(properties);
        try {
            if (logFiles != null) {
                int i = 0;
                for (File f : logFiles) {
                    if (i > 200) {
                        Thread.sleep(2);
                    }
                    FastFile ff = new FastFile(f, "r", properties.getTxLogBufferSize());
                    boolean eof = ff.read(new BusinessActivityLogCallback(bal));
                    if (eof) {
                        f.delete();
                    }
                    i ++;
                }
            }
        } catch (Exception ex) {
            logger.error("Flush Transaction Log to DB Exception: {}", ex);
        }
    }

}
