/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.anrsquirrel;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;
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
  private static final String START_SIGNAL = ">>>>>";
  private static final String END_SIGNAL = "<<<<<";

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
        if (callback != null) callback.onBlocked(anrError, true);
      }
    }
  };

  public SquirrelPrinter(ANRSquirrel anrSquirrel, Callback callback) {
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
        if (callback != null) {
          callback.onPreBlocking();
          callback.onBlocked(anrError, false);
          callback.onBlockCompleted();
        }
      }
    }
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

  public interface Callback extends Serializable {

    void onPreBlocking();

    void onBlocked(ANRError anrError, boolean isDeadLock);

    void onBlockCompleted();
  }
}
