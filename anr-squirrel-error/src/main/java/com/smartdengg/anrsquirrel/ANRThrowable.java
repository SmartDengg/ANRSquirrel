package com.smartdengg.anrsquirrel;

class ANRThrowable extends Throwable {
  private static final long serialVersionUID = -5395891642242998310L;

  private final StackTraceElement[] threadStackTrace;

  ANRThrowable(String threadName, Throwable throwable, StackTraceElement[] stackTrace) {
    super(threadName, throwable);
    threadStackTrace = stackTrace;
    this.fillInStackTrace();
  }

  @Override public Throwable fillInStackTrace() {
    if (threadStackTrace != null) setStackTrace(threadStackTrace);
    return this;
  }
}
