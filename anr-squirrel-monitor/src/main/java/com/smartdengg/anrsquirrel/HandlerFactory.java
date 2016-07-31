package com.smartdengg.anrsquirrel;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;

/**
 * 创建时间:  16/7/18 下午9:54 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
class HandlerFactory {

  static final String THREAD_PREFIX = "|ANRSquirrel|";

  private static HandlerThreadWrapper ANRHandler;
  private static HandlerThreadWrapper checkLockHandler;
  private static HandlerThreadWrapper errorHandler;

  private HandlerFactory() {
    throw new IllegalStateException("No instance!");
  }

  public static Handler getANRHandler() {

    if (ANRHandler == null) {
      synchronized (HandlerFactory.class) {
        if (ANRHandler == null) {
          ANRHandler = new HandlerThreadWrapper("BlockedHandler", Process.THREAD_PRIORITY_DEFAULT);
        }
      }
    }

    return ANRHandler.handler();
  }

  public static Handler getCheckLockHandler() {

    if (checkLockHandler == null) {
      synchronized (HandlerFactory.class) {
        if (checkLockHandler == null) {
          checkLockHandler =
              new HandlerThreadWrapper("DeadLockHandler", Process.THREAD_PRIORITY_BACKGROUND);
        }
      }
    }

    return checkLockHandler.handler();
  }

  public static Handler getErrorHandler() {

    if (errorHandler == null) {
      synchronized (HandlerFactory.class) {
        if (errorHandler == null) {
          errorHandler = new HandlerThreadWrapper("ANRError", -20);
        }
      }
    }

    return errorHandler.handler();
  }

  private static class HandlerThreadWrapper {
    private Handler handler = null;

    public HandlerThreadWrapper(String suffix, int priority) {
      HandlerThread handlerThread =
          new HandlerThread(THREAD_PREFIX + (TextUtils.isEmpty(suffix) ? "" : "_" + suffix + "  "),
              priority);
      handlerThread.start();
      this.handler = new Handler(handlerThread.getLooper());
    }

    public Handler handler() {
      this.handler.removeCallbacksAndMessages(null);
      return handler;
    }
  }
}
