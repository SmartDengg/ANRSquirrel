package com.smartdengg.anrsquirrel.example;

import android.app.Application;
import com.smartdengg.anrsquirrel.lib.ANRError;
import com.smartdengg.anrsquirrel.lib.ANRSquirrel;

/**
 * Created by SmartDengg on 2016/7/18.
 */
public class MyApplication extends Application {

  private ANRSquirrel.Listener listener = new ANRSquirrel.Listener() {
    @Override public void onAppNotResponding(ANRError error) {
      throw error;
    }

    @Override public void onInterrupted(InterruptedException exception) {
      exception.printStackTrace();
    }
  };

  @Override public void onCreate() {
    super.onCreate();
    new ANRSquirrel.Builder().interval(3 * 1000).listener(listener).ignoreDebugger().build();
  }
}
