package com.mtl.hulk.util;

import java.util.concurrent.Future;

public class FutureUtil {

    public static void gracefulCancel(Future future) {
        if (future != null) {
            if (!future.isCancelled()) {
                future.cancel(false);
            }
        }
    }

    public static void cancelNow(Future future) {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
    }

}
