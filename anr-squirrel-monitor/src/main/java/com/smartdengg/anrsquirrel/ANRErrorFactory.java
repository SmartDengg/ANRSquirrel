package com.smartdengg.anrsquirrel;

import android.os.Looper;

/**
 * Created by SmartDengg on 2016/7/23.
 */
class ANRErrorFactory {

  private static final Thread mainThread = Looper.getMainLooper().getThread();

  private ANRErrorFactory() {
    throw new IllegalStateException("No instance!");
  }

  public static ANRError onlyMainThread() {
    return ANRError.dumpStackTrace(mainThread, true, HandlerFactory.THREAD_PREFIX);
  }

  public static ANRError allThread() {
    return ANRError.dumpStackTrace(mainThread, false, HandlerFactory.THREAD_PREFIX);
  }
}
