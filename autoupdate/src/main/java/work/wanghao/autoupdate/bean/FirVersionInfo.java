package work.wanghao.autoupdate.bean;

/**
 * Create on: 2016-07-30
 * Author: wangh
 * Summary: TODO
 */
public class FirVersionInfo {

  public String appName;
  public String versionCode;
  public String changeLog;
  public long updateAtS;
  public String updateDate;
  public String versionName;
  public String downloadUrl;
  public long fileSize;

  public FirVersionInfo() {
  }

  public FirVersionInfo(String appName, String versionCode, String changeLog, long updateAtS,
      String updateData, String versionName, String downloadUrl, long fileSize) {
    this.appName = appName;
    this.versionCode = versionCode;
    this.changeLog = changeLog;
    this.updateAtS = updateAtS;
    this.updateDate = updateData;
    this.versionName = versionName;
    this.downloadUrl = downloadUrl;
    this.fileSize = fileSize;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setVersionCode(String versionCode) {
    this.versionCode = versionCode;
  }

  public void setChangeLog(String changeLog) {
    this.changeLog = changeLog;
  }

  public void setUpdateAtS(long updateAtS) {
    this.updateAtS = updateAtS;
  }

  public void setUpdateDate(String updateDate) {
    this.updateDate = updateDate;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  @Override public String toString() {
    return "FirVersionInfo{" +
        "appName='" + appName + '\'' +
        ", versionCode='" + versionCode + '\'' +
        ", changeLog='" + changeLog + '\'' +
        ", updateAtS=" + updateAtS +
        ", updateDate='" + updateDate + '\'' +
        ", versionName='" + versionName + '\'' +
        ", downloadUrl='" + downloadUrl + '\'' +
        ", fileSize=" + fileSize +
        '}';
  }
}
