package com.smartdengg.anrsquirrel.lib;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 创建时间:  16/7/18 下午9:54 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class HandlerFactory {

  private static final String THREAD_PREFIX = "|ANRSquirrel|";

  private HandlerFactory() {
    throw new IllegalStateException("No instance!");
  }

  public static Handler createdHandler() {
    return createdHandler("");
  }

  public static Handler createdHandler(@NotNull String suffix) {
    return new HandlerThreadWrapper(suffix).getHandler();
  }

  private static class HandlerThreadWrapper {
    private Handler handler = null;

    public HandlerThreadWrapper(String suffix) {
      HandlerThread handlerThread = new android.os.HandlerThread(
          THREAD_PREFIX + (TextUtils.isEmpty(suffix) ? "" : "_" + suffix));
      handlerThread.start();
      handler = new Handler(handlerThread.getLooper());
    }

    public Handler getHandler() {
      return handler;
    }
  }
}
