package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.mtl.hulk.context.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;

public class BrokerInterceptor implements MethodInterceptor, Serializable {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object result = methodInvocation.proceed();
        HulkContext subBusinessActivity = JSONObject.parseObject((String) result, HulkContext.class);

        RuntimeContext rc = RuntimeContextHolder.getContext();
        rc.getActivity().getAtomicTryActions().addAll(subBusinessActivity.getRc().getActivity().getAtomicTryActions());
        rc.getActivity().getAtomicCommitActions().addAll(subBusinessActivity.getRc().getActivity().getAtomicCommitActions());
        rc.getActivity().getAtomicRollbackActions().addAll(subBusinessActivity.getRc().getActivity().getAtomicRollbackActions());
        RuntimeContextHolder.setContext(rc);

        BusinessActivityContext bac = BusinessActivityContextHolder.getContext();
        bac.getParams().putAll(subBusinessActivity.getBac().getParams());
        BusinessActivityContextHolder.setContext(bac);

        return "ok";
    }

}