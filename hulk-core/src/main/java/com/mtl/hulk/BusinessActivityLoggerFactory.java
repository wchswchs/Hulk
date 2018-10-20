package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.logger.data.sql.MySQLLoggerStorage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessActivityLoggerFactory {

    private final static Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerFactory.class);

    public static BusinessActivityLogger getStorage(HulkProperties properties){
        if(StringUtils.equals("mysql", properties.getLoggerStorage())) {
            return new MySQLLoggerStorage(HulkResourceManager.getDatasource(),
                                            properties.getLogSerialize());
        }
        return null;
    }

}
