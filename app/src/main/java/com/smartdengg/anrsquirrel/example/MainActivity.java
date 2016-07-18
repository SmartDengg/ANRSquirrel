package com.smartdengg.anrsquirrel.example;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressWarnings("all") public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(MainActivity.this);
    ButterKnife.setDebug(true);

    findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.e(TAG, "Begin waiting!");
        SystemClock.sleep(2 * 1000);
      }
    });
  }

  @OnClick(R.id.btn) protected void onClick() {
    //Log.e(TAG, "Begin waiting!");
    //SystemClock.sleep(2 * 1000);
  }
}
