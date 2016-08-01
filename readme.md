#### 简介
 
 这是一个基于Fir检测更新封装的自动更新模块，以便于在项目中完成检测更新的快速集成。
  
#### 依赖

本封装的正常工作依赖于Fir的模块集成，如果你不知道怎么集成Fir到你的项目中，请移步:[http://bughd.com/doc/android](http://bughd.com/doc/android)

#### 使用方式

##### 1.添加仓库

在项目的根目录`build.gradle`中添加以下脚本内容:

```gradle
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
			maven {
            url "http://maven.bughd.com/public"
          }
		}
	}
```

##### 2.添加依赖

在你的项目的app moudle的`build.gradle`中添加以下依赖:

```gradle
  compile 'im.fir:fir-sdk:latest.integration@aar'
  compile 'com.squareup.okhttp3:okhttp:3.4.1'
  compile 'com.github.Doublemine:AutoUpdate:0.1.0_apha'
```

注意`com.github.Doublemine:AutoUpdate:0.1.0_apha`的版本，当前最新的版本为: [![](https://jitpack.io/v/Doublemine/AutoUpdate.svg)](https://jitpack.io/#Doublemine/AutoUpdate)

##### 3.代码中使用

 - 本封装下载的更新文件位于SDCard上的应用缓存文件夹中,因此在Android SDK 23+以上你`不需要`额外的添加运行时权限来保证它的正常运行:


 - 在Activity或者Fragment中你想要自动更新的地方调用以下代码即可完成检测自动更新:
 
 ```java
 UpdateManager.checkUpdate(this);
 ```
 
 同时，请在`AndroidManifest.xml`文件中声明以下meta信息:
 
 ```xml
 <meta-data
         android:name="BUG_HD_API_TOKEN"
         android:value="{API_TOKEN}"/>
 ```
 
 其中的value即为你的Fir Api Token 查看你的api token [请点我](http://fir.im/apps/apitoken)
 
 
 如果你觉得这种方式麻烦，也可以使用以下重载方法，其中`apiToken`即为你的Fir Api Token
 
 ```java
  UpdateManager.checkUpdate(this,apiToken);
 ```
 
 #### 自定义
 
 本封装默认使用dialog提示用户安装包的升级信息，如果想自定义你的提示dialog，可以继承自`BaseUpdateService`重写其中的方法来完成Dialog的自定义。具体参照以下写法以及todo注释:
 
 ```java
  @Override
   protected Dialog setInstallTipsDialog(FirVersionInfo info, final String installApkPath) {
     final AlertDialog.Builder builder =
         new AlertDialog.Builder(this, R.style.AlertDialog_AppCompat_Dialog);
     builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
       @Override public void onClick(DialogInterface dialogInterface, int i) {
 
         // TODO:  请必须调用此父类方法以让父类能够取消下载   
         onCancelInstallClick(null, dialogInterface);
       }
     })
         .setPositiveButton("安装", new DialogInterface.OnClickListener() {
           @Override public void onClick(DialogInterface dialogInterface, int i) {
 
             // TODO: 请必须调用此父类方法以让父类能够执行安装逻辑 
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
 
     return builder.create();/*todo:必须返回dialog对象*/
   }
 
   @Override protected Dialog setDialogContent(final FirVersionInfo info) {
     final AlertDialog.Builder builder =
         new AlertDialog.Builder(this, R.style.AlertDialog_AppCompat_Dialog);
     builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
       @Override public void onClick(DialogInterface dialogInterface, int i) {
 
         // TODO: 请务必调用此方法以让父类讷讷够取消下载  
         onCancelDownloadClick(null, dialogInterface);
       }
     })
         .setPositiveButton("更新", new DialogInterface.OnClickListener() {
           @Override public void onClick(DialogInterface dialogInterface, int i) {
 
             // TODO: 请必须调用此父类方法以让父类能够执行下载
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
     return builder.create();/*todo:必须返回dialog对象*/
   }
 ```