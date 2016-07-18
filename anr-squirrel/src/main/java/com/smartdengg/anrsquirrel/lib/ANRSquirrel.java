package com.smartdengg.anrsquirrel.lib;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel extends Thread {

  private static final String TAG = ANRSquirrel.class.getSimpleName();
  public static final String START_SIGNAL = ">>>>>";
  public static final String END_SIGNAL = "<<<<<";

  private static final int DEFAULT_ANR_TIMEOUT = 5 * 1000;

  private final SquirrelListener LISTENER_OF_SOULS = new SquirrelListener() {
    @Override public void onAppNotResponding(ANRError error) {
      if (listener != null) listener.onAppNotResponding(error);
    }

    @Override public void onInterrupted(InterruptedException exception) {
      if (listener != null) listener.onInterrupted(exception);
    }
  };

  private final int interval;
  private final boolean shouldIgnoreDebugger;
  private final boolean onlyMainThread;
  private final SquirrelListener listener;
  private volatile long startNanos;
  private volatile long stopNanos;

  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private volatile int tick = 0;
  private final Runnable ticker = new Runnable() {
    @Override public void run() {
      tick = (tick + 1) % Integer.MAX_VALUE;
    }
  };

  private Runnable mRunnable = new Runnable() {
    @Override public void run() {

      StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
      for (StackTraceElement stackTraceElement : stackTrace)
        System.out.println("stackTraceElement = " + stackTraceElement);

      ANRError anrError;
      if (ANRSquirrel.this.onlyMainThread) {
        anrError = ANRError.onlyMainThread();
      } else {
        anrError = ANRError.allThread();
      }
      LISTENER_OF_SOULS.onAppNotResponding(anrError);
    }
  };

  ANRSquirrel(int interval, boolean shouldIgnoreDebugger, boolean onlyMainThread,
      SquirrelListener listener) {
    this.interval = interval;
    this.shouldIgnoreDebugger = shouldIgnoreDebugger;
    this.onlyMainThread = onlyMainThread;
    this.listener = listener;

    ANRSquirrel.this.initPrinter();

    //ANRSquirrel.this.start();
  }

  private void initPrinter() {
    Looper.getMainLooper().setMessageLogging(new Printer() {
      @Override public void println(String message) {

        if (isStart(message)) {
          ANRSquirrel.this.startNanos = System.nanoTime();
        } else if (isEnd(message)) {
          ANRSquirrel.this.stopNanos = System.nanoTime();

          if (!isBlock()) return;

          String stackTraceString = Log.getStackTraceString(new Throwable());
          System.out.println("stackTraceString = " + stackTraceString);

          /*ANRError anrError;
          if (onlyMainThread) {
            anrError = ANRError.onlyMainThread();
          } else {
            anrError = ANRError.allThread();
          }
          LISTENER_OF_SOULS.onAppNotResponding(anrError);*/
        }
      }
    });
  }

  @Override public void run() {
    this.setName("|ANR-Squirrel|");
    ANRSquirrel.this.startMonitor();
  }

  private void startMonitor() {
    int lastTick;
    int lastIgnored = -1;
    while (!isInterrupted()) {

      lastTick = tick;
      mainHandler.post(ticker);
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        LISTENER_OF_SOULS.onInterrupted(e);
        return;
      }

      // If the main thread has not handled ticker, it is blocked. ANR.
      if (tick == lastTick) {
        if (!shouldIgnoreDebugger && Debug.isDebuggerConnected()) {
          if (tick != lastIgnored) {
            Log.w("ANRWatchdog",
                "An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))");
          }
          lastIgnored = tick;
          continue;
        }

        ANRError anrError;
        if (onlyMainThread) {
          anrError = ANRError.onlyMainThread();
        } else {
          anrError = ANRError.allThread();
        }
        LISTENER_OF_SOULS.onAppNotResponding(anrError);
        return;
      }
    }
  }

  private boolean isBlock() {
    long lengthMillis =
        TimeUnit.NANOSECONDS.toMillis(stopNanos) - TimeUnit.NANOSECONDS.toMillis(startNanos);
    return !(lengthMillis <= interval || !shouldIgnoreDebugger && Debug.isDebuggerConnected());
  }

  @SuppressWarnings("UnusedDeclaration") public static class Builder {

    private int interval;
    private boolean shouldIgnoreDebugger;
    private boolean onlyMainThread;
    private SquirrelListener listener;

    public Builder() {
    }

    public Builder interval(int interval) {
      this.interval = interval;
      return Builder.this;
    }

    public Builder ignoreDebugger() {
      this.shouldIgnoreDebugger = true;
      return Builder.this;
    }

    public Builder onlyMainThread() {
      this.onlyMainThread = true;
      return Builder.this;
    }

    public Builder listener(@NotNull SquirrelListener listener) {
      this.listener = listener;
      return Builder.this;
    }

    public ANRSquirrel build() {

      if (interval < 0) throw new IllegalArgumentException("interval cannot less than 0");

      return new ANRSquirrel(interval != 0 ? interval : DEFAULT_ANR_TIMEOUT, shouldIgnoreDebugger,
          onlyMainThread, listener);
    }
  }

  private boolean isStart(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(START_SIGNAL);
  }

  private boolean isEnd(String message) {
    return !TextUtils.isEmpty(message) && message.startsWith(END_SIGNAL);
  }
}
