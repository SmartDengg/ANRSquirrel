package com.smartdengg.anrsquirrel.marble;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * 创建时间:  16/7/27 下午3:47 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
enum MarbleLifecycleCallback implements Application.ActivityLifecycleCallbacks {
  INSTANCE;

  public interface LifecycleListener {

    void onForeground();

    void onBackground();
  }

  private LifecycleListener lifecycleListener;

  public void registerLifecycle(Application application, LifecycleListener lifecycleListener) {
    this.lifecycleListener = lifecycleListener;
    application.registerActivityLifecycleCallbacks(MarbleLifecycleCallback.this);
  }

  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override public void onActivityStarted(Activity activity) {
    if (lifecycleListener != null) lifecycleListener.onForeground();
  }

  @Override public void onActivityResumed(Activity activity) {

  }

  @Override public void onActivityPaused(Activity activity) {

  }

  @Override public void onActivityStopped(Activity activity) {
    if (!Util.isForeground(activity) && lifecycleListener != null) {
      lifecycleListener.onBackground();
    }
  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override public void onActivityDestroyed(Activity activity) {

  }
}
