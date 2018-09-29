package com.mtl.hulk;

import com.mtl.hulk.bam.BusinessActivityManagerImpl;

import java.util.ArrayList;
import java.util.List;

public class HulkResourceManager {

    private static final List<HulkInterceptor> interceptors = new ArrayList<HulkInterceptor>();
    private static HulkDataSource datasource;
    private static BusinessActivityManagerImpl bam;

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

}
