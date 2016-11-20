package com.smartdengg.allocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static List<Activity> list = new ArrayList<>();

  Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      System.out.printf(msg.toString());
    }
  };

  @SuppressLint("CommitPrefEdits") @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /*SharedPreferences sharedPreferences = getSharedPreferences("smart", Context.MODE_PRIVATE);

    SharedPreferences.Editor edit = sharedPreferences.edit();
    edit.putString("string", "one").commit();

    Set<String> strings = new ArraySet<>(3);
    strings.add("1");
    strings.add("2");
    strings.add("3");
    edit.putStringSet("set", strings);*/

    handler.postDelayed(new Runnable() {
      @Override public void run() {
        System.out.println("run");
      }
    }, 10 * 60 * 1000);

    System.out.println("onCreate");
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    System.out.println("onDestroy");
  }
}
