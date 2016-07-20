package com.smartdengg.anrsquirrel.lib;

import android.os.Looper;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({ "Convert2Diamond", "UnusedDeclaration" }) public class ANRError extends Error {

  private static final long serialVersionUID = -7971806470616989034L;

  private static final Thread mainThread = Looper.getMainLooper().getThread();

  private ANRError(ANRThrowable throwable) {
    super(Thread.currentThread().getName() + "Application Not Responding", throwable);
  }

  @Override public Throwable fillInStackTrace() {
    setStackTrace(new StackTraceElement[] {});
    return this;
  }

  static ANRError allThread() {

    ANRThrowable throwable = null;

    ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
    ThreadGroup parentGroup;
    while ((parentGroup = rootGroup.getParent()) != null) rootGroup = parentGroup;

    Thread[] threads = new Thread[rootGroup.activeCount()];
    while (rootGroup.enumerate(threads, true) == threads.length) {
      threads = new Thread[(int) (threads.length * 1.5)];
    }

    List<Thread> threadList = Arrays.asList(threads);
    if (!threadList.contains(mainThread)) {
      threads = insertElement(threads, mainThread, 0);
    } else {
      int i = threadList.indexOf(mainThread);
      threads[i] = Looper.getMainLooper().getThread();
    }

    for (Thread thread : threads) {
      if (thread == null) continue;

      boolean logThreadStackTrace =
          (mainThread.getId() == thread.getId() || thread.getStackTrace().length > 0);

      if (!logThreadStackTrace) continue;

      String name = getThreadTitle(thread);
      StackTraceElement[] stackTraceElements = thread.getStackTrace();

      if (thread.getId() == mainThread.getId()) {
        throwable = new ANRThrowable(name, throwable, mainThread.getStackTrace());
      } else {
        throwable = new ANRThrowable(name, throwable, stackTraceElements);
      }
    }

    return new ANRError(throwable);
  }

  /**
   * @hide
   */
 /* ANRError New(String prefix, boolean logThreadsWithoutStackTrace) {

    final Thread mainThread = Looper.getMainLooper().getThread();
    _$1._$2 throwable = null;

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
      throwable = new _$1(name, stackTraceElements).new _$2(throwable);
    }

    return new ANRError(throwable);
  }*/
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

    return new ANRError(new ANRThrowable(getThreadTitle(mainThread), null, mainStackTrace));
  }

  private static String getThreadTitle(Thread thread) {
    return thread.getName() + " [state = " + thread.getState() + "]";
  }
}