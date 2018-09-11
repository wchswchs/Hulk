package com.mtl.hulk.task.tasks;

import com.mtl.hulk.BusinessActivityRestorer;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class HulkJob {
    private Logger logger = LoggerFactory.getLogger(HulkJob.class);

    @Autowired
    private BusinessActivityRestorer businessActivityRestorer;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void execute() {
        logger.info("HulkJob start......");
        businessActivityRestorer.run();
        logger.info("HulkJob end......");
    }
}
