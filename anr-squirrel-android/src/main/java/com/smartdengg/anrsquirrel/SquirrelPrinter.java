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
import android.os.Message;
import android.text.TextUtils;
import android.util.Printer;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.smartdengg.anrsquirrel.ANRSquirrel.HANDLER;

/**
 * 创建时间:  16/7/20 下午12:07 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
class SquirrelPrinter implements Printer {

  private static final String TAG = SquirrelPrinter.class.getSimpleName();
  private static final String START_SIGNAL = ">>>>>";
  private static final String END_SIGNAL = "<<<<<";

  private volatile long startNanos;
  private volatile long stopNanos;
  private final long interval;
  private final boolean onlyMainThread;
  private final List<Printer> printers;
  private final Handler ANRHandler;
  private final Handler deadLockHandler;
  private final Callback callback;
  private ANRError anrError;

  boolean isStarted = false;

  private Runnable ANRAction = new Runnable() {
    @Override public void run() {
      if (onlyMainThread) {
        anrError = ANRErrorFactory.onlyMainThread();
      } else {
        anrError = ANRErrorFactory.allThread();
      }
      deadLockHandler.postDelayed(deadLockAction, interval * 2);

      /*String name = Thread.currentThread().getName();
      if (!name.contains("dalvik") && !name.contains("java") && !name.contains("com.android")) {
        anrError = ANRError.allThread();
      }*/
    }
  };

  private Runnable deadLockAction = new Runnable() {
    @Override public void run() {
      if (callback != null) callback.onBlocked(anrError, true);
    }
  };

  private Runnable stopANRAction = new Runnable() {
    @Override public void run() {
      ANRHandler.removeCallbacks(ANRAction);
    }
  };

  private Runnable stopDeadLockAction = new Runnable() {
    @Override public void run() {
      deadLockHandler.removeCallbacks(deadLockAction);
    }
  };

  SquirrelPrinter(ANRSquirrel anrSquirrel, Callback callback) {
    this.interval = anrSquirrel.interval;
    this.onlyMainThread = anrSquirrel.onlyMainThread;
    this.callback = callback;
    this.printers = anrSquirrel.printers;
    this.ANRHandler = HandlerFactory.getANRHandler();
    this.deadLockHandler = HandlerFactory.getCheckLockHandler();
  }

  @Override public void println(String x) {

    if (!isStarted) return;

    if (this.isDispatching(x)) {
      this.startNanos = System.nanoTime();

      /*begin dump*/
      this.startDumping();

      /*dispatch message*/
      this.dispatchingMsg(x);
    } else if (this.isFinished(x)) {
      this.stopNanos = System.nanoTime();

      /*dump end*/
      this.stopDumping();

      /*check block*/
      if (isBlock(startNanos, stopNanos, interval)) {
        if (callback != null) {
          callback.onPreBlocking();
          callback.onBlocked(anrError, false);
          callback.onBlockCompleted();
        }
      }

      /*dispatch message*/
      this.dispatchingMsg(x);
    }
  }

  private void dispatchingMsg(String x) {
    if (this.printers == null || this.printers.size() == 0) return;

    //noinspection ForLoopReplaceableByForEach
    for (Iterator<Printer> iterator = printers.iterator(); iterator.hasNext(); ) {
      Printer printer = iterator.next();
      printer.println(x);
    }
  }

  private void startDumping() {
    this.stopDumping();
    this.ANRHandler.postDelayed(ANRAction, (long) (interval * 0.7));
  }

  private void stopDumping() {
    this.ANRHandler.removeCallbacks(ANRAction);
    this.deadLockHandler.removeCallbacks(deadLockAction);
  }

  private boolean isDispatching(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(START_SIGNAL);
  }

  private boolean isFinished(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(END_SIGNAL);
  }

  private static boolean isBlock(long startNanos, long stopNanos, long interval) {
    long lengthMillis =
        TimeUnit.NANOSECONDS.toMillis(stopNanos) - TimeUnit.NANOSECONDS.toMillis(startNanos);
    return lengthMillis > interval;
  }

  void stopIfStarted() {
    HANDLER.sendMessageAtFrontOfQueue(HANDLER.obtainMessage(ANRSquirrel.STOP_DETECT, this));
  }

  void startIfStopped() {
    HANDLER.sendMessageAtFrontOfQueue(HANDLER.obtainMessage(ANRSquirrel.START_DETECT, this));
  }

  void removePendingMessage() {
    this.startNanos = this.stopNanos = 0;
    this.ANRHandler.removeCallbacksAndMessages(SquirrelPrinter.this);
    this.deadLockHandler.removeCallbacksAndMessages(SquirrelPrinter.this);

    Message stopANRMessage = Message.obtain(ANRHandler, stopANRAction);
    Message stopDeadLockMessage = Message.obtain(deadLockHandler, stopDeadLockAction);
    stopDeadLockMessage.obj = stopANRMessage.obj = SquirrelPrinter.this;
    this.ANRHandler.sendMessageAtFrontOfQueue(stopANRMessage);
    this.deadLockHandler.sendMessageAtFrontOfQueue(stopDeadLockMessage);
  }

  interface Callback extends Serializable {

    void onPreBlocking();

    void onBlocked(ANRError anrError, boolean isDeadLock);

    void onBlockCompleted();
  }
}
