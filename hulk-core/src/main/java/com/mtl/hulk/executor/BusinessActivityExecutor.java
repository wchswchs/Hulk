package com.mtl.hulk.executor;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityStatus;
import org.apache.commons.lang3.BooleanUtils;

import java.util.concurrent.Callable;

public class BusinessActivityExecutor extends AbstractHulk implements Callable<Integer> {

    private BusinessActivityManagerImpl bam;

    public BusinessActivityExecutor(BusinessActivityManagerImpl bam) {
        this.bam = bam;
    }

    @Override
    public Integer call() {
        boolean status = false;
        if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.TRIED) {
            status = bam.commit();
        }
        if (!status || RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITING_FAILED) {
            status = bam.rollback();
            if (!status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
            } else {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
            }
        } else {
            RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTED);
        }
        return BooleanUtils.toInteger(status);
    }

}
