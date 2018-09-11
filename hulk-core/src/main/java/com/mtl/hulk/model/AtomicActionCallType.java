package com.mtl.hulk.model;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.feign.FeignClient;

public enum AtomicActionCallType {

    RequestResponse("RequestResponse", FeignClient.class),
    Async("Async", null),
    HAAsync("HAAsync", null);

    private final String type;
    private final Class<?> typeClass;

    private AtomicActionCallType(String type, Class<?> typeClass) {
        this.type = type;
        this.typeClass = typeClass;
    }

    public static AtomicActionCallType getAtomicActionCallType(String type){
        for (AtomicActionCallType atomicActionCallType : values()) {
            if (StringUtils.equals(atomicActionCallType.getType(), type)) {
                return atomicActionCallType;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

}
