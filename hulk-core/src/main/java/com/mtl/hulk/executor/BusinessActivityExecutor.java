package com.mtl.hulk.executor;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityStatus;
import org.apache.commons.lang3.BooleanUtils;

import java.util.concurrent.Callable;

public class BusinessActivityExecutor extends AbstractHulk implements Callable<Integer> {

    private BusinessActivityManagerImpl bam;
    private boolean isRunCommit;

    public BusinessActivityExecutor(BusinessActivityManagerImpl bam, boolean isRunCommit) {
        this.bam = bam;
        this.isRunCommit = isRunCommit;
    }

    @Override
    public Integer call() {
        boolean status = false;
        if (isRunCommit) {
            status = bam.commit();
            if (status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTED);
            } else {
                status = bam.rollback();
                if (!status) {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
                } else {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
                }
            }
        } else {
            status = bam.rollback();
            if (!status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
            } else {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
            }
        }
        return BooleanUtils.toInteger(status);
    }

}
