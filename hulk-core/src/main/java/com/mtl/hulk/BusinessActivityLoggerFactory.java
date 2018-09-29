package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.logger.data.sql.MySQLLoggerStorage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessActivityLoggerFactory {

    private final static Logger logger = LoggerFactory.getLogger(BusinessActivityLoggerFactory.class);

    public static BusinessActivityLogger getStorage(HulkProperties properties){
        try {
            if(StringUtils.equals("mysql", properties.getLoggerStorage())){
                return new MySQLLoggerStorage(HulkResourceManager.getDatasource(),
                        (HulkSerializer) properties.getLogSerialize().newInstance());
            }
        } catch (InstantiationException initEx) {
            logger.error("New Serializer Instance Error", initEx);
        } catch (IllegalAccessException accessEx) {
            logger.error("New Serializer Instance Error", accessEx);
        }

        return null;
    }

}
