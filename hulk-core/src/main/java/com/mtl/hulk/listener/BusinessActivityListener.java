package com.mtl.hulk.listener;

import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.model.AtomicAction;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class BusinessActivityListener extends HulkListener {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityListener.class);

    public BusinessActivityListener(HulkDataSource ds) {
        super(ds);
    }

    @Override
    public boolean process() {
        List<AtomicAction> currentActions = new CopyOnWriteArrayList<AtomicAction>();
        RuntimeContext context = RuntimeContextHolder.getContext();
        if (context.getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
            currentActions = context.getActivity().getAtomicCommitActions();
        }
        if (context.getActivity().getStatus() == BusinessActivityStatus.ROLLBACKING) {
            currentActions = context.getActivity().getAtomicRollbackActions();
        }
        try {
            bam.setRunFuture(CompletableFuture.completedFuture(
                                            new HulkContext(BusinessActivityContextHolder.getContext(),
                                            RuntimeContextHolder.getContext())));
            for (int i = 0; i < context.getActivity().getAtomicTryActions().size(); i ++) {
                AtomicActionListener listener = new AtomicActionListener(currentActions.get(i), dataSource, applicationContext,
                                                context.getActivity().getAtomicTryActions().get(i));
                listener.setBam(bam);
                listener.setApplicationContext(bam.getApplicationContext());
                boolean status = listener.process();
                if (status == false) {
                    return false;
                }
            }
            HulkContext ret = bam.getRunFuture().join();
            if (ret.getRc().getException() != null && ret.getRc().getException().getCode() > 0) {
                return false;
            }
            return true;
        } finally {
            if (bam.getRunFuture() != null) {
                if (bam.getRunFuture().isCompletedExceptionally() ||
                        bam.getRunFuture().isDone()) {
                    bam.getRunFuture().cancel(false);
                }
            }
        }
    }

}
