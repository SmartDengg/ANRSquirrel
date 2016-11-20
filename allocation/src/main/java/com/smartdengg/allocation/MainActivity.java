package com.smartdengg.allocation;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    HandlerThread handlerThread1 = new HandlerThread("allocation#1");
    handlerThread1.start();

    HandlerThread handlerThread2 = new HandlerThread("allocation#2");
    handlerThread2.start();

    HandlerThread handlerThread3 = new HandlerThread("allocation#3");
    handlerThread3.start();

    Handler handler1 = new Handler(handlerThread1.getLooper());
    Handler handler2 = new Handler(handlerThread2.getLooper());
    Handler handler3 = new Handler(handlerThread3.getLooper());
  }
}
