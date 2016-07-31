package work.wanghao.autoupdate.service;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import java.io.File;
import work.wanghao.autoupdate.R;
import work.wanghao.autoupdate.bean.FirVersionInfo;
import work.wanghao.autoupdate.utils.CommonUtils;

/**
 * Create on: 2016-07-31
 * Author: wangh
 * Summary: TODO
 */
public class DefaultDownloadService extends BaseUpdateService {
  @Override
  protected Dialog setInstallTipsDialog(FirVersionInfo info, final String installApkPath) {
    final AlertDialog.Builder builder =
        new AlertDialog.Builder(this, R.style.AlertDialog_AppCompat_Dialog);
    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        onCancelInstallClick(null, dialogInterface);
      }
    })
        .setPositiveButton("安装", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialogInterface, int i) {
            onInstallClick(null, dialogInterface, new File(installApkPath));
          }
        })
        .setTitle(info.appName + "发现新版本")
        .setMessage("发现新版本已为您下载完毕，是否安装?\n更新日期:"
            + info.updateDate
            + "\n"
            + "更新日志:"
            + info.changeLog
            + "\n"
            + "版本号:"
            + info.versionName
            + "\nBuild:"
            + info.versionCode
            + "\n安装包大小:"
            + CommonUtils.bytes2kb(info.fileSize));

    return builder.create();
  }

  @Override protected Dialog setDialogContent(final FirVersionInfo info) {
    final AlertDialog.Builder builder =
        new AlertDialog.Builder(this, R.style.AlertDialog_AppCompat_Dialog);
    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        onCancelDownloadClick(null, dialogInterface);
      }
    })
        .setPositiveButton("更新", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialogInterface, int i) {
            onDownloadClick(null, dialogInterface, info);
          }
        })
        .setTitle(info.appName + "发现新版本")
        .setMessage("更新日期:"
            + info.updateDate
            + "\n"
            + "更新日志:"
            + info.changeLog
            + "\n"
            + "版本号:"
            + info.versionName
            + "\nBuild:"
            + info.versionCode
            + "\n安装包大小:"
            + CommonUtils.bytes2kb(info.fileSize));
    return builder.create();
  }
}
