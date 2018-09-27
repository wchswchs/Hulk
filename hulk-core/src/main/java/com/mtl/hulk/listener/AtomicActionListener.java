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
            ExecutorService loggerExecutor = bam.getLogExecutor();
            bam.setRunFuture(bam.getRunFuture().thenApplyAsync(ctx -> {
                if (ctx.getRc().getException() != null) {
                    return ctx;
                }
                Object object = null;
                if (applicationContext.getId().split(":")[0].equals(action.getServiceOperation().getService())) {
                    object = applicationContext.getBean(tryAction.getServiceOperation().getBeanClass());
                } else {
                    object = bam.getClients().get(action.getServiceOperation().getService());
                }
                try {
                    logger.info("Transaction Executor running: {}", action.getServiceOperation().getName());
                    Method method = object.getClass().getMethod(action.getServiceOperation().getName(), BusinessActivityContext.class);
                    Object ret = method.invoke(object, bac);
                    if (((boolean) ret) == false) {
                        if (ctx.getRc().getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
                            ctx.getRc().setException(new HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                                    MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                                            ctx.getRc().getActivity().getId().formatString(),
                                            action.getServiceOperation().getName())));
                        } else {
                            ctx.getRc().setException(new HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                                    MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                                            ctx.getRc().getActivity().getId().formatString(),
                                            action.getServiceOperation().getName())));
                        }
                        return ctx;
                    }
                } catch (InvocationTargetException ex) {
                    logger.error("Hulk Commit/Rollback Exception", ex);
                    if (ex.getTargetException().getMessage().contains("interrupted")) {
                        ctx.getRc().setException(new HulkException(HulkErrorCode.INTERRUPTED.getCode(),
                                HulkErrorCode.INTERRUPTED.getMessage()));
                    }
                    BusinessActivityException bax = new BusinessActivityException();
                    bax.setId(ctx.getRc().getActivity().getId());
                    bax.setException(ex.getTargetException().getMessage());
                    BusinessActivityLoggerExceptionThread exceptionLogThread =new BusinessActivityLoggerExceptionThread(bam.getProperties(), bam.getDataSource(),
                            new HulkContext(ctx.getBac(), ctx.getRc()));
                    exceptionLogThread.setEx(bax);
                    loggerExecutor.submit(exceptionLogThread);
                    return ctx;
                } catch (Throwable ex) {
                    logger.error("Hulk Commit/Rollback Exception", ex);
                    if (ctx.getRc().getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
                        ctx.getRc().setException(new HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                                MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                                        ctx.getRc().getActivity().getId().formatString(),
                                        action.getServiceOperation().getName())));
                    } else {
                        ctx.getRc().setException(new HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                                MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                                        ctx.getRc().getActivity().getId().formatString(),
                                        action.getServiceOperation().getName())));
                    }
                    BusinessActivityException bax = new BusinessActivityException();
                    bax.setId(ctx.getRc().getActivity().getId());
                    bax.setException(ex.getMessage());
                    BusinessActivityLoggerExceptionThread exceptionLogThread = new BusinessActivityLoggerExceptionThread(bam.getProperties(), bam.getDataSource(),
                            new HulkContext(ctx.getBac(), ctx.getRc()));
                    exceptionLogThread.setEx(bax);
                    loggerExecutor.submit(exceptionLogThread);
                    return ctx;
                } finally {
                }
                return ctx;
            }, bam.getRunExecutor()));
        }
        return true;
    }

}
