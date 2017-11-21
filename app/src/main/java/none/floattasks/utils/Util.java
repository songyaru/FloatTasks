package none.floattasks.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Author: songyaru | songyaru9@gmail.com
 * Date: 2017/11/21  13:05
 */

public class Util {

    private static final Object handlerLock = new Object();
    private static Handler handler = null;

    public static void runOnUiThread(Runnable runnable) {
        handler().post(runnable);
    }

    public static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private static Handler handler() {
        if (handler == null) {
            synchronized(handlerLock) {
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                }
            }
        }

        return handler;
    }



}