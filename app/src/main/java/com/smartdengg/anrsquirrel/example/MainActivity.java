package com.smartdengg.anrsquirrel.example;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("all") public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private final Object mutexSync = new Object();
  private MyApplication myApplication;

  private static void sleepAMinute() {
    SystemClock.sleep(6 * 1000);
  }

  private void deadLock() {
    new LockerThread().start();

    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        synchronized (mutexSync) {
          Log.e("ANR-Failed", "There should be a dead lock before this message");
        }
      }
    }, 1000);
  }

  public class LockerThread extends Thread {

    public LockerThread() {
      setName("APP: Locker");
    }

    @Override public void run() {
      synchronized (mutexSync) {
        while (true) sleepAMinute();
      }
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(MainActivity.this);
    ButterKnife.setDebug(true);
    this.myApplication = (MyApplication) getApplication();
  }

  @OnClick(R.id.btn_sleep) protected void onNormalBlockClick() {
    Log.e(TAG, "Begin waiting!");
    this.sleepAMinute();
  }

  @OnClick(R.id.btn_deadlock) protected void onDeadBlockClick() {
    Log.e(TAG, "Begin waiting!");
    this.deadLock();
  }

  @OnClick(R.id.btn_start) protected void onStopClick() {
    Log.e(TAG, "Start detection!");
    this.myApplication.getANRSquirrel().start();
  }

  @OnClick(R.id.btn_stop) protected void onStartClick() {
    Log.e(TAG, "Stop detection!");
    this.myApplication.getANRSquirrel().stop();
  }
}
