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
  private final Handler handler;
  private ANRError anrError;
  private Callback mCallback;
  private Runnable runnable = new Runnable() {
    @Override public void run() {

      anrError = ANRError.allThread();

      /*String name = Thread.currentThread().getName();
      if (!name.contains("dalvik") && !name.contains("java") && !name.contains("com.android")) {
        anrError = ANRError.allThread();
      }*/
    }
  };

  public SquirrelPrinter(int interval, boolean shouldIgnoreDebugger, Callback callback) {
    this.interval = interval;
    this.shouldIgnoreDebugger = shouldIgnoreDebugger;
    this.mCallback = callback;
    this.handler = HandlerFactory.createdHandler();
  }

  @Override public void println(String x) {

    if (isStart(x)) {
      SquirrelPrinter.this.startNanos = System.nanoTime();

      if (isDumping.get()) return;
      isDumping.set(true);

      this.handler.removeCallbacks(runnable);
      this.handler.postDelayed(runnable, (long) (interval * 0.75));
    } else if (isEnd(x)) {
      SquirrelPrinter.this.stopNanos = System.nanoTime();

      if (!isDumping.get()) return;
      isDumping.set(false);

      if (!isBlock()) {
        this.handler.removeCallbacks(runnable);
      } else {
        if (mCallback != null) mCallback.onBlockOccur(anrError);
      }
    }
  }

  private boolean isStart(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(START_SIGNAL);
  }

  private boolean isEnd(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(END_SIGNAL);
  }

  private boolean isBlock() {
    long lengthMillis =
        TimeUnit.NANOSECONDS.toMillis(stopNanos) - TimeUnit.NANOSECONDS.toMillis(startNanos);
    return !(lengthMillis <= interval || !shouldIgnoreDebugger && Debug.isDebuggerConnected());
  }

  interface Callback {

    void onBlockOccur(ANRError anrError);
  }
}
