package com.smartdengg.anrsquirrel.lib;

/**
 * 创建时间:  16/7/20 下午4:34 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
class ANRWrapper extends Throwable {
  private static final long serialVersionUID = -5395891642242998310L;

  private final StackTraceElement[] threadStackTrace;

  ANRWrapper(String threadName, Throwable throwable, StackTraceElement[] stackTrace) {
    super(threadName, throwable);
    threadStackTrace = stackTrace;
    this.fillInStackTrace();
  }

  @Override public Throwable fillInStackTrace() {
    if (threadStackTrace != null) setStackTrace(threadStackTrace);
    return this;
  }
}
