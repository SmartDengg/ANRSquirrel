/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.anrsquirrel;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({ "Convert2Diamond", "UnusedDeclaration" }) public class ANRError extends Error {

  private static final long serialVersionUID = -7971806470616989034L;

  private ANRError(ANRThrowable throwable) {
    super(Thread.currentThread().getName() + "Application Not Responding", throwable);
  }

  @Override public Throwable fillInStackTrace() {
    setStackTrace(new StackTraceElement[] {});
    return this;
  }

  static ANRError dumpStackTrace(Thread mainThread, boolean onlyMainThread, String threadPrefix) {

    if (onlyMainThread) {
      return new ANRError(
          new ANRThrowable(generatorThreadTitle(mainThread), null, mainThread.getStackTrace()));
    }

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
      threads[i] = mainThread;
    }

    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < threads.length; i++) {
      Thread thread = threads[i];
      if (thread == null) continue;
      if (thread.getName().startsWith(threadPrefix)) continue;

      boolean logThreadStackTrace =
          (mainThread.getId() == thread.getId() || thread.getStackTrace().length > 0);
      if (!logThreadStackTrace) continue;

      String name = generatorThreadTitle(thread);
      StackTraceElement[] stackTraceElements = thread.getStackTrace();

      if (thread.getId() == mainThread.getId()) {
        throwable = new ANRThrowable(name, throwable, mainThread.getStackTrace());
      } else {
        throwable = new ANRThrowable(name, throwable, stackTraceElements);
      }
    }

    return new ANRError(throwable);
  }

  private static Thread[] insertElement(Thread original[], Thread element, int index) {
    int length = original.length;
    Thread[] destination = new Thread[length + 1];
    System.arraycopy(original, 0, destination, 0, index);
    destination[index] = element;
    System.arraycopy(original, index, destination, index + 1, length - index);
    return destination;
  }

  private static String generatorThreadTitle(Thread thread) {
    return thread.getName() + " [state = " + thread.getState() + "]";
  }
}