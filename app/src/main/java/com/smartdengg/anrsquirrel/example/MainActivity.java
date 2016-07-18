package com.smartdengg.anrsquirrel.example;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(MainActivity.this);
    ButterKnife.setDebug(true);
  }

  @OnClick(R.id.btn) protected void onClick() {
    Log.e(TAG, "Begin waiting!");
    SystemClock.sleep(20 * 1000);
  }
}
