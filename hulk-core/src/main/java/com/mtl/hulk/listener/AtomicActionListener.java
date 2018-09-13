package com.mtl.hulk.listener;

import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.*;
import com.mtl.hulk.context.RuntimeContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.text.MessageFormat;

public class AtomicActionListener extends HulkListener {

    private volatile AtomicAction tryAction;

    private final Logger logger = LoggerFactory.getLogger(AtomicActionListener.class);

    public AtomicActionListener(AtomicAction action, HulkDataSource ds, ApplicationContext applicationContext, AtomicAction tryAction) {
        super(action, ds, applicationContext);
        this.tryAction = tryAction;
    }

    @Override
    public boolean process() {
        if (action.getServiceOperation().getType() == ServiceOperationType.TCC) {
            BusinessActivityContext bac = BusinessActivityContextHolder.getContext();
            RuntimeContext context = RuntimeContextHolder.getContext();
            try {
                Object object = applicationContext.getBean(tryAction.getServiceOperation().getBeanClass());
                Method method = object.getClass().getMethod(action.getServiceOperation().getName(), BusinessActivityContext.class);
                boolean response = (boolean) method.invoke(object, bac);
                if (response == false) {
                    if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
                        RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                                MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                                        RuntimeContextHolder.getContext().getActivity().getId().formatString(),
                                        action.getServiceOperation().getName())));
                    } else {
                        RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                                MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                                        RuntimeContextHolder.getContext().getActivity().getId().formatString(),
                                        action.getServiceOperation().getName())));
                    }
                    return false;
                }
            } catch (Throwable ex) {
                logger.error("Hulk Commit/Rollback Exception", ex);
                if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
                    RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                            MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(),
                            action.getServiceOperation().getName())));
                } else {
                    RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                            MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(),
                            action.getServiceOperation().getName())));
                }
                BusinessActivityException bax = new BusinessActivityException();
                bax.setId(context.getActivity().getId());
                bax.setException(ex.getMessage());
                loggerExceptionThread.setEx(bax);
                loggerExecutor.submit(loggerExceptionThread);
                return false;
            }
        }
        return true;
    }

}
