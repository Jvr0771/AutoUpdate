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
    this.storePath = dirPath.getAbsolutePath();
  }

  @Override protected void onPostExecute(Exception aVoid) {
    super.onPostExecute(aVoid);
    if (aVoid == null) {
      if (mProgressCallback != null && currentDownloadCompleted != null) {
        mProgressCallback.downloadCompleted(currentDownloadCompleted);
        currentDownloadCompleted = null;
      }
    } else {
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
    File downloadDir = new File(storePath);
    if (!downloadDir.exists()) {
      if (downloadDir.mkdir()) {
        return downloadFile(netUrl, storePath + File.separator + UPDATE_FILE_NAME);
      } else {
        return new IllegalArgumentException("创建目录失败");
      }
    } else {
      if (downloadDir.isDirectory()) {
        int i;
        for (i = 1; ; i++) {
          if (!new File(storePath + File.separator + "update" + i + ".apk").exists()) {
            break;
          }
        }
        return downloadFile(netUrl, storePath + File.separator + "update" + i + ".apk");
      } else {
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
    Exception exception = null;
    File file = new File(storePath);
    currentDownloadCompleted = file;
    FileOutputStream outputStream = null;
    InputStream inputStream = null;
    try {
      Request request = new Request.Builder().url(netPath).build();
      Call call = mOkHttpClient.newCall(request);
      Response response = call.execute();
      if (response.isSuccessful()) {
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
        deleteFileWhenFail(file);
        currentDownloadCompleted = null;
        exception = new IllegalArgumentException("文件下载失败");
      }
    } catch (IOException ioException) {
      exception = ioException;
      deleteFileWhenFail(file);
      currentDownloadCompleted = null;
    } finally {
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
      io.printStackTrace();
    }
  }
}
