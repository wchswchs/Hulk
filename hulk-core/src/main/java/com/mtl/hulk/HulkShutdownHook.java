package com.mtl.hulk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class HulkShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(HulkShutdownHook.class);

    private static final HulkShutdownHook hulkShutdownHook = new HulkShutdownHook();

    public static HulkShutdownHook getHulkShutdownHook() {
        return hulkShutdownHook;
    }

    /**
     * Has it already been destroyed or not?
     */
    private final AtomicBoolean destroyed;

    private HulkShutdownHook() {
        this.destroyed = new AtomicBoolean(false);
    }

    public void destroyAll() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        HulkResourceManager.destroy();
    }

}
