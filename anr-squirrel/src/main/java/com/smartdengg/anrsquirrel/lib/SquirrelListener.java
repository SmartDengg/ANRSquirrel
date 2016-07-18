package com.smartdengg.anrsquirrel.lib;

/**
 * 创建时间:  16/7/18 下午5:22 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public interface SquirrelListener {
  void onAppNotResponding(ANRError error);

  void onInterrupted(InterruptedException exception);
}
