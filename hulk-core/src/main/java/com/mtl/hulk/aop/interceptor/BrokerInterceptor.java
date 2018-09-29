package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.HulkInterceptor;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.*;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.util.ExecutorUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrokerInterceptor extends HulkAspectSupport implements HulkInterceptor, MethodInterceptor, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BrokerInterceptor.class);

    private final ExecutorService tryExecutor = new ThreadPoolExecutor(bam.getProperties().getTrythreadPoolSize(),
                            Integer.MAX_VALUE, 10L,
                            TimeUnit.SECONDS, new SynchronousQueue<>(),
                                    (new ThreadFactoryBuilder()).setNameFormat("Try-Thread-%d").build());
    private static List<String> orders;

    public BrokerInterceptor(BusinessActivityManagerImpl bam) {
        super(bam, null);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!orders.contains(methodInvocation.getMethod().getName())) {
            orders.add(methodInvocation.getMethod().getName());
        }
        bam.setTryFuture(bam.getTryFuture().thenApplyAsync(am -> {
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
        }, tryExecutor));
        return "ok";
    }

    public static void setOrders(List<String> orders) {
        BrokerInterceptor.orders = orders;
    }

    public static List<String> getOrders() {
        return orders;
    }

    @Override
    public void destroy() {
        ExecutorUtil.gracefulShutdown(tryExecutor);
    }

    @Override
    public void destroyNow() {
    }

}