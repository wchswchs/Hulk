package com.mtl.hulk.executor;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.BusinessActivityStatus;
import org.apache.commons.lang3.BooleanUtils;

import java.util.concurrent.Callable;

public class BusinessActivityExecutor extends AbstractHulk implements Callable<Integer> {

    private BusinessActivityManagerImpl bam;
    private HulkContext ctx;

    public BusinessActivityExecutor(BusinessActivityManagerImpl bam, HulkContext ctx) {
        this.bam = bam;
        this.ctx = ctx;
    }

    @Override
    public Integer call() {
        RuntimeContextHolder.setContext(ctx.getRc());
        BusinessActivityContextHolder.setContext(ctx.getBac());
        boolean status = false;
        if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.TRIED) {
            RuntimeContextHolder.getContext().setException(null);
            status = bam.commit();
            if (RuntimeContextHolder.getContext()
                    .getException() != null && RuntimeContextHolder.getContext()
                    .getException().getCode() == HulkErrorCode.INTERRUPTED.getCode()) {
                return BooleanUtils.toInteger(status);
            }
            if (status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTED);
            } else {
                RuntimeContextHolder.getContext().setException(null);
                status = bam.rollback();
                if (RuntimeContextHolder.getContext().getException() != null &&
                        RuntimeContextHolder.getContext().getException().getCode() == HulkErrorCode.INTERRUPTED.getCode()) {
                    return BooleanUtils.toInteger(status);
                }
                if (!status) {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
                } else {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
                }
            }
        } else {
            RuntimeContextHolder.getContext().setException(null);
            status = bam.rollback();
            if (RuntimeContextHolder.getContext().getException() != null &&
                    RuntimeContextHolder.getContext().getException().getCode() == HulkErrorCode.INTERRUPTED.getCode()) {
                return BooleanUtils.toInteger(status);
            }
            if (!status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
            } else {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
            }
        }
        return BooleanUtils.toInteger(status);
    }

}
