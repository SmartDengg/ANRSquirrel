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

package com.smartdengg.anrsquirrel;

import java.io.Serializable;

/**
 * 创建时间:  16/7/18 下午5:22 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public interface SquirrelListener extends Serializable {
  long serialVersionUID = 4543664503268951395L;

  void onAppNotResponding(ANRError error);
}
