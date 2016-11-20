/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.anrsquirrel.marble;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.smartdengg.anrsquirrel.Utils;

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
    if (!Utils.isForeground(activity) && lifecycleListener != null) lifecycleListener.onBackground();
  }

  @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

  }

  @Override public void onActivityDestroyed(Activity activity) {

  }
}
