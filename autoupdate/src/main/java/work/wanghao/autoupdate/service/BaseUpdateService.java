package work.wanghao.autoupdate.service;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import work.wanghao.autoupdate.bean.FirVersionInfo;
import work.wanghao.autoupdate.bean.PackageInfo;
import work.wanghao.autoupdate.callback.DownloadProgressCallback;
import work.wanghao.autoupdate.callback.DownloadTaskCanceledCallback;
import work.wanghao.autoupdate.utils.CommonUtils;
import work.wanghao.autoupdate.utils.NetUtils;
import work.wanghao.autoupdate.utils.PackageUtils;

/**
 * Create on: 2016-07-30
 * Author: wangh
 * Summary: TODO
 */
public abstract class BaseUpdateService extends Service implements DownloadProgressCallback {
  private final static int THREAD_KEEP_ALIVE_TIME = 1;
  private final static int NOTIFICATION_ID = 20169394;
  private final static String DIR_NAME = "update";

  private DownloadTask mDownloadTask;
  private String DOWNLOAD_DIR;

  private NotificationCompat.Builder mBuilder;
  private NotificationManager mNotificationManager;
  private ConnectivityManager mConnectivityManager;
  private ThreadPoolExecutor mPoolExecutor;

  private PackageInfo mDirCacheLastPackInfo;

  private int NUMBER_OF_CORES;

  private boolean downloadAllNetStatus;

  @Override public final void onCreate() {
    super.onCreate();
    initThreadPool();
    initNotification();
    initDownloadDir();
    initConnectManager();
  }

  @Override public final int onStartCommand(Intent intent, int flags, int startId) {
    setupFirTask(intent);
    return START_FLAG_RETRY;
  }

  @Nullable @Override public final IBinder onBind(Intent intent) {
    return null;
  }

  @Override public final void onDestroy() {
    super.onDestroy();
    if (mDownloadTask != null && !mDownloadTask.isCancelled()) {
      mDownloadTask.cancel(true);
    }
    if (mPoolExecutor != null) {
      if (!mPoolExecutor.isShutdown()) {
        mPoolExecutor.shutdownNow();
      }
    }
  }

  private OkHttpClient getOkHttpClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.connectTimeout(120, TimeUnit.SECONDS);
    builder.retryOnConnectionFailure(true);
    return builder.build();
  }

  @Override public void progressCallback(final double progress) {
    mBuilder.setProgress(100, (int) progress, false);
    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
  }

  @Override public void downloadFail(Exception exception) {

  }

  @Override public void downloadCompleted(File file) {
    if (getPackageName().equals(getPackageName())) PackageUtils.installAPK(this, file);
    stopForeground(true);
    stopSelf();
  }

  private void initThreadPool() {
    NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    mPoolExecutor =
        new ThreadPoolExecutor(0, NUMBER_OF_CORES, THREAD_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
  }

  @SuppressWarnings("unchecked") private void initDownloadDir() {
    mPoolExecutor.execute(new Runnable() {
      @Override public void run() {
        initDownloadTask();
      }
    });
  }

  private void initDownloadTask() {
    if (getApplication().getExternalCacheDir() != null && !TextUtils.isEmpty(
        getApplication().getExternalCacheDir().getAbsolutePath())) {
      DOWNLOAD_DIR =
          getApplication().getExternalCacheDir().getAbsolutePath() + File.separator + DIR_NAME;
      final File file = new File(DOWNLOAD_DIR);
      if (!file.exists()) {
        if (!file.mkdir()) {
          /*todo*/
        } else {
          /*todo*/
        }
      } else {

        mPoolExecutor.execute(new Runnable() {
          @Override public void run() {
            listDownloadDirApkTask(file.getAbsolutePath());
          }
        });
      }
    } else {
      /*todo*/
      stopSelf();
    }
  }

  /**
   * 列出指定目录所有下载好的apk安装包
   *
   * @param path 安装包的目录
   */
  private void listDownloadDirApkTask(final String path) {
    List<File> files = new ArrayList<>();
    File dir = new File(path);
    if (dir.exists() && dir.isDirectory()) {
      File[] fileList = dir.listFiles();
      if (fileList.length > 0) {
        for (File file : fileList) {
          if (file.isFile() && file.getName().endsWith(".apk")) {
            files.add(file);
          }
        }
        if (files.size() > 0) {
          List<PackageInfo> packageInfoList = new ArrayList<>();
          for (File file : files) {
            packageInfoList.add(
                PackageUtils.getUninstallAPKInfo(BaseUpdateService.this, file.getAbsolutePath()));
          }
          if (packageInfoList.size() > 0) {
            List<PackageInfo> samePackageList = new ArrayList<>();
            for (PackageInfo info : packageInfoList) {
              if (getPackageName().equals(info.packageName)) {
                if (PackageUtils.getPackageInfo(BaseUpdateService.this).versionCode
                    < info.versionCode) {
                  samePackageList.add(info);
                }
                continue;
              }
            }
            if (samePackageList.size() > 0) {
              int versionCode = 0;
              PackageInfo packageInfo = null;
              for (PackageInfo info : samePackageList) {
                if (info.versionCode > versionCode) {
                  versionCode = info.versionCode;
                  packageInfo = info;
                }
              }
              if (packageInfo != null) {
                mDirCacheLastPackInfo = packageInfo;
                for (File file : files) {
                  if (file.getAbsolutePath().equals(packageInfo.path)) {
                    continue;
                  }
                  file.deleteOnExit();
                }
              } else {
                for (File file : files) {
                  boolean b = file.delete();
                }
              }
            } else {
              for (File file : files) {
                boolean b = file.delete();
              }
            }
          }
        }
      } else {
        /*todo*/
      }
    } else {
      /*todo*/
    }
  }

  private void initConnectManager() {
    mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
  }

  private void initNotification() {
    mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
        getApplication().getApplicationInfo().icon)
        .setOngoing(true)
        .setContentText("正在更新" + getString(getApplicationInfo().labelRes))
        .setContentTitle(getString(getApplicationInfo().labelRes));
    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
  }

  private void setupFirTask(final Intent intent) {
    downloadAllNetStatus = intent.getBooleanExtra("download_all_net_status", false);
    if (downloadAllNetStatus) {
      mPoolExecutor.execute(new Runnable() {
        @Override public void run() {
          setupFir(intent);
        }
      });
    } else {
      if (NetUtils.getConnectedType(mConnectivityManager) == ConnectivityManager.TYPE_WIFI) {
        mPoolExecutor.execute(new Runnable() {
          @Override public void run() {
            setupFir(intent);
          }
        });
      } else {
        stopSelf();
      }
    }
  }

  private void setupFir(final Intent intent) {

    String apiToken = intent.getStringExtra("api_token");
    if (TextUtils.isEmpty(apiToken)) {
      stopSelf();
    }
    FIR.checkForUpdateInFIR(apiToken, new VersionCheckCallback() {
      @Override public void onSuccess(String s) {
        try {
          isNewVersion(s);
        } catch (JSONException e) {
          e.printStackTrace();
          stopSelf();
        }
      }

      @Override public void onFail(Exception e) {
        e.printStackTrace();
        /*todo*/
        setupFir(intent);
      }
    });
  }

  private void isNewVersion(String json) throws JSONException {
    JSONObject jsonObject = new JSONObject(json);
    FirVersionInfo info = CommonUtils.parseJsonToObj(jsonObject);
    if (mDirCacheLastPackInfo != null) {
      if (mDirCacheLastPackInfo.versionCode >= Integer.parseInt(info.versionCode)) {
        if (Math.abs(new File(mDirCacheLastPackInfo.path).length() - info.fileSize) < 500) {
          Dialog dialog = setInstallTipsDialog(info, mDirCacheLastPackInfo.path);
          if (dialog == null) {
            throw new NullPointerException("dialog is empty");
          } else {
            if (Build.VERSION.SDK_INT >= 19) {
              dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
              dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
            dialog.show();
          }
        } else {
          File tempFile = new File(mDirCacheLastPackInfo.path);
          if (tempFile.exists()) tempFile.delete();
          PackageInfo current = PackageUtils.getPackageInfo(this);
          if (current.versionCode < Integer.parseInt(info.versionCode)) {
            showNewVersion(info);
          } else {
            stopSelf();
          }
        }
      } else {
        File tempFile = new File(mDirCacheLastPackInfo.path);
        if (tempFile.exists()) tempFile.delete();
        PackageInfo current = PackageUtils.getPackageInfo(this);
        if (current.versionCode < Integer.parseInt(info.versionCode)) {
          showNewVersion(info);
        } else {
          stopSelf();
        }
      }
    } else {
      PackageInfo current = PackageUtils.getPackageInfo(this);
      if (current.versionCode < Integer.parseInt(info.versionCode)) {
        showNewVersion(info);
      } else {
        stopSelf();
      }
    }
  }

  private void showNewVersion(FirVersionInfo info) {
    final Dialog dialog = setDialogContent(info);
    if (dialog == null) throw new NullPointerException("Dialog Must Not be NULL!");
    if (Build.VERSION.SDK_INT >= 19) {
      dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
    } else {
      dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }
    dialog.show();
  }

  protected abstract Dialog setInstallTipsDialog(final FirVersionInfo info,
      final String installApkPath);

  protected final void onInstallClick(Dialog dialog, DialogInterface dialogInterface, File file) {
    if (dialog != null) {
      dialog.dismiss();
    } else if (dialogInterface != null) {
      dialogInterface.dismiss();
    } else {
      throw new IllegalArgumentException("dialog and dialogInterface Not be NULL both!");
    }
    PackageUtils.installAPK(this, file);
    stopSelf();
  }

  protected final void onCancelInstallClick(Dialog dialog, DialogInterface dialogInterface) {
    if (dialog != null) {
      dialog.dismiss();
    } else if (dialogInterface != null) {
      dialogInterface.dismiss();
    } else {
      throw new IllegalArgumentException("dialog and dialogInterface Not be NULL both!");
    }
    stopSelf();
  }

  protected abstract Dialog setDialogContent(final FirVersionInfo info);

  protected final void onDownloadClick(Dialog dialog, DialogInterface dialogInterface,
      final FirVersionInfo info) {
    if (dialog != null) {
      dialog.dismiss();
    } else if (dialogInterface != null) {
      dialogInterface.dismiss();
    } else {
      throw new IllegalArgumentException("dialog and dialogInterface Not be NULL both!");
    }
    if (mDownloadTask == null) {
      mDownloadTask = new DownloadTask(getOkHttpClient(), info.downloadUrl, DOWNLOAD_DIR);
    }
    if (mDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
      if (mDownloadTask.getNetUrl().equals(info.downloadUrl)) {
        return;
      } else {
        mDownloadTask.cancel(true);
        mDownloadTask.setCanceledCallback(new DownloadTaskCanceledCallback() {
          @Override public void onCanceledCallback() {
            mDownloadTask = null;
            mDownloadTask = new DownloadTask(getOkHttpClient(), info.downloadUrl, DOWNLOAD_DIR);
            mDownloadTask.setupProgressCallBack(BaseUpdateService.this);
            startForeground(NOTIFICATION_ID, mBuilder.build());
            mDownloadTask.execute();
          }
        });
      }
    } else {
      mDownloadTask = null;
      mDownloadTask = new DownloadTask(getOkHttpClient(), info.downloadUrl, DOWNLOAD_DIR);
      mDownloadTask.setupProgressCallBack(BaseUpdateService.this);
      startForeground(NOTIFICATION_ID, mBuilder.build());
      mDownloadTask.execute();
    }
  }

  protected final void onCancelDownloadClick(Dialog dialog, DialogInterface dialogInterface) {
    if (dialog != null) {
      dialog.dismiss();
      stopSelf();
    } else if (dialogInterface != null) {
      dialogInterface.dismiss();
      stopSelf();
    } else {
      throw new IllegalArgumentException("dialog and dialogInterface Not be NULL both!");
    }
  }
}
