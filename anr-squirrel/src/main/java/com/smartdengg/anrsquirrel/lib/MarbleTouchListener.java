package com.smartdengg.anrsquirrel.lib;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by brianplummer on 9/12/15.
 */
public class MarbleTouchListener implements View.OnTouchListener {

  private int initialX;
  private int initialY;
  private float initialTouchX;
  private float initialTouchY;

  private WindowManager.LayoutParams paramsF;
  private WindowManager windowManager;
  private GestureDetector gestureDetector;

  public MarbleTouchListener(WindowManager.LayoutParams layoutParams, WindowManager windowManager,
      GestureDetector gestureDetector) {
    this.windowManager = windowManager;
    this.paramsF = layoutParams;
    this.gestureDetector = gestureDetector;
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        initialX = paramsF.x;
        initialY = paramsF.y;
        initialTouchX = event.getRawX();
        initialTouchY = event.getRawY();
        break;
      case MotionEvent.ACTION_MOVE:
        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
        windowManager.updateViewLayout(v, paramsF);
        break;
      case MotionEvent.ACTION_UP:
        break;
    }
    return false;
  }
}
