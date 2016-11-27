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

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Printer;
import com.smartdengg.anrsquirrel.marble.SquirrelMarble;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel {

  private static final String TAG = ANRSquirrel.class.getSimpleName();
  private static final int DEFAULT_ANR_TIMEOUT = 10 * 1000;
  private static final String LISTENER = "LISTENER";
  private static final String ERROR = "ERROR";
  private static Handler THROW_HANDLER;
  static Handler HANDLER;

  static final int START_DETECT = 1;
  static final int STOP_DETECT = 2;
  static final int UPDATE_MARBLE = 3;
  static final int RESET_MARBLE = 4;

  private final boolean canAlertWindow;
  private boolean isStarted = false;
  private SquirrelMarble squirrelMarble;

  static {
    THROW_HANDLER = new Handler(HandlerFactory.getThrowHandler().getLooper()) {
      @Override public void handleMessage(Message msg) {

        Bundle bundle = msg.getData();
        SquirrelListener squirrelListener = (SquirrelListener) bundle.get(LISTENER);
        ANRError anrError = (ANRError) bundle.get(ERROR);
        if (squirrelListener != null && anrError != null) {
          squirrelListener.onAppNotResponding(anrError);
        }
      }
    };

    HANDLER = new Handler(Looper.getMainLooper()) {
      @Override public void handleMessage(Message msg) {

        switch (msg.what) {

          case START_DETECT: {
            SquirrelPrinter squirrelPrinter = (SquirrelPrinter) msg.obj;
            if (squirrelPrinter == null) break;

            squirrelPrinter.isStarted = true;
            Looper.getMainLooper().setMessageLogging(squirrelPrinter);
            break;
          }

          case STOP_DETECT: {
            SquirrelPrinter squirrelPrinter = (SquirrelPrinter) msg.obj;
            if (squirrelPrinter == null) break;

            squirrelPrinter.isStarted = false;
            squirrelPrinter.removePendingMessage();
            Looper.getMainLooper().setMessageLogging(null);
            break;
          }

          case UPDATE_MARBLE: {
            SquirrelMarble squirrelMarble = (SquirrelMarble) msg.obj;
            if (squirrelMarble != null) squirrelMarble.update();
            break;
          }

          case RESET_MARBLE: {
            SquirrelMarble squirrelMarble = (SquirrelMarble) msg.obj;
            if (squirrelMarble != null) squirrelMarble.reset();
            break;
          }

          default:
            throw new AssertionError("Unknown handler message received: " + msg.what);
        }
      }
    };
  }

  private final SquirrelListener LISTENER_OF_SOULS = new SquirrelListener() {
    private static final long serialVersionUID = 7709345094663791741L;

    @Override public void onAppNotResponding(ANRError error) {
      if (!ignoreDebugger && Debug.isDebuggerConnected()) {
        this.onDebuggerConnected();
      } else {
        if (listener != null) listener.onAppNotResponding(error);
      }
    }

    private void onDebuggerConnected() {
      Log.w(TAG,
          "An ANR was detected but ignored because the debugger is connected (you can prevent this with Builder#.ignoreDebugger(true))");
    }
  };

  final int interval;
  final boolean onlyMainThread;
  final List<Printer> printers;
  private final boolean ignoreDebugger;
  private final SquirrelListener listener;
  private final SquirrelPrinter squirrelPrinter;

  private ANRSquirrel(Context context, final int interval, boolean ignoreDebugger,
      boolean onlyMainThread, Point point, SquirrelListener listener, List<Printer> printers) {
    this.interval = interval;
    this.onlyMainThread = onlyMainThread;
    this.ignoreDebugger = ignoreDebugger;
    this.listener = listener;
    this.printers = printers;

    this.canAlertWindow = Utils.hasPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW);
    if (canAlertWindow) squirrelMarble = SquirrelMarble.initWith(context, point);

    this.squirrelPrinter = new SquirrelPrinter(ANRSquirrel.this, new SquirrelPrinter.Callback() {
      private static final long serialVersionUID = -4595328905012011071L;

      @Override public void onPreBlocking() {
        if (!canAlertWindow) return;
        HANDLER.removeMessages(UPDATE_MARBLE, squirrelMarble);
        HANDLER.sendMessage(HANDLER.obtainMessage(UPDATE_MARBLE));
      }

      @Override public void onBlocked(ANRError anrError, boolean isDeadLock) {
        ANRSquirrel.this.sendWrapperMessageWithDelay(anrError,
            isDeadLock ? 0 : (long) (interval * 0.3));
      }

      @Override public void onBlockCompleted() {
        if (!canAlertWindow) return;
        HANDLER.removeMessages(RESET_MARBLE);
        long delayMillis = (long) (interval * 0.2);
        HANDLER.sendMessageDelayed(HANDLER.obtainMessage(RESET_MARBLE, squirrelMarble),
            delayMillis);
      }
    });
  }

  public synchronized void start() {
    if (isStarted) {
      Log.v(TAG, "ANRSquirrel detector already started");
      return;
    }

    this.isStarted = true;
    this.squirrelPrinter.startIfStopped();
    if (squirrelMarble != null && canAlertWindow) squirrelMarble.show();
  }

  public synchronized void stop() {
    if (!isStarted) {
      Log.v(TAG, "ANRSquirrel detector already stopped");
      return;
    }

    this.isStarted = false;
    this.squirrelPrinter.stopIfStarted();
    if (squirrelMarble != null && canAlertWindow) squirrelMarble.hide();
  }

  private void sendWrapperMessageWithDelay(ANRError error, long delayMillis) {
    Message message = THROW_HANDLER.obtainMessage();
    Bundle bundle = new Bundle();
    bundle.putSerializable(LISTENER, LISTENER_OF_SOULS);
    bundle.putSerializable(ERROR, error);
    message.setData(bundle);
    THROW_HANDLER.sendMessageDelayed(message, delayMillis);
  }

  public static ANRSquirrel initializeWithDefaults(final Context context) {
    if (context == null) throw new IllegalArgumentException("Context must not be null.");
    return new Builder(context).build();
  }

  @SuppressWarnings("UnusedDeclaration") public static class Builder {

    private Context context;
    private int interval = DEFAULT_ANR_TIMEOUT;
    private boolean ignoreDebugger;
    private boolean onlyMainThread;
    private Point point;
    private SquirrelListener listener;
    private List<Printer> printers;

    public Builder(Context context) {
      if (context == null) throw new IllegalStateException("Context must not be null.");
      this.context = (context instanceof Application) ? context : context.getApplicationContext();
    }

    public Builder interval(int interval) {
      if (interval < 0) throw new IllegalArgumentException("Interval must not less than 0");
      if (interval < 1000) interval = 1000;
      this.interval = interval;
      return Builder.this;
    }

    public Builder ignoreDebugger(boolean ignoreDebugger) {
      this.ignoreDebugger = ignoreDebugger;
      return Builder.this;
    }

    public Builder onlyMainThread() {
      this.onlyMainThread = true;
      return Builder.this;
    }

    public Builder anchor(Point point) {
      if (point == null) throw new NullPointerException("Point == null");
      if (point.x < 0) throw new IllegalArgumentException("x must not be less than 0");
      if (point.y < 0) throw new IllegalArgumentException("y must not be less than 0");

      this.point = point;
      return Builder.this;
    }

    public Builder listener(SquirrelListener listener) {
      if (listener == null) throw new NullPointerException("SquirrelListener == null.");
      if (this.listener != null) throw new IllegalStateException("SquirrelListener already set.");

      this.listener = listener;
      return Builder.this;
    }

    public Builder addPrinter(Printer printer) {
      if (printer == null) throw new NullPointerException("Printer == null.");

      if (this.printers == null) this.printers = new LinkedList<>();
      this.printers.add(printer);

      return Builder.this;
    }

    public ANRSquirrel build() {

      if (point == null) point = new Point(10, 10);

      return new ANRSquirrel(context, interval, ignoreDebugger, onlyMainThread, point, listener,
          printers);
    }
  }
}
