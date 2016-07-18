package com.smartdengg.anrsquirrel.lib;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.jetbrains.annotations.NotNull;

/**
 * Created by SmartDengg on 2016/7/17.
 */
@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel extends Thread {

  private static final int DEFAULT_ANR_TIMEOUT = 8 * 1000;

  private final Listener LISTENER_OF_SOULS = new Listener() {
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
  private final Listener listener;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private volatile int tick = 0;
  private final Runnable ticker = new Runnable() {
    @Override public void run() {
      tick = (tick + 1) % Integer.MAX_VALUE;
    }
  };

  ANRSquirrel(int interval, boolean shouldIgnoreDebugger, boolean onlyMainThread,
      Listener listener) {
    this.interval = interval;
    this.shouldIgnoreDebugger = shouldIgnoreDebugger;
    this.onlyMainThread = onlyMainThread;
    this.listener = listener;

    ANRSquirrel.this.start();
  }

  @Override public void run() {
    this.setName("|ANR-Squirrel|");

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

  @SuppressWarnings("UnusedDeclaration") public static class Builder {

    private int interval;
    private boolean shouldIgnoreDebugger;
    private boolean onlyMainThread;
    private Listener listener;

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

    public Builder listener(@NotNull Listener listener) {
      this.listener = listener;
      return Builder.this;
    }

    public ANRSquirrel build() {

      if (interval < 0) throw new IllegalArgumentException("interval cannot less than 0");

      return new ANRSquirrel(interval != 0 ? interval : DEFAULT_ANR_TIMEOUT, shouldIgnoreDebugger,
          onlyMainThread, listener);
    }
  }

  public interface Listener {
    void onAppNotResponding(ANRError error);

    void onInterrupted(InterruptedException exception);
  }
}
