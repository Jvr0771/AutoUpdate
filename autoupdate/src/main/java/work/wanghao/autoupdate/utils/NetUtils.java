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

  public static boolean isNetworkConnected(ConnectivityManager manager) {

    NetworkInfo networkInfo = manager.getActiveNetworkInfo();

    if (networkInfo != null) return networkInfo.isAvailable();

    return false;
  }

  public static boolean isMobileConnected(ConnectivityManager manager) {

    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
      return networkInfo.isAvailable();
    }

    return false;
  }

  public static int getConnectedType(ConnectivityManager manager) {

    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isAvailable()) {

      return networkInfo.getType();
    }

    return -1;
  }

  public static boolean isGPSEnabled(Context context) {

    LocationManager locationManager =
        ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }
}
