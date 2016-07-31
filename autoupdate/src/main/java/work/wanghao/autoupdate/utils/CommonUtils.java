package work.wanghao.autoupdate.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import work.wanghao.autoupdate.bean.FirVersionInfo;

/**
 * Create on: 2016-07-30
 * Author: wangh
 * Summary: TODO
 */
public class CommonUtils {

  public static String bytes2kb(long bytes) {
    BigDecimal fileSize = new BigDecimal(bytes);
    BigDecimal megabyte = new BigDecimal(1024 * 1024);
    float returnValue = fileSize.divide(megabyte, 2, BigDecimal.ROUND_UP).floatValue();
    if (returnValue > 1) return (returnValue + "MB");
    BigDecimal kilobyte = new BigDecimal(1024);
    returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP).floatValue();
    return (returnValue + "KB");
  }

  public static String formatDate(long time) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    return simpleDateFormat.format(new Date(time * 1000));
  }

  public static FirVersionInfo parseJsonToObj(JSONObject json) throws JSONException {
    FirVersionInfo versionInfo = new FirVersionInfo();
    versionInfo.appName = json.getString("name");
    versionInfo.changeLog = json.getString("changelog");
    versionInfo.downloadUrl = json.getString("direct_install_url");
    versionInfo.fileSize = json.getJSONObject("binary").getLong("fsize");
    versionInfo.updateDate = CommonUtils.formatDate(json.getLong("updated_at"));
    versionInfo.updateAtS = json.getLong("updated_at");
    versionInfo.versionCode = json.getString("build");
    versionInfo.versionName = json.getString("versionShort");
    return versionInfo;
  }
}
