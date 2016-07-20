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

  /* private final int interval;
   private final boolean shouldIgnoreDebugger;
   private final boolean onlyMainThread;*/
  private final SquirrelListener listener;

  ANRSquirrel(int interval, boolean shouldIgnoreDebugger, boolean onlyMainThread,
      SquirrelListener listener) {
  /*  this.interval = interval;
    this.shouldIgnoreDebugger = shouldIgnoreDebugger;
    this.onlyMainThread = onlyMainThread;*/
    this.listener = listener;

    Looper.getMainLooper()
        .setMessageLogging(
            new SquirrelPrinter(interval, shouldIgnoreDebugger, new SquirrelPrinter.Callback() {
              @Override public void onBlockOccur(ANRError anrError) {
                LISTENER_OF_SOULS.onAppNotResponding(anrError);
              }
            }));

    //ANRSquirrel.this.start();
  }

  /*@Override public Throwable fillInStackTrace() {
    setStackTrace(new StackTraceElement[] {});
    return ANRSquirrel.this;
  }*/

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
}
