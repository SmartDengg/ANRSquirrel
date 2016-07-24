package com.smartdengg.anrsquirrel.lib;

import android.content.Context;
import android.os.Debug;
import android.os.Looper;
import android.util.Log;
import com.smartdengg.squirrel.ANRError;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel {

  private static final String TAG = ANRSquirrel.class.getSimpleName();
  private static final int DEFAULT_ANR_TIMEOUT = 10 * 1000;
  private AtomicBoolean isStarted = new AtomicBoolean(false);

  private WeakReference<SquirrelPrinter> printerWeakReference;

  private final SquirrelListener LISTENER_OF_SOULS = new SquirrelListener() {
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

  private final Context context;
  private int interval;
  private boolean onlyMainThread;
  private boolean ignoreDebugger;
  private final SquirrelListener listener;

  private ANRSquirrel(Context context, int interval, boolean ignoreDebugger, boolean onlyMainThread,
      SquirrelListener listener) {
    this.context = context;
    this.interval = interval;
    this.onlyMainThread = onlyMainThread;
    this.ignoreDebugger = ignoreDebugger;
    this.listener = listener;
  }

  public void start() {

    if (isStarted.get()) throw new IllegalStateException("ANRSquirrel detector already started.");
    isStarted.set(true);

    Looper.getMainLooper()
        .setMessageLogging(
            new SquirrelPrinter(interval, onlyMainThread, new SquirrelPrinter.Callback() {
              @Override public void onBlocked(ANRError anrError) {
                LISTENER_OF_SOULS.onAppNotResponding(anrError);
              }
            }));
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
      return new ANRSquirrel(context, interval, ignoreDebugger, onlyMainThread, listener);
    }
  }
}
