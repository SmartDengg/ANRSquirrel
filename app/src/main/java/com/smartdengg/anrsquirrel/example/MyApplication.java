package com.smartdengg.anrsquirrel.example;

import android.app.Application;
import com.smartdengg.anrsquirrel.lib.ANRSquirrel;
import com.smartdengg.anrsquirrel.lib.SquirrelListener;
import com.smartdengg.squirrel.ANRError;

/**
 * Created by SmartDengg on 2016/7/18.
 */
public class MyApplication extends Application {

  private SquirrelListener listener = new SquirrelListener() {
    @Override public void onAppNotResponding(ANRError error) {
      throw error;
      //error.printStackTrace();
    }

    @Override public void onInterrupted(InterruptedException exception) {
      exception.printStackTrace();
    }
  };

  @Override public void onCreate() {
    super.onCreate();

    ANRSquirrel anrSquirrel =
        new ANRSquirrel.Builder(MyApplication.this).interval(1000).listener(listener).build();
    /*Start detector*/
    anrSquirrel.start();
  }
}
