package com.mtl.hulk.listener;

import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.model.AtomicAction;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BusinessActivityListener extends HulkListener {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityListener.class);

    public BusinessActivityListener(HulkDataSource ds) {
        super(ds);
    }

    @Override
    public boolean process() {
        List<AtomicAction> currentActions = new ArrayList<>();
        RuntimeContext context = RuntimeContextHolder.getContext();
        if (context.getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
            currentActions = context.getActivity().getAtomicCommitActions();
        }
        if (context.getActivity().getStatus() == BusinessActivityStatus.ROLLBACKING) {
            currentActions = context.getActivity().getAtomicRollbackActions();
        }
        for (int i = 0; i < context.getActivity().getAtomicTryActions().size(); i ++) {
            AtomicActionListener listener = new AtomicActionListener(currentActions.get(i), dataSource, applicationContext, context.getActivity().getAtomicTryActions().get(i));
            listener.setBam(bam);
            listener.setLoggerExecutor(bam.getLoggerExecutor());
            listener.setApplicationContext(bam.getApplicationContext());
            boolean status = listener.process();
            if (status == false) {
                return false;
            }
        }
        return true;
    }

}
