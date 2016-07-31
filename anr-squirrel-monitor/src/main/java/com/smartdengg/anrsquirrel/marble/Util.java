package com.smartdengg.anrsquirrel.marble;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 创建时间:  16/7/27 下午3:52 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class Util {

  private Util() {

    throw new IllegalStateException("No instance!");
  }

  public static boolean isForeground(Context context) {

    ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      ActivityManager.RunningAppProcessInfo topAppProcess = Util.getTopProcessCompatV21(context);
      return topAppProcess != null && topAppProcess.pid == android.os.Process.myPid();
    } else {
      List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
      ActivityManager.RunningTaskInfo localRunningTaskInfo = tasks.get(0);
      String topPackageName = localRunningTaskInfo.topActivity.getPackageName();
      String myPackageName = context.getPackageName();
      return topPackageName.equals(myPackageName);
    }
  }

  @SuppressWarnings("all")
  private static ActivityManager.RunningAppProcessInfo getTopProcessCompatV21(Context context) {
    final int PROCESS_STATE_TOP = 2;
    ActivityManager.RunningAppProcessInfo currentInfo = null;
    Field field = null;
    try {
      field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
    } catch (Exception ignored) {
    }
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
    if (appList == null || appList.size() == 0) return null;
    for (ActivityManager.RunningAppProcessInfo app : appList) {
      if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
          && app.importanceReasonCode == 0) {
        Integer state = null;
        try {
          state = field.getInt(app);
        } catch (Exception ignored) {
        }
        if (state != null && state == PROCESS_STATE_TOP) {
          currentInfo = app;
          break;
        }
      }
    }
    return currentInfo;
  }
}
