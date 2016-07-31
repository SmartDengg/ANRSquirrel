package com.smartdengg.anrsquirrel;

import android.content.Context;
import android.graphics.Point;

public class ANRSquirrel {

  /** stub **/
  private ANRSquirrel(Context context, int interval, boolean ignoreDebugger, boolean onlyMainThread,
      Point point, SquirrelListener listener) {
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
    public ANRSquirrel build() {
      return new ANRSquirrel(null, 0, false, false, null, null);
    }
  }
}
