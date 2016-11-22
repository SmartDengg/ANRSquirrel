package com.smartdengg.allocation;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by SmartDengg on 2016/11/21.
 */
public class MyApp extends Application {

  private AClazz mAClazz;

  @Override public void onCreate() {
    super.onCreate();
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);

    DClazz dClazz = new DClazz();

    BClazz bClazz = new BClazz(dClazz);
    CClazz cClazz = new CClazz(dClazz);

    mAClazz = new AClazz(bClazz, cClazz);
  }

  class AClazz {

    private BClazz mBClazz;
    private CClazz mCClazz;

    public AClazz(BClazz bClazz, CClazz cClazz) {
      this.mBClazz = bClazz;
      this.mCClazz = cClazz;
    }
  }

  class BClazz {
    private byte[] bBytes = new byte[128];

    private DClazz mDClazz;

    public BClazz(DClazz clazz) {
      this.mDClazz = clazz;
    }
  }

  class CClazz {
    private byte[] cBytes = new byte[256];

    private DClazz mDClazz;

    public CClazz(DClazz clazz) {
      this.mDClazz = clazz;
    }
  }

  class DClazz {
    private byte[] dBytes = new byte[512];
  }
}
