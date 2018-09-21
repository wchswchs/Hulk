package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BrokerInterceptor extends HulkAspectSupport implements MethodInterceptor, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BrokerInterceptor.class);

    public BrokerInterceptor(BusinessActivityManagerImpl bam) {
        super(bam);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        bam.setFuture(bam.getFuture().thenApplyAsync(hc -> {
            try {
                Object result = methodInvocation.proceed();
                HulkContext subBusinessActivity = JSONObject.parseObject((String) result, HulkContext.class);
                RuntimeContext rc = hc.getRc();
                rc.getActivity().getAtomicTryActions().addAll(subBusinessActivity.getRc().getActivity().getAtomicTryActions());
                rc.getActivity().getAtomicCommitActions().addAll(subBusinessActivity.getRc().getActivity().getAtomicCommitActions());
                rc.getActivity().getAtomicRollbackActions().addAll(subBusinessActivity.getRc().getActivity().getAtomicRollbackActions());

                BusinessActivityContext bac = hc.getBac();
                bac.getParams().putAll(subBusinessActivity.getBac().getParams());
                return new HulkContext(bac, rc);
            } catch (Throwable t) {
                logger.error("Broker Request Exception", t);
            }
            return null;
        }));
        return "ok";
    }

}