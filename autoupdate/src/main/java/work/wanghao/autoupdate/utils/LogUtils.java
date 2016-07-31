package work.wanghao.autoupdate.utils;

import android.util.Log;
import work.wanghao.autoupdate.BuildConfig;

/**
 * Create on: 2016-07-28
 * Author: wangh
 * Summary: TODO
 */
public class LogUtils {
  public static void v(Object o, String message) {
    if (BuildConfig.DEBUG) {
      Log.v(o.getClass().getCanonicalName(), message);
    }
  }

  public static void i(Object o, String message) {
    if (BuildConfig.DEBUG) {
      Log.i(o.getClass().getCanonicalName(), message);
    }
  }

  public static void d(Object o, String message) {
    if (BuildConfig.DEBUG) {
      Log.d(o.getClass().getCanonicalName(), message);
    }
  }

  public static void w(Object o, String message) {
    if (BuildConfig.DEBUG) {
      Log.w(o.getClass().getCanonicalName(), message);
    }
  }

  public static void e(Object o, String message) {
    if (BuildConfig.DEBUG) {
      Log.e(o.getClass().getCanonicalName(), message);
    }
  }
}
