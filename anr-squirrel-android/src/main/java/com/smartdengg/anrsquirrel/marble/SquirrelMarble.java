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

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;
import com.smartdengg.mylibrary.R;

/**
 * Created by SmartDengg on 2016/7/24.
 */
public class SquirrelMarble {

  private static SquirrelMarble instance;
  private final Point marblePoint;
  private final Context context;

  private ImageView marbleView;
  private WindowManager windowManager;
  private WindowManager.LayoutParams layoutParams;
  private int lastX, lastY;
  private boolean isHalf = false;

  private MarbleLifecycleCallback.LifecycleListener lifecycleListener =
      new MarbleLifecycleCallback.LifecycleListener() {
        @Override public void onForeground() {
          if (!isShowing()) SquirrelMarble.this.show();
        }

        @Override public void onBackground() {
          if (isShowing()) SquirrelMarble.this.hide();
        }
      };

  private SquirrelMarble(Context context, Point point) {
    this.context = context;
    this.marblePoint = point;
    this.initOverlayMarble();
  }

  private void initOverlayMarble() {
    this.marbleView =
        (ImageView) LayoutInflater.from(context).inflate(R.layout.marble_view, null, false);
    this.marbleView.setHapticFeedbackEnabled(false);
    this.windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);

    layoutParams = new WindowManager.LayoutParams();
    layoutParams.height = layoutParams.width =
        context.getResources().getDimensionPixelOffset(R.dimen.marble_diameter);
    layoutParams.format = PixelFormat.TRANSLUCENT;
    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    layoutParams.windowAnimations = android.R.style.Animation_Toast;
    layoutParams.gravity = Gravity.START | Gravity.TOP;

    Point displayPoint = new Point();
    this.windowManager.getDefaultDisplay().getSize(displayPoint);
    this.lastX = layoutParams.x = marblePoint.x < displayPoint.x ? marblePoint.x : displayPoint.x;
    this.lastY = layoutParams.y = marblePoint.y < displayPoint.y ? marblePoint.y : displayPoint.y;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    } else {
      layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
    }

    MarbleTouchListener marbleTouchListener = new MarbleTouchListener(layoutParams, windowManager);
    this.marbleView.setOnTouchListener(marbleTouchListener);
    MarbleLifecycleCallback.INSTANCE.registerLifecycle((Application) context, lifecycleListener);
  }

  public static SquirrelMarble initWith(Context context, Point point) {
    if (instance == null) {
      synchronized (SquirrelMarble.class) {
        if (instance == null) {
          instance = new SquirrelMarble(context, point);
        }
      }
    }
    return instance;
  }

  public void show() {

    this.layoutParams.x = lastX;
    this.layoutParams.y = lastY;

    this.windowManager.addView(marbleView, layoutParams);
  }

  private void hide() {
    if (this.marbleView.getParent() != null) this.windowManager.removeViewImmediate(marbleView);

    this.lastX = layoutParams.x;
    this.lastY = layoutParams.y;
  }

  private boolean isShowing() {
    return marbleView.getParent() != null;
  }

  public void update() {
    if (this.isHalf) return;
    this.isHalf = true;

    this.marbleView.setImageDrawable(
        context.getResources().getDrawable(R.drawable.half_water_melon));
  }

  public void reset() {
    if (!this.isHalf) return;
    this.isHalf = false;

    this.marbleView.setImageDrawable(context.getResources().getDrawable(R.drawable.water_melon));
  }
}
