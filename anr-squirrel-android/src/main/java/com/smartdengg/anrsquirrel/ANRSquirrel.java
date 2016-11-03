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
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel {

  private static final String TAG = ANRSquirrel.class.getSimpleName();
  private static final int DEFAULT_ANR_TIMEOUT = 10 * 1000;
  private static final String LISTENER = "LISTENER";
  private static final String ERROR = "ERROR";
  private static Handler HANDLER;
  private Handler UiHandler = new Handler(Looper.getMainLooper());

  private AtomicBoolean isStarted = new AtomicBoolean(false);
  private SquirrelMarble squirrelMarble;

  static {
    HANDLER = new Handler(HandlerFactory.getThrowHandler().getLooper()) {
      @Override public void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        SquirrelListener squirrelListener = (SquirrelListener) bundle.get(LISTENER);
        ANRError anrError = (ANRError) bundle.get(ERROR);
        if (squirrelListener != null && anrError != null) {
          squirrelListener.onAppNotResponding(anrError);
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

  private Runnable updateRunnable = new Runnable() {
    @Override public void run() {
      if (squirrelMarble != null) squirrelMarble.update();
    }
  };

  private Runnable resetRunnable = new Runnable() {
    @Override public void run() {
      if (squirrelMarble != null) squirrelMarble.reset();
    }
  };

  private final Context context;
  int interval;
  boolean onlyMainThread;
  boolean ignoreDebugger;
  final SquirrelListener listener;
  Point marblePoint;
  List<Printer> printers;

  private ANRSquirrel(Context context, int interval, boolean ignoreDebugger, boolean onlyMainThread,
      Point point, SquirrelListener listener, List<Printer> printers) {
    this.context = context;
    this.interval = interval;
    this.onlyMainThread = onlyMainThread;
    this.ignoreDebugger = ignoreDebugger;
    this.marblePoint = point;
    this.listener = listener;
    this.printers = printers;
  }

  public void show() {

    if (isStarted.get()) throw new IllegalStateException("ANRSquirrel detector already started");
    isStarted.set(true);

    squirrelMarble = SquirrelMarble.initWith(context, marblePoint);
    squirrelMarble.show();

    Looper.getMainLooper()
        .setMessageLogging(new SquirrelPrinter(ANRSquirrel.this, new SquirrelPrinter.Callback() {
          private static final long serialVersionUID = -4595328905012011071L;

          @Override public void onPreBlocking() {
            UiHandler.removeCallbacks(updateRunnable);
            UiHandler.post(updateRunnable);
          }

          @Override public void onBlocked(ANRError anrError, boolean isDeadLock) {
            ANRSquirrel.this.sendWrapperMessageWithDelay(anrError,
                isDeadLock ? 0 : (long) (interval * 0.3));
          }

          @Override public void onBlockCompleted() {
            UiHandler.removeCallbacks(resetRunnable);
            UiHandler.postDelayed(resetRunnable, (long) (interval * 0.2));
          }
        }));
  }

  private void sendWrapperMessageWithDelay(ANRError error, long delayMillis) {
    Message message = HANDLER.obtainMessage();
    Bundle bundle = new Bundle();
    bundle.putSerializable(LISTENER, LISTENER_OF_SOULS);
    bundle.putSerializable(ERROR, error);
    message.setData(bundle);
    HANDLER.sendMessageDelayed(message, delayMillis);
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
      if (context == null) throw new IllegalArgumentException("Context must not be null.");
      this.context = context.getApplicationContext();
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
