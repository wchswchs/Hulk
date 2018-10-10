package com.mtl.hulk.traffic;

import com.mtl.hulk.extension.NetworkCommunication;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class FeignClientCommunication implements NetworkCommunication {

    @Override
    public Map<String, Object> getProviders(ApplicationContext ctx) {
        FeignClient annotation = null;
        Map<String, Object> providerMap = new HashMap<String, Object>();
        Map<String, Object> feignClientMap = ctx.getBeansWithAnnotation(FeignClient.class);
        for (Object client : feignClientMap.values()) {
            annotation = client.getClass().getInterfaces()[0].getAnnotation(FeignClient.class);
            providerMap.put(annotation.value(), client);
        }
        return providerMap;
    }

}
