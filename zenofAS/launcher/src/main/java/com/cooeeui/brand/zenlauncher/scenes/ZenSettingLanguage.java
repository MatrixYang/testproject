package com.cooeeui.brand.zenlauncher.scenes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class ZenSettingLanguage extends BaseActivity
    implements View.OnClickListener, View.OnLongClickListener {

    private static final String DOWNLOAD_URL = "http://nanohome.cn/launcher/language/";

    private RelativeLayout[] mLayout = new RelativeLayout[StringUtil.LAN_COUNT];
    private int[] mId = new int[StringUtil.LAN_COUNT];
    private ZenLanguageImage[] mImage = new ZenLanguageImage[StringUtil.LAN_COUNT];
    private boolean[] mIsDownload = new boolean[StringUtil.LAN_COUNT];
    private int mCurLanguage;
    private static int mCurDownload = -1;
    private static boolean isStop;
    private TextView mTitle;
    private int mLongLan;
    private boolean isPop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPop = getIntent().getBooleanExtra("pop", false);

        if (!isPop) {
            setTheme(android.R.style.Theme_NoTitleBar);
        }

        setContentView(R.layout.zen_setting_language);

        mTitle = (TextView) findViewById(R.id.zs_titlebarTitle);

        if (isPop) {
            int width = DeviceUtils.getScreenPixelsWidth(this);
            int height = DeviceUtils.getScreenPixelsHeight(this);
            Window window = getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = width;
            params.height = height / 2;
            window.setAttributes(params);
            window.setGravity(Gravity.BOTTOM);

            TextView popOk = (TextView) findViewById(R.id.language_pop_ok);
            popOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_OK);
                    ZenSettingLanguage.this.finish();
                }
            });
            findViewById(R.id.zen_setting_back).setVisibility(View.GONE);
            findViewById(R.id.zs_titlebar).setBackgroundColor(
                getResources().getColor(R.color.blue));
            mTitle.setText(getResources().getString(R.string.language_title));
        } else {
            findViewById(R.id.language_pop_ok).setVisibility(View.GONE);
            FrameLayout backArrow = (FrameLayout) findViewById(R.id.zen_setting_back);
            backArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ZenSettingLanguage.this.finish();
                }
            });
            mTitle.setText(StringUtil.getString(this, R.string.zs_language));
        }

        findViewById(R.id.zen_setting_fivestar).setVisibility(View.GONE);

        mLayout[StringUtil.LAN_US] = (RelativeLayout) findViewById(R.id.rl_language_us);
        mLayout[StringUtil.LAN_RU] = (RelativeLayout) findViewById(R.id.rl_language_ru);
        mLayout[StringUtil.LAN_PT] = (RelativeLayout) findViewById(R.id.rl_language_pt);
        mLayout[StringUtil.LAN_IT] = (RelativeLayout) findViewById(R.id.rl_language_it);
        mLayout[StringUtil.LAN_FR] = (RelativeLayout) findViewById(R.id.rl_language_fr);
        mLayout[StringUtil.LAN_DE] = (RelativeLayout) findViewById(R.id.rl_language_de);
        mLayout[StringUtil.LAN_ES] = (RelativeLayout) findViewById(R.id.rl_language_es);
        mLayout[StringUtil.LAN_IN] = (RelativeLayout) findViewById(R.id.rl_language_in);
        mLayout[StringUtil.LAN_TR] = (RelativeLayout) findViewById(R.id.rl_language_tr);
        mLayout[StringUtil.LAN_PL] = (RelativeLayout) findViewById(R.id.rl_language_pl);
        mLayout[StringUtil.LAN_CN] = (RelativeLayout) findViewById(R.id.rl_language_cn);
        mLayout[StringUtil.LAN_TW] = (RelativeLayout) findViewById(R.id.rl_language_tw);
        mLayout[StringUtil.LAN_AR] = (RelativeLayout) findViewById(R.id.rl_language_ar);
        mLayout[StringUtil.LAN_EL] = (RelativeLayout) findViewById(R.id.rl_language_el);
        mLayout[StringUtil.LAN_RO] = (RelativeLayout) findViewById(R.id.rl_language_ro);
        mLayout[StringUtil.LAN_CS] = (RelativeLayout) findViewById(R.id.rl_language_cs);

        mImage[StringUtil.LAN_US] = (ZenLanguageImage) findViewById(R.id.image_us);
        mImage[StringUtil.LAN_RU] = (ZenLanguageImage) findViewById(R.id.image_ru);
        mImage[StringUtil.LAN_PT] = (ZenLanguageImage) findViewById(R.id.image_pt);
        mImage[StringUtil.LAN_IT] = (ZenLanguageImage) findViewById(R.id.image_it);
        mImage[StringUtil.LAN_FR] = (ZenLanguageImage) findViewById(R.id.image_fr);
        mImage[StringUtil.LAN_DE] = (ZenLanguageImage) findViewById(R.id.image_de);
        mImage[StringUtil.LAN_ES] = (ZenLanguageImage) findViewById(R.id.image_es);
        mImage[StringUtil.LAN_IN] = (ZenLanguageImage) findViewById(R.id.image_in);
        mImage[StringUtil.LAN_TR] = (ZenLanguageImage) findViewById(R.id.image_tr);
        mImage[StringUtil.LAN_PL] = (ZenLanguageImage) findViewById(R.id.image_pl);
        mImage[StringUtil.LAN_CN] = (ZenLanguageImage) findViewById(R.id.image_cn);
        mImage[StringUtil.LAN_TW] = (ZenLanguageImage) findViewById(R.id.image_tw);
        mImage[StringUtil.LAN_AR] = (ZenLanguageImage) findViewById(R.id.image_ar);
        mImage[StringUtil.LAN_EL] = (ZenLanguageImage) findViewById(R.id.image_el);
        mImage[StringUtil.LAN_RO] = (ZenLanguageImage) findViewById(R.id.image_ro);
        mImage[StringUtil.LAN_CS] = (ZenLanguageImage) findViewById(R.id.image_cs);

        for (int i = 0; i < StringUtil.LAN_COUNT; i++) {
            mLayout[i].setOnClickListener(this);
            mLayout[i].setOnLongClickListener(this);
            mId[i] = mLayout[i].getId();
            if (i == 0) {
                mIsDownload[0] = true;
            } else {
                mIsDownload[i] = isDownload(StringUtil.getDir(i));
            }
            if (mIsDownload[i]) {
                mImage[i].setState(ZenLanguageImage.DOWNLOADED);
            }
        }

        mCurLanguage = SettingPreference.getZenLanguage();
        mImage[mCurLanguage].setState(ZenLanguageImage.SELECTED);

        if (mCurDownload != -1) {
            mIsDownload[mCurDownload] = false;
            mImage[mCurDownload].setState(ZenLanguageImage.DOWNLOAD);
            mCurDownload = -1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
    }

    private void showAlert() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.alert);
        window.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = (TextView) window.findViewById(R.id.alert_title);
        title.setText(StringUtil.getString(this, R.string.tips_title));
        TextView text = (TextView) window.findViewById(R.id.alter_text);
        text.setText(StringUtil.getString(this, R.string.zs_alert));
        TextView ok = (TextView) window.findViewById(R.id.alert_ok);
        ok.setText(StringUtil.getString(this, R.string.text_delete));
        TextView cancel = (TextView) window.findViewById(R.id.alert_cancel);
        cancel.setText(StringUtil.getString(this, R.string.alter_cancel));
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                removeFile(mLongLan);
                alertDialog.cancel();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private static final int DOWNLOAD_SUCCESS = 0;
    private static final int DOWNLOAD_FAIL = 1;

    private Handler mHandler = new MyHandler(this);

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
    private class MyHandler extends Handler {

        private final WeakReference<ZenSettingLanguage> mOuter;

        public MyHandler(ZenSettingLanguage outer) {
            mOuter = new WeakReference<ZenSettingLanguage>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            ZenSettingLanguage outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case DOWNLOAD_SUCCESS:
                        if (mCurDownload != -1) {
                            mImage[mCurDownload].setState(ZenLanguageImage.DOWNLOADED);
                            mCurDownload = -1;
                        }
                        if (!isPop) {
                            Toast.makeText(ZenSettingLanguage.this, StringUtil.getString(
                                               ZenSettingLanguage.this, R.string.zs_long_delete),
                                           Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case DOWNLOAD_FAIL:
                        if (mCurDownload != -1) {
                            mImage[mCurDownload].setState(ZenLanguageImage.DOWNLOAD);
                            mCurDownload = -1;
                        }
                        Toast.makeText(ZenSettingLanguage.this, StringUtil.getString(
                                           ZenSettingLanguage.this, R.string.zs_download_fail),
                                       Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    private void removeFile(String name) {
        File file = new File(getFilesDir().getAbsolutePath() + name + StringUtil.FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    private void downloadXml(final String name) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    download(name);
                    if (isStop) {
                        removeFile(name);
                        return;
                    }
                    mIsDownload[mCurDownload] = true;
                    mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                } catch (Exception e) {
                    removeFile(name);
                    mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setLanguageUs() {
        StringUtil.clearMap();
        mCurLanguage = StringUtil.LAN_US;
        SettingPreference.setZenLanguage(mCurLanguage);
        Launcher.isLanguageChanged = true;
        mImage[mCurLanguage].setState(ZenLanguageImage.SELECTED);
        if (isPop) {
            mTitle.setText(getResources().getString(R.string.language_title));
        } else {
            mTitle.setText(StringUtil.getString(this, R.string.zs_language));
        }
    }

    private void doClick(int lan) {
        if (lan == StringUtil.LAN_US) {
            mImage[mCurLanguage].setState(ZenLanguageImage.DOWNLOADED);
            setLanguageUs();
        } else if (mIsDownload[lan]) {
            mImage[mCurLanguage].setState(ZenLanguageImage.DOWNLOADED);
            StringUtil.loadXml(this, lan);
            mCurLanguage = lan;
            SettingPreference.setZenLanguage(lan);
            Launcher.isLanguageChanged = true;
            mImage[lan].setState(ZenLanguageImage.SELECTED);
            if (isPop) {
                mTitle.setText(getResources().getString(R.string.language_title));
            } else {
                mTitle.setText(StringUtil.getString(this, R.string.zs_language));
            }
            //选择除英语以外的语言次数
            MobclickAgent.onEvent(this, "Chooselanguage");
        } else if (mCurDownload == -1) {
            mCurDownload = lan;
            isStop = false;
            mImage[lan].setProgress(10);
            mImage[lan].setState(ZenLanguageImage.DOWNLOADING);
            downloadXml(StringUtil.getDir(lan));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        for (int i = 0; i < StringUtil.LAN_COUNT; i++) {
            if (id == mId[i]) {
                if (mCurLanguage == i) {
                    break;
                }
                doClick(i);
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mCurDownload != -1) {
            return true;
        }
        int id = v.getId();
        for (int i = 0; i < StringUtil.LAN_COUNT; i++) {
            if (id == mId[i] && i != StringUtil.LAN_US) {
                if (mIsDownload[i]) {
                    mLongLan = i;
                    showAlert();
                }
                break;
            }
        }
        return true;
    }

    private void removeFile(int lan) {
        String path = getFilesDir().getAbsolutePath() + StringUtil.getDir(lan)
                      + StringUtil.FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            mIsDownload[lan] = false;
            mImage[lan].setState(ZenLanguageImage.DOWNLOAD);
            if (mCurLanguage == lan) {
                setLanguageUs();
            }
            Toast.makeText(this, StringUtil.getString(this, R.string.zs_delete),
                           Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDownload(String name) {
        String path = getFilesDir().getAbsolutePath() + name + StringUtil.FILE_NAME;
        File file = new File(path);

        return file.exists();
    }

    private void download(String name) throws Exception {
        String dir = getFilesDir().getAbsolutePath() + name;
        File path = new File(dir);
        if (!path.exists()) {
            path.mkdirs();
        }

        File file = new File(dir + StringUtil.FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        String urlString = DOWNLOAD_URL + name + StringUtil.FILE_NAME;
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.connect();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Not Ok");
        }
        int contentLength = con.getContentLength();
        InputStream is = con.getInputStream();
        int hasRead = 0;
        int progress;
        int len;
        byte[] buff = new byte[1024];
        OutputStream os = new FileOutputStream(file);
        while ((len = is.read(buff)) != -1) {
            if (isStop) {
                os.close();
                is.close();
                con.disconnect();
                file.delete();
                return;
            }
            os.write(buff, 0, len);
            hasRead += len;
            progress = (int) ((double) hasRead / (double) contentLength * 100);
            if (mCurDownload >= 0) {
                mImage[mCurDownload].setProgress(progress);
                mImage[mCurDownload].postInvalidate();
            }
        }
        os.flush();
        os.close();
        is.close();
        con.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStop = true;
    }
}
