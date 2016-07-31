package com.smartdengg.anrsquirrel.lib;

import com.smartdengg.squirrel.ANRError;
import java.io.Serializable;

/**
 * 创建时间:  16/7/18 下午5:22 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public interface SquirrelListener extends Serializable {
  void onAppNotResponding(ANRError error);

  void onInterrupted(InterruptedException exception);
}
