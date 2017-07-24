package com.cooeeui.mytest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import com.cooeeui.downloader.api.DLManager;
import com.cooeeui.downloader.core.interfaces.SimpleDListener;
import com.cooeeui.nanoqrcodescan.R;

/**
 * Created by Hugo.ye on 2016/3/22.
 */
public class MyTestActivity extends Activity {

    private static final String DOWNLOAD_URL = "http://www.coolauncher.cn/nano/apk/NanoIconPKG.apk";
    final String path = Environment.getExternalStorageDirectory().getPath() + "/NanoLauncher/App/";

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_test_layout);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_download);
        mProgressBar.setMax(100);
//        DLManager.getInstance(MyTestActivity.this).setMaxTask(2);
    }

    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.bt_dl_start:
                DLManager.getInstance(MyTestActivity.this).
                    dlStart(DOWNLOAD_URL, path,
                            null, null,
                            new SimpleDListener() {
                                @Override
                                public void onStart(String fileName, String realUrl,
                                                    int fileLength) {
                                    mProgressBar.setMax(fileLength);
                                }

                                @Override
                                public void onProgress(int progress) {
                                    mProgressBar.setProgress(progress);
                                }
                            });
                break;
            case R.id.bt_dl_stop:
                DLManager.getInstance(MyTestActivity.this).dlStop(DOWNLOAD_URL);
                break;
            case R.id.bt_dl_cancel:
                DLManager.getInstance(MyTestActivity.this).dlCancel(DOWNLOAD_URL);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        DLManager.getInstance(this).dlStop(DOWNLOAD_URL);

        super.onDestroy();
    }
}
