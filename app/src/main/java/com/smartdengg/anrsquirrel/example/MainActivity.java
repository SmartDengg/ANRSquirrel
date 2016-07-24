package com.smartdengg.anrsquirrel.example;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("all") public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private final Object mutex = new Object();

  private static void sleepAMinute() {
    try {
      Thread.sleep(2 * 1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void deadLock() {
    new LockerThread().start();

    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        synchronized (mutex) {
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
      synchronized (mutex) {
        //noinspection InfiniteLoopStatement
        while (true) sleepAMinute();
      }
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(MainActivity.this);
    ButterKnife.setDebug(true);
  }

  @OnClick(R.id.btn) protected void onClick() {
    Log.e(TAG, "Begin waiting!");
    sleepAMinute();
  }

  @OnClick(R.id.btn1) protected void onClick1() {
    Log.e(TAG, "Begin waiting!");
    this.deadLock();
  }
}
