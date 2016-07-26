package com.smartdengg.anrsquirrel.lib;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ImageView;
import com.smartdengg.squirrellib.R;

/**
 * Created by SmartDengg on 2016/7/24.
 */
public class SquirrelMarble {

  private static SquirrelMarble instance;
  private Context context;

  private ImageView marbleView;
  private WindowManager windowManager;
  private MarbleTouchListener marbleTouchListener;
  private WindowManager.LayoutParams layoutParams;

  private SquirrelMarble(Context context) {
    this.context = context;
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
    layoutParams.gravity = Gravity.CENTER | Gravity.TOP;
    layoutParams.y = 100;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
    } else {
      layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
    }

    this.marbleTouchListener = new MarbleTouchListener(layoutParams, windowManager);
    marbleView.setOnTouchListener(marbleTouchListener);
  }

  public static SquirrelMarble initWith(Context context) {
    if (instance == null) {
      synchronized (SquirrelMarble.class) {
        if (instance == null) {
          instance = new SquirrelMarble(context);
        }
      }
    }
    return instance;
  }

  public void show() {
    this.windowManager.addView(marbleView, layoutParams);
  }

  public void update() {
    this.marbleView.setImageDrawable(
        context.getResources().getDrawable(R.drawable.half_water_melon));
    //this.marbleView.setBackgroundResource(R.drawable.marble_status_anr);
  }

  public void reset() {
    this.marbleView.setImageDrawable(context.getResources().getDrawable(R.drawable.water_melon));
    //this.marbleView.setBackgroundResource(R.drawable.marble_status_normal);
  }
}
