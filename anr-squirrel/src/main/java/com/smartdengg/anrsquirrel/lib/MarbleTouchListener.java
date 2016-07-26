package com.smartdengg.anrsquirrel.lib;

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

  public MarbleTouchListener(WindowManager.LayoutParams layoutParams, WindowManager windowManager) {
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
        break;
    }
    return false;
  }
}
