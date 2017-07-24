package com.cooeeui.downloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.cooeeui.downloader.api.DLManager;
import com.cooeeui.downloader.core.interfaces.IDListener;
import com.cooeeui.nanoqrcodescan.R;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Hugo.ye on 2016/3/23. Singleton
 */
public class NotificationForDLApk {

    private static final String TAG = NotificationForDLApk.class.getSimpleName();
    private final boolean DEBUG = false;

    private static final String DOWN_LOAD_PATH =
        Environment.getExternalStorageDirectory().getPath() + "/NanoLauncher/App/";

    private static final String ACTION_BUTTON =
        "com.cooeeui.notifications.intent.action.ButtonClick";
    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    public final static int BUTTON_CONTINUE_ID = 1; // 继续下载
    public final static int BUTTON_PAUSE_ID = 2;    // 暂停下载
    public final static int BUTTON_CANCEL_ID = 3;   // 取消下载

    private static NotificationForDLApk sInstance;

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private Notification mNotification;
    private String mDownloadPath;
    private String mUrl;
    private HashMap<String, Integer> mDownloadMap = new HashMap<>();
    private ButtonBroadcastReceiver mBtnReceiver;
    private final int mButtonLeftViewId = R.id.notification_left_button;
    private final int mButtonRightViewId = R.id.notification_right_button;

    private NotificationForDLApk(Context context) {
        this.mContext = context;
        mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.downloader_notification_warn_icon) // 需要改
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setOngoing(true)
            .setTicker(context.getResources().getString(R.string.app_name));// 需要改
        mNotification = mBuilder.build();
        mRemoteViews =
            new RemoteViews(context.getPackageName(), R.layout.downloader_notification_layout);
        mNotification.contentView = mRemoteViews;
    }

    public static NotificationForDLApk getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NotificationForDLApk(context);
        }
        return sInstance;
    }

    public void launchDLNotification(String url, String path) {

        if (path == null || TextUtils.isEmpty(path)) {
            path = DOWN_LOAD_PATH;
        }
        mDownloadPath = url;
        mUrl = url;
        if (!mDownloadMap.containsKey(url)) {
            mDownloadMap.put(url, (int) (Math.random() * 1024));
        }
        initButtonReceiver();
        mRemoteViews.setViewVisibility(mButtonLeftViewId, View.GONE);
        mRemoteViews.setTextViewText(R.id.notification_dl_status, mContext.getResources()
            .getText(R.string.downloader_noti_downloading));
        registerButtonPause(mButtonRightViewId);
        DLManager.getInstance(mContext).dlStart(url, path, new DownLoadListener());
    }

    public void initButtonReceiver() {
        if (mBtnReceiver == null) {
            mBtnReceiver = new ButtonBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_BUTTON);
            mContext.registerReceiver(mBtnReceiver, intentFilter);
        }
    }

    private void registerButtonContinue(int buttonViewId) {
        mRemoteViews.setViewVisibility(buttonViewId, View.VISIBLE);
        mRemoteViews
            .setImageViewResource(buttonViewId, R.drawable.downloader_notification_continue);

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CONTINUE_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, BUTTON_CONTINUE_ID,
                                                                 buttonIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(buttonViewId, pendingIntent);
    }

    private void registerButtonPause(int buttonViewId) {
        mRemoteViews.setViewVisibility(buttonViewId, View.VISIBLE);
        mRemoteViews.setImageViewResource(buttonViewId, R.drawable.downloader_notification_pause);

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PAUSE_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, BUTTON_PAUSE_ID,
                                                                 buttonIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(buttonViewId, pendingIntent);
    }

    private void registerButtonCancel(int buttonViewId) {
        mRemoteViews.setViewVisibility(buttonViewId, View.VISIBLE);
        mRemoteViews.setImageViewResource(buttonViewId, R.drawable.downloader_notification_cancel);

        Intent buttonIntent = new Intent(ACTION_BUTTON);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_CANCEL_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, BUTTON_CANCEL_ID,
                                                                 buttonIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(buttonViewId, pendingIntent);
    }

    // 广播监听按钮点击事件
    class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                // 通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case BUTTON_CONTINUE_ID:
                        DLManager.getInstance(mContext)
                            .dlStart(mUrl, mDownloadPath, new DownLoadListener());
                        registerButtonPause(mButtonRightViewId);
                        mRemoteViews.setTextViewText(R.id.notification_dl_status,
                                                     mContext.getResources().getText(
                                                         R.string.downloader_noti_downloading));
                        mRemoteViews.setViewVisibility(mButtonLeftViewId, View.GONE);
                        mNotificationManager.notify(mDownloadMap.get(mUrl), mNotification);
                        break;
                    case BUTTON_PAUSE_ID:
                        DLManager.getInstance(mContext).dlStop(mUrl);
                        registerButtonContinue(mButtonRightViewId);
                        registerButtonCancel(mButtonLeftViewId);
                        mRemoteViews.setTextViewText(R.id.notification_dl_status,
                                                     mContext.getResources().getText(
                                                         R.string.downloader_noti_pause));
                        mNotificationManager.notify(mDownloadMap.get(mUrl), mNotification);
                        break;
                    case BUTTON_CANCEL_ID:
                        DLManager.getInstance(mContext).dlCancel(mUrl);
                        mNotificationManager.cancel(mDownloadMap.get(mUrl));
                        if (mBtnReceiver != null) {
                            mContext.unregisterReceiver(mBtnReceiver);
                            mBtnReceiver = null;
                        }
                        break;
                }
            }
        }
    }

    class DownLoadListener implements IDListener {

        private int mFileLength;

        @Override
        public void onPrepare() {
            if (DEBUG) {
                Log.i(TAG, "onPrepare");
            }
        }

        @Override
        public void onStart(String fileName, String realUrl, int fileLength) {
            if (DEBUG) {
                Log.i(TAG, "onStart fileName = " + fileName + " realUrl = " + realUrl
                           + " fileLength = " + fileLength);
            }
            mFileLength = fileLength;
        }

        @Override
        public void onProgress(int progress) {
            if (DEBUG) {
                Log.i(TAG, "onProgress progress = " + progress);
            }
            mRemoteViews
                .setProgressBar(R.id.notification_progressbar, mFileLength, progress, false);
            mNotificationManager.notify(mDownloadMap.get(mUrl), mNotification);
        }

        @Override
        public void onStop(int progress) {
            if (DEBUG) {
                Log.i(TAG, "onStop progress = " + progress);
            }
        }

        @Override
        public void onFinish(File file) {
            if (DEBUG) {
                Log.i(TAG, "onFinish file = " + file);
            }
            registerButtonCancel(mButtonRightViewId);
            Intent intent = launchInstallAction(file);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            mNotification.contentIntent = pendingIntent;
            mRemoteViews.setTextViewText(R.id.notification_dl_status, mContext.getResources().
                getText(R.string.downloader_noti_done));
            mNotificationManager.notify(mDownloadMap.get(mUrl), mNotification);
        }

        @Override
        public void onError(int status, String error) {
            if (DEBUG) {
                Log.i(TAG, "onError error = " + error);
            }
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
        }
    }

    private Intent launchInstallAction(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
        return intent;
    }

}
