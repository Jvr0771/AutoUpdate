package work.wanghao.autoupdate.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Create on: 2016-07-31
 * Author: wangh
 * Summary: TODO
 */
public class NetUtils {
  /**
   * 判断是否有网络连接
   */
  public static boolean isNetworkConnected(ConnectivityManager manager) {

    // 获取NetworkInfo对象
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    //判断NetworkInfo对象是否为空
    if (networkInfo != null) return networkInfo.isAvailable();

    return false;
  }

  /**
   * 判断WIFI网络是否可用
   */
  public static boolean isMobileConnected(ConnectivityManager manager) {

    //获取NetworkInfo对象
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    //判断NetworkInfo对象是否为空 并且类型是否为MOBILE 
    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
      return networkInfo.isAvailable();
    }

    return false;
  }

  /**
   * 获取当前网络连接的类型信息
   * 原生
   */
  public static int getConnectedType(ConnectivityManager manager) {

    //获取NetworkInfo对象
    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isAvailable()) {
      //返回NetworkInfo的类型
      return networkInfo.getType();
    }

    return -1;
  }

  /**
   * 判断GPS是否打开
   * ACCESS_FINE_LOCATION权限
   */
  public static boolean isGPSEnabled(Context context) {
    //获取手机所有连接LOCATION_SERVICE对象        
    LocationManager locationManager =
        ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }
}
