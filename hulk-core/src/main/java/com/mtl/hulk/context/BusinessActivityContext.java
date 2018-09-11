package com.mtl.hulk.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BusinessActivityContext {

    Map<String, Object[]> params = new ConcurrentHashMap<String, Object[]>();

    public void setParams(Map<String, Object[]> params) {
        this.params = params;
    }

    public Map<String, Object[]> getParams() {
        return params;
    }

}
