package com.smartdengg.anrsquirrel.lib;

import android.os.Looper;
import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings({ "Convert2Diamond", "UnusedDeclaration" }) public class ANRError extends Error {

  private static final long serialVersionUID = -7971806470616989034L;

  private static class Inner implements Serializable {
    private static final long serialVersionUID = -1669800781514807253L;
    private final String threadName;
    private final StackTraceElement[] threadStackTrace;

    private Inner(String name, StackTraceElement[] stackTrace) {
      threadName = name;
      threadStackTrace = stackTrace;
    }

    private class ThreadThrowable extends Throwable {
      private static final long serialVersionUID = -2092890922495326064L;

      private ThreadThrowable(ThreadThrowable throwable) {
        super(threadName, throwable);
      }

      @Override public Throwable fillInStackTrace() {
        setStackTrace(threadStackTrace);
        return this;
      }
    }
  }

  // TODO: 2016/7/18 转换首字母大写
  private ANRError(Inner.ThreadThrowable throwable) {
    super(Thread.currentThread().getName() + "Application Not Responding", throwable);
  }

  @Override public Throwable fillInStackTrace() {
    setStackTrace(new StackTraceElement[] {});
    return this;
  }

  static ANRError allThread() {

    final Thread mainThread = Looper.getMainLooper().getThread();
    Inner.ThreadThrowable throwable = null;

    ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
    ThreadGroup parentGroup;
    while ((parentGroup = rootGroup.getParent()) != null) rootGroup = parentGroup;

    Thread[] threads = new Thread[rootGroup.activeCount()];
    while (rootGroup.enumerate(threads, true) == threads.length) {
      threads = new Thread[(int) (threads.length * 1.5)];
    }

    if (!Arrays.asList(threads).contains(mainThread)) {
      threads = insertElement(threads, mainThread, 0);
    }

    for (Thread thread : threads) {
      if (thread == null) continue;

      boolean logThreadStackTrace =
          (mainThread.getId() == thread.getId() || thread.getStackTrace().length > 0);

      if (!logThreadStackTrace) continue;

      String name = getThreadTitle(thread);
      StackTraceElement[] stackTraceElements = thread.getStackTrace();

      if (thread.getName().equals(ANRSquirrel.currentThread().getName())) {
        throwable = new Inner(name, new StackTraceElement[] {}).new ThreadThrowable(throwable);
      } else {
        throwable = new Inner(name, stackTraceElements).new ThreadThrowable(throwable);
      }
    }

    return new ANRError(throwable);
  }

  /**
   * @hide
   */
  ANRError New(String prefix, boolean logThreadsWithoutStackTrace) {

    final Thread mainThread = Looper.getMainLooper().getThread();
    Inner.ThreadThrowable throwable = null;

    ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
    ThreadGroup parentGroup;
    while ((parentGroup = rootGroup.getParent()) != null) rootGroup = parentGroup;

    Thread[] threads = new Thread[rootGroup.activeCount()];
    while (rootGroup.enumerate(threads, true) == threads.length) {
      threads = new Thread[(int) (threads.length * 1.5)];
    }

    if (!Arrays.asList(threads).contains(mainThread)) {
      threads = insertElement(threads, mainThread, 0);
    }

    for (Thread thread : threads) {
      if (thread == null) continue;

      boolean logThreadStackTrace =
          mainThread.getId() == thread.getId() || (thread.getName().startsWith(prefix) && (
              logThreadsWithoutStackTrace
                  || thread.getStackTrace().length > 0));

      if (!logThreadStackTrace) continue;

      String name = getThreadTitle(thread);
      StackTraceElement[] stackTraceElements = thread.getStackTrace();
      throwable = new Inner(name, stackTraceElements).new ThreadThrowable(throwable);
    }

    return new ANRError(throwable);
  }

  private static Thread[] insertElement(Thread original[], Thread element, int index) {
    int length = original.length;
    Thread destination[] = new Thread[length + 1];
    System.arraycopy(original, 0, destination, 0, index);
    destination[index] = element;
    System.arraycopy(original, index, destination, index + 1, length - index);
    return destination;
  }

  static ANRError onlyMainThread() {
    final Thread mainThread = Looper.getMainLooper().getThread();
    final StackTraceElement[] mainStackTrace = mainThread.getStackTrace();

    return new ANRError(
        new Inner(getThreadTitle(mainThread), mainStackTrace).new ThreadThrowable(null));
  }

  private static String getThreadTitle(Thread thread) {
    return thread.getName() + " (state = " + thread.getState() + ")";
  }
}