package com.smartdengg.anrsquirrel.lib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;
import com.smartdengg.squirrel.ANRError;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 创建时间:  16/7/20 下午12:07 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
class SquirrelPrinter implements Printer {

  private static final String TAG = SquirrelPrinter.class.getSimpleName();
  public static final String START_SIGNAL = ">>>>>";
  public static final String END_SIGNAL = "<<<<<";
  private static final String CALLBACK = "CALLBACK";
  private static final String ERROR = "ERROR";

  private AtomicBoolean isDumping = new AtomicBoolean(false);

  private volatile long startNanos;
  private volatile long stopNanos;
  private long interval;
  private boolean onlyMainThread;
  private final Handler ANRHandler;
  private final Handler deadLockHandler;
  private ANRError anrError;
  private Callback callback;
  private Runnable ANRRunnable = new Runnable() {
    @Override public void run() {

      if (onlyMainThread) {
        anrError = ANRErrorFactory.onlyMainThread();
      } else {
        anrError = ANRErrorFactory.allThread();
      }

      if (isDumping.get()) deadLockHandler.postDelayed(deadLockRunnable, interval * 2);

      /*String name = Thread.currentThread().getName();
      if (!name.contains("dalvik") && !name.contains("java") && !name.contains("com.android")) {
        anrError = ANRError.allThread();
      }*/
    }
  };

  private Runnable deadLockRunnable = new Runnable() {
    @Override public void run() {
      if (isDumping.get()) {
        Log.w(TAG, "From deadLockRunnable");
        SquirrelPrinter.this.sendWrapperMessageDelay(anrError, (long) (interval * 0.5));
      }
    }
  };

  @SuppressWarnings("HandlerLeak") static final Handler HANDLER =
      new Handler(HandlerFactory.getErrorHandler().getLooper()) {
        @Override public void handleMessage(Message msg) {
          Bundle bundle = msg.getData();
          SquirrelPrinter.Callback callback = (SquirrelPrinter.Callback) bundle.get(CALLBACK);
          ANRError anrError = (ANRError) bundle.get(ERROR);
          if (callback != null && anrError != null) callback.onBlocked(anrError);
        }
      };

  SquirrelPrinter(int interval, boolean onlyMainThread, Callback callback) {
    this.interval = interval;
    this.onlyMainThread = onlyMainThread;
    this.callback = callback;
    this.ANRHandler = HandlerFactory.getANRHandler();
    this.deadLockHandler = HandlerFactory.getCheckLockHandler();
  }

  @Override public void println(String x) {

    if (this.isDispatching(x)) {

      this.startNanos = System.nanoTime();
      if (isDumping.get()) return;
      isDumping.set(true);

      this.startDumping();
    } else if (this.isFinished(x)) {

      this.stopNanos = System.nanoTime();
      isDumping.set(false);

      this.stopDumping();
      if (isBlock()) {
        this.sendWrapperMessageDelay(anrError, (long) (interval * 0.5));
      }
    }
  }

  private void sendWrapperMessageDelay(ANRError error, long delayMillis) {
    Message message = HANDLER.obtainMessage();
    Bundle bundle = new Bundle();
    bundle.putSerializable(CALLBACK, callback);
    bundle.putSerializable(ERROR, error);
    message.setData(bundle);
    HANDLER.sendMessageDelayed(message, delayMillis);
  }

  private void startDumping() {
    this.stopDumping();
    this.ANRHandler.postDelayed(ANRRunnable, (long) (interval * 0.8));
  }

  private void stopDumping() {
    this.ANRHandler.removeCallbacks(ANRRunnable);
    this.deadLockHandler.removeCallbacks(deadLockRunnable);
  }

  private boolean isDispatching(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(START_SIGNAL);
  }

  private boolean isFinished(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(END_SIGNAL);
  }

  private boolean isBlock() {
    long lengthMillis =
        TimeUnit.NANOSECONDS.toMillis(stopNanos) - TimeUnit.NANOSECONDS.toMillis(startNanos);
    return lengthMillis > interval;
  }

  interface Callback extends Serializable {

    void onBlocked(ANRError anrError);
  }
}
