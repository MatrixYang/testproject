package com.cooeeui.brand.zenlauncher.http;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;

import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 传入URL即可在notification处进行下载，默认保存在sdcard的download目录下，文件名保持一致
 *
 * @author Steve
 */
@SuppressLint("NewApi")
public class NotificationDownloadTask extends AsyncTask<String, Integer, String> {

    public static final int PREPARE = 11;
    public static final int COMPLETED = 22;
    public static final int UPDATEPROGRESS = 33;
    final String path = Environment.getExternalStorageDirectory().getPath() + "/Download/";

    private Context mContext;
    private NotificationManager mNotificationManager;
    private Notification.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private PendingIntent contentIntent;
    private Notification notification;
    // 指定ID可以重复产生notification
    private int notificationID = 11;
    private String title = "Title";
    private String filename;
    private int fileSize;
    private int downLoadFileSize;
    private FileOutputStream fos;
    private int iconID = 0;
    private String tip_download = "Download";

    public NotificationDownloadTask(Context context, int id, String title, int srcId) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationID = id;
        this.title = title;
        this.iconID = srcId;
        this.tip_download = StringUtil.getString(mContext, R.string.alter_download);
        mBuilder = new Notification.Builder(mContext);
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_layout);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // 进度条更新
        mRemoteViews.setProgressBar(R.id.progressbar_progress, fileSize, downLoadFileSize, false);
        int i = downLoadFileSize * 100 / fileSize;
        mRemoteViews.setTextViewText(R.id.textview_progress, tip_download + ":" + i + "%");
        mRemoteViews.setImageViewResource(R.id.image_icon, iconID);
        notification.contentView = mRemoteViews;
        mNotificationManager.notify(notificationID, notification);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBuilder.setSmallIcon(iconID)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false);
        notification = mBuilder.build();
        mRemoteViews.setTextViewText(R.id.textview_title, title);
        notification.contentView = mRemoteViews;
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        // 加i是为了显示多条Notification
        mNotificationManager.notify(notificationID, notification);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String url = params[0];
            // 获取文件名
            filename = url.substring(url.lastIndexOf("/") + 1);
            down_file(url, path + filename);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * 下载文件函数
     *
     * @param url  需要被下载的文件的地址
     * @param path 文件需要被存储的完整路径
     */
    public void down_file(String url, String path) throws IOException {
        int times = 0;
        URL downloadURL = new URL(url);
        URLConnection conn = downloadURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        this.fileSize = conn.getContentLength();// 根据响应获取文件大小
        if (this.fileSize <= 0) {
            throw new RuntimeException("无法获知文件大小 ");
        }
        if (is == null) {
            throw new RuntimeException("stream is null");
        }
        fos = new FileOutputStream(path);
        // 把数据存入路径+文件名
        byte buf[] = new byte[1024];

        downLoadFileSize = 0;
        sendMsg(PREPARE);
        do {
            // 循环读取
            int numread = is.read(buf);
            if (numread == -1) {
                break;
            }
            fos.write(buf, 0, numread);
            downLoadFileSize += numread;
            // 每下载10KB进行一次刷新，避免频繁刷新
            if ((times == 10) || (downLoadFileSize == fileSize)) {
                publishProgress(downLoadFileSize);
                times = 0;
            }
            times++;
        } while (true);
        try {
            fos.flush();
            fos.close();
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        /**
         * 点击安装
         */
        File apkFile = new File(path + filename);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        // 点击执行的intent
        notification.contentIntent = contentIntent;
        mNotificationManager.notify(notificationID, notification);
    }

    private void sendMsg(int what) {
        mHandler.obtainMessage(what).sendToTarget();
    }

    // 还没开始用
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case PREPARE:
                    mRemoteViews.setProgressBar(R.id.progressbar_progress, fileSize, 0, false);
                    break;
                case COMPLETED:

                    break;
                case UPDATEPROGRESS:
                    break;
            }
            super.handleMessage(message);
        }
    };

}
