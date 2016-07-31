package com.smartdengg.anrsquirrel.lib;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;

/**
 * 创建时间:  16/7/18 下午9:54 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class HandlerFactory {

  static final String THREAD_PREFIX = "|ANRSquirrel|";

  private static HandlerThreadWrapper ANRHandler =
      new HandlerThreadWrapper("BlockedHandler", Process.THREAD_PRIORITY_DEFAULT);
  private static HandlerThreadWrapper checkLockHandler =
      new HandlerThreadWrapper("DeadLockHandler", Process.THREAD_PRIORITY_BACKGROUND);
  private static HandlerThreadWrapper errorHandler = new HandlerThreadWrapper("ANRError", -20);

  private HandlerFactory() {
    throw new IllegalStateException("No instance!");
  }

  public static Handler getANRHandler() {
    return ANRHandler.getHandler();
  }

  public static Handler getCheckLockHandler() {
    return checkLockHandler.getHandler();
  }

  public static Handler getErrorHandler() {
    return errorHandler.getHandler();
  }

  private static class HandlerThreadWrapper {
    private Handler handler = null;

    public HandlerThreadWrapper(String suffix, int priority) {
      HandlerThread handlerThread =
          new HandlerThread(THREAD_PREFIX + (TextUtils.isEmpty(suffix) ? "" : "_" + suffix + "  "),
              priority);
      handlerThread.start();
      handler = new Handler(handlerThread.getLooper());
    }

    public Handler getHandler() {
      handler.removeCallbacksAndMessages(null);
      return handler;
    }
  }
}
