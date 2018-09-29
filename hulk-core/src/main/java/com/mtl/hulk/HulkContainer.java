package com.mtl.hulk;

import java.util.ArrayList;
import java.util.List;

public class HulkContainer {

    private static final List<HulkInterceptor> interceptors = new ArrayList<HulkInterceptor>();
    private static HulkDataSource datasource;

    public static final List<HulkInterceptor> getInterceptors() {
        return interceptors;
    }

    public static void destroy() {
        for (HulkInterceptor interceptor : interceptors) {
            interceptor.destroy();
        }
    }

    public static void setDatasource(HulkDataSource datasource) {
        HulkContainer.datasource = datasource;
    }

    public static HulkDataSource getDatasource() {
        return datasource;
    }

}
