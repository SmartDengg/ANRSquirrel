package com.smartdengg.anrsquirrel.lib;

import android.os.Handler;
import android.os.HandlerThread;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

/**
 * 创建时间:  16/7/18 下午9:54 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class HandlerFactory {

  private static final String THREAD_PREFIX = "|ANRSquirrel|-";

  private static final AtomicInteger mAtomicInteger = new AtomicInteger();

  private HandlerFactory() {
    throw new IllegalStateException("No instance!");
  }

  public static Handler createdHandler() {
    return new HandlerThreadWrapper(mAtomicInteger.getAndIncrement()).getHandler();
  }

  public static Handler createdHandler(@NotNull String name) {
    return new HandlerThreadWrapper(name).getHandler();
  }

  private static class HandlerThreadWrapper {
    private Handler handler = null;

    public HandlerThreadWrapper(String name) {
      HandlerThread handlerThread = new android.os.HandlerThread(THREAD_PREFIX + name);
      handlerThread.start();
      handler = new Handler(handlerThread.getLooper());
    }

    public HandlerThreadWrapper(int name) {
      HandlerThread handlerThread = new android.os.HandlerThread(THREAD_PREFIX + name);
      handlerThread.start();
      handler = new Handler(handlerThread.getLooper());
    }

    public Handler getHandler() {
      return handler;
    }
  }
}
