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

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by SmartDengg on 9/12/15.
 */
class MarbleTouchListener implements View.OnTouchListener {

  private int initialX;
  private int initialY;
  private float initialTouchX;
  private float initialTouchY;

  private WindowManager.LayoutParams layoutParams;
  private WindowManager windowManager;
  private GestureDetector gestureDetector;

  public MarbleTouchListener(WindowManager.LayoutParams layoutParams, WindowManager windowManager,
      GestureDetector gestureDetector) {
    this.windowManager = windowManager;
    this.layoutParams = layoutParams;
    this.gestureDetector = gestureDetector;
  }

  MarbleTouchListener(WindowManager.LayoutParams layoutParams, WindowManager windowManager) {
    this.windowManager = windowManager;
    this.layoutParams = layoutParams;
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    if (gestureDetector != null) gestureDetector.onTouchEvent(event);
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        initialX = layoutParams.x;
        initialY = layoutParams.y;
        initialTouchX = event.getRawX();
        initialTouchY = event.getRawY();
        break;
      case MotionEvent.ACTION_MOVE:
        layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
        layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
        windowManager.updateViewLayout(v, layoutParams);
        break;
      case MotionEvent.ACTION_UP:
        v.performClick();
        break;
      default:
        break;
    }
    return true;
  }
}
