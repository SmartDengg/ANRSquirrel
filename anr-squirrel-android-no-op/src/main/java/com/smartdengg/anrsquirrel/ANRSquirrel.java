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

import android.content.Context;
import android.graphics.Point;
import android.util.Printer;
import java.util.List;

public class ANRSquirrel {

  /** stub **/
  private ANRSquirrel(Context context, final int interval, boolean ignoreDebugger,
      boolean onlyMainThread, Point point, SquirrelListener listener, List<Printer> printers) {
  }

  /** stub **/
  public void show() {
  }

  public static ANRSquirrel initializeWithDefaults(final Context context) {
    return new Builder(context).build();
  }

  public static class Builder {

    /** stub **/
    public Builder(Context context) {
    }

    /** stub **/
    public Builder interval(int interval) {
      return Builder.this;
    }

    /** stub **/
    public Builder ignoreDebugger(boolean ignoreDebugger) {
      return Builder.this;
    }

    /** stub **/
    public Builder onlyMainThread() {
      return Builder.this;
    }

    /** stub **/
    public Builder anchor(Point point) {
      return Builder.this;
    }

    /** stub **/
    public Builder listener(SquirrelListener listener) {
      return Builder.this;
    }

    /** stub **/
    public Builder addPrinter(Printer printer) {
      return Builder.this;
    }

    /** stub **/
    public ANRSquirrel build() {
      return new ANRSquirrel(null, 0, false, false, null, null, null);
    }
  }
}
