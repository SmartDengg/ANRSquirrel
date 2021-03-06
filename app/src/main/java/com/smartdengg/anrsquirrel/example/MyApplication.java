package com.smartdengg.anrsquirrel.example;

import android.app.Application;
import android.graphics.Point;
import android.os.Looper;
import android.util.Printer;
import com.smartdengg.anrsquirrel.ANRError;
import com.smartdengg.anrsquirrel.ANRSquirrel;
import com.smartdengg.anrsquirrel.SquirrelListener;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by SmartDengg on 2016/7/18.
 */
public class MyApplication extends Application {

  private ANRSquirrel anrSquirrel;

  private SquirrelListener listener = new SquirrelListener() {
    private static final long serialVersionUID = -8753541650745626066L;

    @Override public void onAppNotResponding(ANRError error) {
      //rethrow error ?;
      error.printStackTrace();
    }
  };

  @Override public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);

    anrSquirrel = new ANRSquirrel.Builder(MyApplication.this).interval(2 * 1000)
        .anchor(new Point(100, 100))
        .listener(listener)
        .ignoreDebugger(true)
        .build();

    Looper mainLooper = getMainLooper();
    mainLooper.setMessageLogging(new Printer() {
      @Override public void println(String x) {
        //System.err.println(x);
      }
    });
  }

  public ANRSquirrel getANRSquirrel() {
    return anrSquirrel;
  }
}
