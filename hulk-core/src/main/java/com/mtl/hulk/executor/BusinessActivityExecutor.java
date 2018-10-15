package com.mtl.hulk.executor;

import com.mtl.hulk.HulkResourceManager;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityStatus;

import java.util.concurrent.Callable;

public class BusinessActivityExecutor implements Callable<Boolean> {

    private HulkContext ctx;

    public BusinessActivityExecutor(HulkContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 异步发起事务
     * @return
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        RuntimeContextHolder.setContext(ctx.getRc());
        BusinessActivityContextHolder.setContext(ctx.getBac());
        boolean status = false;
        try {
            if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.TRIED) {
                RuntimeContextHolder.getContext().setException(null);
                status = HulkResourceManager.getBam().commit();
                if (status) {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTED);
                    return status;
                }
                status = HulkResourceManager.getBam().rollback();
                if (!status) {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
                } else {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
                }
                return status;
            }
            status = HulkResourceManager.getBam().rollback();
            if (!status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
            } else {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
            }
            return status;
        } catch (Exception ex) {
            throw ex;
        }
    }

}
