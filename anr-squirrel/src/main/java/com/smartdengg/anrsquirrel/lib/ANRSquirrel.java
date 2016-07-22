package com.smartdengg.anrsquirrel.lib;

import android.os.Looper;
import android.util.Printer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  private class ParentPrinter implements Printer {

    private final List<Printer> mPrinters;

    public ParentPrinter(List<Printer> printers) {
      mPrinters = printers;
    }

    @Override public void println(String x) {
      for (Printer printer : mPrinters)
        printer.println(x);
    }
  }

  ANRSquirrel(int interval, boolean ignoreDebugger, boolean debuggable, boolean onlyMainThread,
      List<Printer> printerList, SquirrelListener listener) {
    this.listener = listener;

    if (debuggable) {

      int builtInPrinters = 1;
      int printerCount = (printerList != null ? printerList.size() : 0);
      List<Printer> allPrinters = new ArrayList<>(builtInPrinters + printerCount);

      if (printerList != null) allPrinters.addAll(printerList);
      allPrinters.add(new SquirrelPrinter(interval, ignoreDebugger, onlyMainThread,
          new SquirrelPrinter.Callback() {
            @Override public void onBlockOccur(ANRError anrError) {
              LISTENER_OF_SOULS.onAppNotResponding(anrError);
            }
          }));

      Looper.getMainLooper()
          .setMessageLogging(new ParentPrinter(Collections.unmodifiableList(allPrinters)));
    }
  }

  @SuppressWarnings("UnusedDeclaration") public static class Builder {

    private int interval;
    private boolean shouldIgnoreDebugger;
    private boolean debuggable;
    private boolean onlyMainThread;
    private SquirrelListener listener;
    private List<Printer> mPrinterList;

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

    public Builder injectPrinter(Printer printer) {

      if (printer == null) throw new NullPointerException("printer == null");

      if (mPrinterList == null) mPrinterList = new ArrayList<>();

      if (mPrinterList.contains(printer)) {
        throw new IllegalStateException("Printer already injected.");
      }

      this.mPrinterList.add(printer);

      return Builder.this;
    }

    public ANRSquirrel build() {

      if (interval < 0) throw new IllegalArgumentException("interval cannot less than 0");

      return new ANRSquirrel(interval != 0 ? interval : DEFAULT_ANR_TIMEOUT, shouldIgnoreDebugger,
          debuggable, onlyMainThread, mPrinterList, listener);
    }
  }
}
