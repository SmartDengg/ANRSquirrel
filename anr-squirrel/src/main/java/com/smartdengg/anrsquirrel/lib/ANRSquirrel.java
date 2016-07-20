package com.smartdengg.anrsquirrel.lib;

import android.os.Looper;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedDeclaration") public class ANRSquirrel {

  private static final String TAG = ANRSquirrel.class.getSimpleName();

  private static final int DEFAULT_ANR_TIMEOUT = 5 * 1000;

  private final SquirrelListener LISTENER_OF_SOULS = new SquirrelListener() {
    @Override public void onAppNotResponding(ANRError error) {
      if (listener != null) listener.onAppNotResponding(error);
    }

    @Override public void onInterrupted(InterruptedException exception) {
      if (listener != null) listener.onInterrupted(exception);
    }
  };

  private final SquirrelListener listener;

  ANRSquirrel(int interval, boolean ignoreDebugger, boolean debuggable, boolean onlyMainThread,
      SquirrelListener listener) {
    this.listener = listener;

    if (debuggable) {
      Looper.getMainLooper()
          .setMessageLogging(new SquirrelPrinter(interval, ignoreDebugger, onlyMainThread,
              new SquirrelPrinter.Callback() {
                @Override public void onBlockOccur(ANRError anrError) {
                  LISTENER_OF_SOULS.onAppNotResponding(anrError);
                }
              }));
    }
  }

  @SuppressWarnings("UnusedDeclaration") public static class Builder {

    private int interval;
    private boolean shouldIgnoreDebugger;
    private boolean debuggable;
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

    public Builder isDebuggable(boolean debuggable) {
      this.debuggable = debuggable;
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
          debuggable, onlyMainThread, listener);
    }
  }
}
