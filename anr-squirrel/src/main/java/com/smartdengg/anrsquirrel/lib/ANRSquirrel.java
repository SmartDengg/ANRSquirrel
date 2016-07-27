package com.smartdengg.anrsquirrel.lib;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.smartdengg.anrsquirrel.lib.marble.SquirrelMarble;
import com.smartdengg.squirrel.ANRError;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel {

  private static final String TAG = ANRSquirrel.class.getSimpleName();
  private static final int DEFAULT_ANR_TIMEOUT = 10 * 1000;
  private static final int DEFAULT_SHOW_TIME = 1000;
  private static final String LISTENER = "LISTENER";
  private static final String ERROR = "ERROR";
  private Handler UiHandler = new Handler(Looper.getMainLooper());

  private AtomicBoolean isStarted = new AtomicBoolean(false);
  private SquirrelMarble squirrelMarble;

  private final SquirrelListener LISTENER_OF_SOULS = new SquirrelListener() {
    private static final long serialVersionUID = 7709345094663791741L;

    @Override public void onAppNotResponding(ANRError error) {
      if (!ignoreDebugger && Debug.isDebuggerConnected()) {
        this.onDebuggerConnected();
      } else {
        if (listener != null) listener.onAppNotResponding(error);
      }
    }

    @Override public void onInterrupted(InterruptedException exception) {
      if (listener != null) listener.onInterrupted(exception);
    }

    private void onDebuggerConnected() {
      Log.w(TAG,
          "An ANR was detected but ignored because the debugger is connected (you can prevent this with Builder.ignoreDebugger(true))");
    }
  };

  private static Handler HANDLER = new Handler(HandlerFactory.getErrorHandler().getLooper()) {
    @Override public void handleMessage(Message msg) {
      Bundle bundle = msg.getData();
      SquirrelListener squirrelListener = (SquirrelListener) bundle.get(LISTENER);
      ANRError anrError = (ANRError) bundle.get(ERROR);
      if (squirrelListener != null && anrError != null) {
        squirrelListener.onAppNotResponding(anrError);
      }
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
  private int interval;
  private boolean onlyMainThread;
  private boolean ignoreDebugger;
  private final SquirrelListener listener;
  private Point marblePoint;

  private ANRSquirrel(Context context, int interval, boolean ignoreDebugger, boolean onlyMainThread,
      Point point, SquirrelListener listener) {
    this.context = context;
    this.interval = interval;
    this.onlyMainThread = onlyMainThread;
    this.ignoreDebugger = ignoreDebugger;
    this.marblePoint = point;
    this.listener = listener;
  }

  public void show() {

    if (isStarted.get()) throw new IllegalStateException("ANRSquirrel detector already started.");
    isStarted.set(true);

    squirrelMarble = SquirrelMarble.initWith(context, marblePoint);
    squirrelMarble.show();

    Looper.getMainLooper()
        .setMessageLogging(
            new SquirrelPrinter(interval, onlyMainThread, new SquirrelPrinter.Callback() {
              private static final long serialVersionUID = -4595328905012011071L;

              @Override public void onPreBlocking() {
                UiHandler.removeCallbacks(updateRunnable);
                UiHandler.post(updateRunnable);
              }

              @Override public void onBlockOccur(ANRError anrError, boolean isDeadLock) {
                ANRSquirrel.this.sendWrapperMessageWithDelay(anrError,
                    isDeadLock ? 0 : (long) (interval * 0.5));
              }

              @Override public void onBlocked() {
                UiHandler.removeCallbacks(resetRunnable);
                UiHandler.postDelayed(resetRunnable, DEFAULT_SHOW_TIME);
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

    public Builder(Context context) {
      if (context == null) throw new IllegalArgumentException("Context must not be null.");
      this.context = context.getApplicationContext();
    }

    public Builder interval(int interval) {
      if (interval < 0) throw new IllegalArgumentException("Interval must not less than 0");
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
      if (point == null) {
        throw new IllegalArgumentException("Point must not be null.");
      }
      if (point.x < 0) throw new IllegalStateException("x must not be less than 0");
      if (point.y < 0) throw new IllegalStateException("x must not be less than 0");
      this.point = point;
      return Builder.this;
    }

    public Builder listener(SquirrelListener listener) {
      if (listener == null) {
        throw new IllegalArgumentException("SquirrelListener must not be null.");
      }
      if (this.listener != null) {
        throw new IllegalStateException("SquirrelListener already set.");
      }
      this.listener = listener;
      return Builder.this;
    }

    public ANRSquirrel build() {

      if (point == null) point = new Point(10, 10);

      return new ANRSquirrel(context, interval, ignoreDebugger, onlyMainThread, point, listener);
    }
  }
}
