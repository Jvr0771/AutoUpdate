package work.wanghao.autoupdate.callback;

import java.io.File;

/**
 * Create on: 2016-07-30
 * Author: wangh
 * Summary: TODO
 */
public interface DownloadProgressCallback {

  void progressCallback(double progress);

  void downloadFail(Exception exception);

  void downloadCompleted(File file);
}
