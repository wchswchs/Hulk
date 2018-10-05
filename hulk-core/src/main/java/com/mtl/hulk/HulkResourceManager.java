package com.mtl.hulk;

import com.mtl.hulk.bam.BusinessActivityManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HulkResourceManager {

    private static final List<HulkInterceptor> interceptors = new ArrayList<HulkInterceptor>();
    private static HulkDataSource datasource;
    private static BusinessActivityManagerImpl bam;
    private static final Map<String, Object> clients = new ConcurrentHashMap<String, Object>();

    public static final List<HulkInterceptor> getInterceptors() {
        return interceptors;
    }

    public static void destroy() {
        for (HulkInterceptor interceptor : interceptors) {
            interceptor.destroy();
        }
        datasource.destroy();
        bam.destroy();
    }

    public static void setDatasource(HulkDataSource datasource) {
        HulkResourceManager.datasource = datasource;
    }

    public static HulkDataSource getDatasource() {
        return datasource;
    }

    public static void setBam(BusinessActivityManagerImpl bam) {
        HulkResourceManager.bam = bam;
    }

    public static BusinessActivityManagerImpl getBam() {
        return bam;
    }

    public static Map<String, Object> getClients() {
        return clients;
    }

}
