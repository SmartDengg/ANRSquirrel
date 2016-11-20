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
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
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
  private static HandlerThreadWrapper throwHandler;

  private static final int THREAD_LEAK_CLEANING_MS = 1000;

  private HandlerFactory() {
    throw new IllegalStateException("No instance!");
  }

  static Handler getANRHandler() {

    if (ANRHandler == null) {
      synchronized (HandlerFactory.class) {
        if (ANRHandler == null) {
          ANRHandler = new HandlerThreadWrapper("BlockedHandler", Process.THREAD_PRIORITY_DEFAULT);
        }
      }
    }

    return ANRHandler.handler();
  }

  static Handler getCheckLockHandler() {

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

  static Handler getThrowHandler() {

    if (throwHandler == null) {
      synchronized (HandlerFactory.class) {
        if (throwHandler == null) {
          throwHandler = new HandlerThreadWrapper("ANRError", -20);
        }
      }
    }

    return throwHandler.handler();
  }

  private static class HandlerThreadWrapper {
    private Handler handler = null;

    HandlerThreadWrapper(String suffix, int priority) {
      HandlerThread handlerThread =
          new HandlerThread(THREAD_PREFIX + (TextUtils.isEmpty(suffix) ? "" : "_" + suffix + "  "),
              priority);
      handlerThread.start();
      //flushStackLocalLeaks(handlerThread.getLooper());

      this.handler = new Handler(handlerThread.getLooper());
    }

    Handler handler() {
      this.handler.removeCallbacksAndMessages(null);
      return handler;
    }
  }

  /**
   * Prior to Android 5, HandlerThread always keeps a stack local reference to the last message
   * that was sent to it. This method makes sure that stack local reference never stays there
   * for too long by sending new messages to it every second.
   */
  private static void flushStackLocalLeaks(Looper looper) {
    final Handler handler = new Handler(looper);
    handler.post(new Runnable() {
      @Override public void run() {
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
          @Override public boolean queueIdle() {
            handler.sendMessageDelayed(handler.obtainMessage(), THREAD_LEAK_CLEANING_MS);
            return true;
          }
        });
      }
    });
  }
}
