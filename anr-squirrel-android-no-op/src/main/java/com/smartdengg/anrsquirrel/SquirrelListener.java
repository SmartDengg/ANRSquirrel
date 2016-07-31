package com.smartdengg.anrsquirrel;

import java.io.Serializable;

/**
 * Created by SmartDengg on 2016/7/31.
 */
public interface SquirrelListener extends Serializable {

  long serialVersionUID = 3996383371697859813L;

  /** stub **/
  void onAppNotResponding(ANRError error);
}
