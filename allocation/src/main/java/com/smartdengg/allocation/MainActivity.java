package com.smartdengg.allocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  private static List<Activity> list = new ArrayList<>();

  Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      System.out.printf(msg.toString());
    }
  };
  private FragmentManager mFragmentManager;

  @SuppressLint("CommitPrefEdits") @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    SharedPreferences sharedPreferences = getSharedPreferences("smart", Context.MODE_PRIVATE);

    SharedPreferences.Editor edit = sharedPreferences.edit();
    edit.putString("string1", "one").commit();
    edit.putString("string2", "two").commit();

    Set<String> strings = new HashSet<>(3);
    strings.add("1");
    strings.add("2");
    strings.add("3");
    edit.putStringSet("set", strings);
    edit.commit();

    edit.putString("nullvalue", "haha");
    edit.commit();

    edit.remove("nullvalue");
    edit.commit();

    edit.clear().commit();

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
