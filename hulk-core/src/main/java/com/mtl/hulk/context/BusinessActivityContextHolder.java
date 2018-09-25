package com.mtl.hulk.context;

public class BusinessActivityContextHolder {

    private static final ThreadLocal<BusinessActivityContext> contextHolder = new ThreadLocal<BusinessActivityContext>();

    public static BusinessActivityContext getContext() {
        BusinessActivityContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }
        return ctx;
    }

    public static void setContext(BusinessActivityContext context) {
        contextHolder.set(context);
    }

    public static BusinessActivityContext createEmptyContext() {
        return new BusinessActivityContext();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

}
