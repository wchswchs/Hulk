package com.mtl.hulk.context;

public class RuntimeContextHolder {

    private static final InheritableThreadLocal<RuntimeContext> contextHolder = new InheritableThreadLocal<RuntimeContext>();

    public static RuntimeContext getContext() {
        RuntimeContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }
        return ctx;
    }

    public static void setContext(RuntimeContext context) {
        contextHolder.set(context);
    }

    public static RuntimeContext createEmptyContext() {
        return new RuntimeContext();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

}
