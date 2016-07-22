package com.smartdengg.anrsquirrel.lib;

import android.os.Debug;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Printer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 创建时间:  16/7/20 下午12:07 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
class SquirrelPrinter implements Printer {

  public static final String START_SIGNAL = ">>>>>";
  public static final String END_SIGNAL = "<<<<<";

  private AtomicBoolean isDumping = new AtomicBoolean(false);

  private volatile long startNanos;
  private volatile long stopNanos;
  private long interval;
  private boolean shouldIgnoreDebugger;
  private boolean onlyMainThread;
  private final Handler ANRHandler;
  private final Handler checkLockHandler;
  private ANRError anrError;
  private Callback callback;
  private Runnable ANRRunnable = new Runnable() {
    @Override public void run() {

      if (onlyMainThread) {
        anrError = ANRError.onlyMainThread();
      } else {
        anrError = ANRError.allThread();
      }

      if (isDumping.get()) checkLockHandler.postDelayed(checkLockRunnable, (long) (interval * 0.3));

      /*String name = Thread.currentThread().getName();
      if (!name.contains("dalvik") && !name.contains("java") && !name.contains("com.android")) {
        anrError = ANRError.allThread();
      }*/
    }
  };

  private Runnable checkLockRunnable = new Runnable() {
    @Override public void run() {
      if (isDumping.get() && callback != null) callback.onBlockOccur(anrError);
    }
  };

  public SquirrelPrinter(int interval, boolean shouldIgnoreDebugger, boolean onlyMainThread,
      Callback callback) {
    this.interval = interval;
    this.shouldIgnoreDebugger = shouldIgnoreDebugger;
    this.onlyMainThread = onlyMainThread;
    this.callback = callback;
    this.ANRHandler = HandlerFactory.createdHandler("ANRHandler");
    this.checkLockHandler = HandlerFactory.createdHandler("CheckLockHandler");
  }

  @Override public void println(String x) {

    if (this.isStart(x)) {
      SquirrelPrinter.this.startNanos = System.nanoTime();

      if (isDumping.get()) return;
      isDumping.set(true);

      this.ANRHandler.removeCallbacks(ANRRunnable);
      this.checkLockHandler.removeCallbacks(checkLockRunnable);
      this.ANRHandler.postDelayed(ANRRunnable, (long) (interval * 0.8));
    } else if (this.isEnd(x)) {
      SquirrelPrinter.this.stopNanos = System.nanoTime();

      isDumping.set(false);

      if (!isBlocked()) {
        this.ANRHandler.removeCallbacks(ANRRunnable);
        this.checkLockHandler.removeCallbacks(checkLockRunnable);
      } else {
        if (callback != null) callback.onBlockOccur(anrError);
      }
    }
  }

  private boolean isStart(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(START_SIGNAL);
  }

  private boolean isEnd(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(END_SIGNAL);
  }

  private boolean isBlocked() {
    long lengthMillis =
        TimeUnit.NANOSECONDS.toMillis(stopNanos) - TimeUnit.NANOSECONDS.toMillis(startNanos);
    return !(lengthMillis <= interval || !shouldIgnoreDebugger && Debug.isDebuggerConnected());
  }

  interface Callback {

    void onBlockOccur(ANRError anrError);
  }
}
