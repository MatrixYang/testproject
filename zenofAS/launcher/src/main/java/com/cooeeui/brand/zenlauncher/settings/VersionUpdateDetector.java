package com.cooeeui.brand.zenlauncher.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.AlertDialogUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hugo.ye on 2016/1/27.
 */
public class VersionUpdateDetector {

    private static final int HAVE_UPDATE = 0;
    private static final int NO_UPDATE = 1;
    private static final int FAIL_UPDATE = 2;

    private final String VERSION_NAME_URL = "http://nanohome.cn/launcher/version/version.php";
    private final String VERSION_INFO_URL = "http://nanohome.cn/launcher/version/versioninfo.php";

    private Context mContext;
    private final Handler mHandler = new UpdateHandler(this);
    private boolean mShowWaitDialog;
    private DialogUtil mWaitDialog;
    private String mRemoteVersionName;
    private String mVersionInfo;
    private boolean mAutoDetector;
    private boolean mJustAlert;
    private AlertDialogUtil mAlertDialog;

    public VersionUpdateDetector(Context context) {
        this.mContext = context;
        mAlertDialog = new AlertDialogUtil((Activity) context);
    }

    public void setmAutoDetector(boolean auto) {
        mAutoDetector = auto;
    }

    public void checkUpdate(boolean showWaitDialog, boolean justAlert) {

        // 手动点击更新的话，清除掉之前的状态
        if (!mAutoDetector) {
            LauncherPreference.setVersionUpdateForgetStatus(false);
        }

        mRemoteVersionName = LauncherPreference.getRemoteVersionName();
        mVersionInfo = LauncherPreference.getVersionInfo();

        // 如果当前版本已经更新到最新版本，清除通知栏通知消息
        if (mRemoteVersionName != null) {
            if (mRemoteVersionName.equals(CommonUtil.getVersionName(mContext))) {
                VersionUpdateNotification.clearNotification(mContext);
            }
        }

        // 如果更新提示框已经显示，则返回
        if (mAlertDialog != null && mAlertDialog.isAlertDialogShowing()) {
            return;
        }

        mShowWaitDialog = showWaitDialog;
        mJustAlert = justAlert;

        // 如果是通过通知栏消息进入的检测，则直接弹出提示框，不再进行联网操作
        if (mJustAlert && mRemoteVersionName != null && mVersionInfo != null) {
            if (mAlertDialog != null) {
                mAlertDialog.showAlertDialog(false, false,
                                             AlertDialogUtil.AlertDialogType.TYPE_VERSION_UPDATE,
                                             R.layout.alter_dialog_update);
                mAlertDialog.fillUpdateVersionDetail(mRemoteVersionName, mVersionInfo);
                //自更新弹框弹出次数
                MobclickAgent.onEvent(mContext, "Autoupdatepop");
                return;
            }
        }

        // 是否需要联网等待提示loading
        if (mShowWaitDialog) {
            if (mWaitDialog == null) {
                mWaitDialog = new DialogUtil(mContext);
                mWaitDialog.showLoadingDialog(true);
            }
        }

        // 开启线程联网
        ThreadUtil.execute(mUpdateCheckRunnable);
    }


    private final Runnable mUpdateCheckRunnable = new Runnable() {
        @Override
        public void run() {

            if (mAlertDialog != null && mAlertDialog.isAlertDialogShowing()) {
                return;
            }

            InputStream in = null;
            try {
                // 获取服务器上版本号
                URL url = new URL(VERSION_NAME_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);
                urlConnection.setConnectTimeout(10 * 1000);
                urlConnection.setReadTimeout(10 * 1000);
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Charset", "UTF-8");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

                urlConnection.connect();
                in = urlConnection.getInputStream();
                String remoteVersionJson = CommonUtil.inputStream2String(in);

//                // temp test begin
//                if (remoteVersionJson.equals("")) {
//                    remoteVersionJson = "1.7.1";
//                }
//                // temp end

                JSONObject jsonObject = new JSONObject(remoteVersionJson);
                mRemoteVersionName = jsonObject.getString("version");
                LauncherPreference.setRemoteVersionName(mRemoteVersionName);
                int remoteVersion = CommonUtil.versionNameString2Int(mRemoteVersionName);
                int localVersion = CommonUtil.getVersion(mContext);
                boolean getVersionInfo = false;

                if (mAutoDetector && LauncherPreference.getVersionUpdateForgetStatus()) {
                    int forgotVersion = LauncherPreference.getVersionUpdateForgotVersion();
                    if (remoteVersion > forgotVersion) {
                        getVersionInfo = true;
                        LauncherPreference.setVersionUpdateForgetStatus(false);
                    } else {
                        getVersionInfo = false;
                    }
                } else if (remoteVersion > localVersion) {
                    getVersionInfo = true;
                }

                if (getVersionInfo) {
                    // 获取具体版本更新信息
                    url = new URL(VERSION_INFO_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("Charset", "UTF-8");
                    urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

                    urlConnection.connect();
                    in = urlConnection.getInputStream();
                    remoteVersionJson = CommonUtil.inputStream2String(in);
                    jsonObject = new JSONObject(remoteVersionJson);
                    mVersionInfo = jsonObject.getString("data");
                    LauncherPreference.setVersionInfo(mVersionInfo);
                    mHandler.sendEmptyMessage(HAVE_UPDATE);
                } else {
                    if (!mAutoDetector) {
                        mHandler.sendEmptyMessage(NO_UPDATE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!mAutoDetector) {
                    mHandler.sendEmptyMessage(FAIL_UPDATE);
                }

            } finally {

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };


    /**
     * 采用内部Handler类来更新UI，避免内存泄露
     *
     * Handler mHandler = new Handler() { public void handleMessage(Message msg) {
     * mImageView.setImageBitmap(mBitmap); } }
     *
     * 上面是一段简单的Handler的使用。当使用内部类（包括匿名类）来创建Handler的时候，Handler对象会隐式地持有一个外部类对象（
     * 通常是一个Activity）的引用（不然你怎么可能通过Handler来操作Activity中的View？）。 而Handler通常会伴随着一个耗时的后台线程（例如从网络拉取图片）一起出现，
     * 这个后台线程在任务执行完毕（例如图片下载完毕）之后，通过消息机制通知Handler，然后Handler把图片更新到界面。 然而，如果用户在网络请求过程中关闭了Activity，正常情况下，Activity不再被使用，它就有可能在GC检查时被回收掉，
     * 但由于这时线程尚未执行完，而该线程持有Handler的引用（不然它怎么发消息给Handler？）， 这个Handler又持有Activity的引用，
     * 就导致该Activity无法被回收（即内存泄露），直到网络请求结束（例如图片下载完毕）。 另外，如果你执行了Handler的postDelayed()方法，该方法会将你的Handler装入一个Message，并把这条Message推到MessageQueue中，
     * 那么在你设定的delay到达之前，会有一条MessageQueue -> Message -> Handler -> Activity的链，导致你的Activity被持有引用而无法被回收。
     */
    private class UpdateHandler extends Handler {

        private final WeakReference<VersionUpdateDetector> mOuter;

        public UpdateHandler(VersionUpdateDetector outer) {
            mOuter = new WeakReference<VersionUpdateDetector>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            VersionUpdateDetector outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case HAVE_UPDATE:
                        if (mWaitDialog != null && mWaitDialog.isShowing()) {
                            mWaitDialog.cancelLoadingDialog();
                            mWaitDialog = null;
                        }
                        if (mAlertDialog != null && !mAlertDialog.isAlertDialogShowing()) {
                            mAlertDialog.showAlertDialog(false,false,
                                                         AlertDialogUtil.AlertDialogType.TYPE_VERSION_UPDATE,
                                                         R.layout.alter_dialog_update);
                            mAlertDialog.fillUpdateVersionDetail(
                                mRemoteVersionName, mVersionInfo);
                            VersionUpdateNotification.showNotification(mContext);
                            //自更新弹框弹出次数
                            MobclickAgent.onEvent(mContext, "Autoupdatepop");
                        }

                        break;
                    case NO_UPDATE:
                        if (mWaitDialog != null && mWaitDialog.isShowing()) {
                            mWaitDialog.cancelLoadingDialog();
                            mWaitDialog = null;
                        }
                        Toast.makeText(mContext, StringUtil
                                           .getString(outer.mContext, R.string.update_is_newest),
                                       Toast.LENGTH_SHORT).show();
                        break;
                    case FAIL_UPDATE:
                        if (mWaitDialog != null && mWaitDialog.isShowing()) {
                            mWaitDialog.cancelLoadingDialog();
                            mWaitDialog = null;
                        }
                        Toast.makeText(mContext,
                                       StringUtil.getString(outer.mContext, R.string.update_fail),
                                       Toast.LENGTH_SHORT).show();
                        break;
                }
            }

        }
    }
}
