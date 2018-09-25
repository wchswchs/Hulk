package com.mtl.hulk.listener;

import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.context.*;
import com.mtl.hulk.logger.BusinessActivityLoggerExceptionThread;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;

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
            ExecutorService loggerExecutor = bam.getLogExecutor();
            try {
                Object object = applicationContext.getBean(tryAction.getServiceOperation().getBeanClass());
                Method method = object.getClass().getMethod(action.getServiceOperation().getName(), BusinessActivityContext.class);
                if (method == null) {
                    return false;
                }
                Object ret = method.invoke(object, bac);
                if (ret == null) {
                    return false;
                }
                if (((boolean) ret) == false) {
                    if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
                        RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                                MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                                        RuntimeContextHolder.getContext().getActivity().getId().formatString(),
                                        action.getServiceOperation().getName())));
                    } else {
                        RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                                MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                                        RuntimeContextHolder.getContext ().getActivity().getId().formatString(),
                                        action.getServiceOperation().getName())));
                    }
                    return false;
                }
            } catch (InvocationTargetException ex) {
                logger.error("Hulk Commit/Rollback Exception", ex);
                if (ex.getTargetException().getMessage().contains("interrupted")) {
                    RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.INTERRUPTED.getCode(),
                                                        HulkErrorCode.INTERRUPTED.getMessage()));
                }
                BusinessActivityException bax = new BusinessActivityException();
                bax.setId(context.getActivity().getId());
                bax.setException(ex.getTargetException().getMessage());
                BusinessActivityLoggerExceptionThread exceptionLogThread =new BusinessActivityLoggerExceptionThread(bam.getProperties(), bam.getDataSource(),
                        new HulkContext(BusinessActivityContextHolder.getContext(), RuntimeContextHolder.getContext()));
                exceptionLogThread.setEx(bax);
                loggerExecutor.submit(exceptionLogThread);
                return false;
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
                BusinessActivityLoggerExceptionThread exceptionLogThread =new BusinessActivityLoggerExceptionThread(bam.getProperties(), bam.getDataSource(),
                        new HulkContext(BusinessActivityContextHolder.getContext(), RuntimeContextHolder.getContext()));
                exceptionLogThread.setEx(bax);
                loggerExecutor.submit(exceptionLogThread);
                return false;
            }
        }
        return true;
    }

}
