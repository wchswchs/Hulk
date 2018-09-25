package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.*;
import com.mtl.hulk.message.HulkErrorCode;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

public class BrokerInterceptor extends HulkAspectSupport implements MethodInterceptor, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BrokerInterceptor.class);

    private static List<String> orders;

    public BrokerInterceptor(BusinessActivityManagerImpl bam) {
        super(bam);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!orders.contains(methodInvocation.getMethod().getName())) {
            orders.add(methodInvocation.getMethod().getName());
        }
        bam.setFuture(bam.getFuture().thenApplyAsync(am -> {
            try {
                String name = methodInvocation.getMethod().getName();
                logger.info("Try request sending: {}", name);
                Object result = methodInvocation.proceed();
                HulkContext subBusinessActivity = JSONObject.parseObject((String) result, HulkContext.class);
                am.put(orders.indexOf(name), subBusinessActivity);
                return am;
            } catch (Throwable t) {
                logger.error("Broker Request Exception", t);
                RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.TRY_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.TRY_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), methodInvocation.getMethod().getName())));
            }
            return null;
        }, bam.getTryExecutor()));
        return "ok";
    }

    public static void setOrders(List<String> orders) {
        BrokerInterceptor.orders = orders;
    }

    public static List<String> getOrders() {
        return orders;
    }

}