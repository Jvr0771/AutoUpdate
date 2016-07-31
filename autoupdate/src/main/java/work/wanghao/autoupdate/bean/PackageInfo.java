package work.wanghao.autoupdate.bean;

/**
 * Create on: 2016-07-30
 * Author: wangh
 * Summary: TODO
 */
public class PackageInfo {

  public String versionName;
  public String packageName;
  public int versionCode;
  public String path;

  public void setPath(String path) {
    this.path = path;
  }

  public PackageInfo(String versionName, String packageName, int versionCode) {
    this.versionName = versionName;
    this.packageName = packageName;
    this.versionCode = versionCode;
  }

  public PackageInfo() {
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setVersionCode(int versionCode) {
    this.versionCode = versionCode;
  }

  @Override public String toString() {
    return "PackageInfo{" +
        "versionName='" + versionName + '\'' +
        ", packageName='" + packageName + '\'' +
        ", versionCode=" + versionCode +
        ", path='" + path + '\'' +
        '}';
  }
}
