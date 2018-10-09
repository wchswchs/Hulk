package com.mtl.hulk.api;

import org.springframework.context.ApplicationContext;

import java.util.Map;

public interface NetworkCommunication {

    /**
     * Return provider map like map <helloService, helloServiceBean>
     * @param ctx ApplicationContext
     * @return provider map
     */
    Map<String, Object> getProviders(ApplicationContext ctx);

}
