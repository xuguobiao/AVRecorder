package com.kido.androidlame.manager;

import android.util.Log;

/**
 * @author XuGuobiao
 * @email everlastxgb@gmail.com
 * @create_date 2016/4/11
 */
public class DebugLog {
  public static final boolean isDebugEnable = true;
  public static final String TAG = "Kido";

  public static void log(String logString) {
    if (isDebugEnable) {
      Log.d(TAG, logString);
    }
  }

  public static void i(String msg) {
    i(TAG, msg);
  }

  public static void i(String tag, String msg) {
    if (isDebugEnable) {
      Log.i(tag, msg);
    }
  }


  public static void d(String msg) {
    d(TAG, msg);
  }

  public static void d(String tag, String msg) {
    if (isDebugEnable) {
      Log.d(tag, msg);
    }
  }

  public static void e(String msg) {
    if (isDebugEnable) {
      e(TAG, msg);
    }
  }

  public static void e(String tag, String msg) {
    if (isDebugEnable) {
      Log.e(tag, msg);
    }
  }


}
