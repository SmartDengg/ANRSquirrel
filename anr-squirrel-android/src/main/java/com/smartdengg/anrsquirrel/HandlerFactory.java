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

  public static Handler getThrowHandler() {

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
