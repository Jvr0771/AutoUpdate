package work.wanghao.autoupdate.service;

import android.os.AsyncTask;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import work.wanghao.autoupdate.callback.DownloadProgressCallback;
import work.wanghao.autoupdate.callback.DownloadTaskCanceledCallback;
import work.wanghao.autoupdate.utils.LogUtils;

/**
 * Create on: 2016-07-30
 * Author: wangh
 * Summary: TODO
 */
public class DownloadTask extends AsyncTask<Void, Void, Exception> {

  private OkHttpClient mOkHttpClient;
  private String netUrl;
  private String storePath;
  private DownloadProgressCallback mProgressCallback;
  private final static String UPDATE_FILE_NAME = "update.apk";
  private DownloadTaskCanceledCallback mCanceledCallback;

  private File currentDownloadCompleted;

  public String getNetUrl() {
    return netUrl;
  }

  public void setupProgressCallBack(DownloadProgressCallback progressCallback) {
    this.mProgressCallback = progressCallback;
  }

  public DownloadTask(OkHttpClient okHttpClient, String netPath, String dirPath) {
    super();
    if (okHttpClient == null) {
      throw new NullPointerException("OkHttpClient is NULL!");
    }
    File file = new File(dirPath);
    if (file.exists() && !file.isDirectory()) {
      throw new IllegalArgumentException("dirPath must be a dir path");
    }
    this.mOkHttpClient = okHttpClient;
    this.netUrl = netPath;
    this.storePath = dirPath;
  }

  public DownloadTask(OkHttpClient okHttpClient, String netPath, File dirPath) {
    super();
    if (okHttpClient == null) {
      throw new NullPointerException("OkHttpClient is NULL!");
    }
    this.mOkHttpClient = okHttpClient;
    this.netUrl = netPath;
    if (dirPath.exists() && !dirPath.isDirectory()) {
      throw new IllegalArgumentException("dirPath must be a dir path");
    }
    storePath = dirPath.getAbsolutePath();

    /*
    if (dirPath.exists()) {
      String path =
          dirPath.getPath().substring(0, dirPath.getPath().length() - dirPath.getName().length());
      LogUtils.d(this, path);
      String name = dirPath.getName().substring(0, dirPath.getName().length() - 4);
      LogUtils.d(this, name);
      StringBuilder stringBuilder = new StringBuilder(name);
      for (int i = 1; ; i++) {
        LogUtils.d(this, "循环次数:" + i);
        if (!new File(path + stringBuilder.insert(stringBuilder.length(), i) + ".apk").exists()) {
          dirPath = new File(path + stringBuilder.toString() + ".apk");
          LogUtils.d(this, dirPath.getAbsolutePath());
          break;
        }
      }
    }*/
    this.storePath = dirPath.getAbsolutePath();
  }

  @Override protected void onPreExecute() {
    super.onPreExecute();
  }

  @Override protected void onPostExecute(Exception aVoid) {
    super.onPostExecute(aVoid);
    if (aVoid == null) {
      LogUtils.d(this, "下载完成");
      if (mProgressCallback != null && currentDownloadCompleted != null) {
        mProgressCallback.downloadCompleted(currentDownloadCompleted);
        currentDownloadCompleted = null;
      }
    } else {
      LogUtils.e(this, "下载失败:" + aVoid.getMessage());
      if (mProgressCallback != null) mProgressCallback.downloadFail(aVoid);
    }
  }

  public void setCanceledCallback(DownloadTaskCanceledCallback canceledCallback) {
    mCanceledCallback = canceledCallback;
    currentDownloadCompleted = null;
  }

  @Override protected void onCancelled() {
    super.onCancelled();
    if (mCanceledCallback != null) mCanceledCallback.onCanceledCallback();
  }

  @Override protected Exception doInBackground(Void... strings) {
    return initDownloadPath();
  }

  private Exception initDownloadPath() {
    LogUtils.d(this, "开始初始化下载文件的保存全路径");
    File downloadDir = new File(storePath);
    if (!downloadDir.exists()) {//简单
      if (downloadDir.mkdir()) {//确定文件名
        return downloadFile(netUrl, storePath + File.separator + UPDATE_FILE_NAME);
      } else {
        return new IllegalArgumentException("创建目录失败");
      }
    } else {
      if (downloadDir.isDirectory()) {//确定文件名
        int i;
        for (i = 1; ; i++) {
          if (!new File(storePath + File.separator + "update" + i + ".apk").exists()) {
            break;
          }
        }
        LogUtils.d(this, "确定的下载路径为:" + storePath + File.separator + "update" + i + ".apk");
        return downloadFile(netUrl, storePath + File.separator + "update" + i + ".apk");
      } else {
        LogUtils.e(this, "日狗了，居然不是目录");
        if (downloadDir.mkdir()) {//确定文件名
          return downloadFile(netUrl, storePath + File.separator + UPDATE_FILE_NAME);
        } else {
          return new IllegalArgumentException("创建目录失败");
        }
      }
    }
  }

  /**
   * @param netPath 网络地址
   * @param storePath 保存路径
   */
  private Exception downloadFile(String netPath, String storePath) {
    LogUtils.d(this, "开始下载文件，网络地址:" + netPath + "\n" + "本地路径:" + storePath);
    Exception exception = null;
    File file = new File(storePath);
    currentDownloadCompleted = file;
    FileOutputStream outputStream = null;
    InputStream inputStream = null;
    try {
      Request request = new Request.Builder().url(netPath).build();
      Call call = mOkHttpClient.newCall(request);
      Response response = call.execute();
      if (response.isSuccessful()) {//执行下载操作
        ResponseBody responseBody = response.body();
        inputStream = responseBody.byteStream();
        outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int len;
        int time = 0;
        if (mProgressCallback != null) {
          final long totalSize = responseBody.contentLength();
          long sum = 0;
          while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
            sum += len;
            if (time == 500 || sum == totalSize) {
              mProgressCallback.progressCallback((sum * 1.0f / totalSize) * 100);
              time = 0;
              continue;
            }
            time++;
          }
        } else {
          while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
          }
        }
      } else {
        LogUtils.e(this, "请求下载失败,错误码:" + response.code());
        deleteFileWhenFail(file);
        currentDownloadCompleted = null;
        exception = new IllegalArgumentException("文件下载失败");
      }
    } catch (IOException ioException) {
      exception = ioException;
      deleteFileWhenFail(file);
      currentDownloadCompleted = null;
      LogUtils.e(this, "写入文件发生错误:" + ioException.getMessage());
    } finally {
      LogUtils.d(this, "关闭IO");
      CloseQuietly(inputStream, outputStream);
    }
    return exception;
  }

  private void deleteFileWhenFail(File file) {
    if (file.exists()) {
      file.delete();
    }
  }

  public void setNetUrl(String netUrl) {
    this.netUrl = netUrl;
  }

  private void CloseQuietly(Closeable... closeable) {

    try {
      for (Closeable closeable1 : closeable) {
        if (closeable1 != null) closeable1.close();
      }
    } catch (IOException io) {
      LogUtils.e(this, "关闭文件发生错误：" + io.getMessage());
    }
  }
}
