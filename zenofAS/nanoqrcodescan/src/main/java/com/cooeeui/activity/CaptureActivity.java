package com.cooeeui.activity;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsBaseNew;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooeeui.ad.KmobAdNativeData;
import com.cooeeui.camera.CameraManager;
import com.cooeeui.decoding.CaptureActivityHandler;
import com.cooeeui.decoding.InactivityTimer;
import com.cooeeui.decoding.RGBLuminanceSource;
import com.cooeeui.nanoqrcodescan.R;
import com.cooeeui.scan.DBHelper;
import com.cooeeui.scan.DialogScanDetail;
import com.cooeeui.scan.HistoryActivity;
import com.cooeeui.scan.HistoryBean;
import com.cooeeui.statistics.Config;
import com.cooeeui.utils.CommonUtil;
import com.cooeeui.utils.Constant;
import com.cooeeui.utils.ThreadUtil;
import com.cooeeui.view.ViewfinderView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.kmob.kmobsdk.NativeAdData;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.Frame;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.umeng.message.PushAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class CaptureActivity extends Activity implements Callback, OnClickListener {

    public static int screenWidth;
    public static int screenHeight;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ToggleButton tb_flashlight;
    private ImageButton btn_history;
    private ImageButton btn_album;
    private DBHelper dbHelper;
    private List<HistoryBean> histroyList;
    private boolean isBackChoose = false;

    // Cooee 统计 begin
    public static final String SP_FILE_NAME = "cooee_statistic";
    public static final String SP_KEY_FIRST_RUN = "first_run";
    private String sn;
    private String appid;
    private String shellid;
    private int producttype = 3;
    private String opversion;
    // Cooee 统计 end

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.CAMERA)
                  == PackageManager.PERMISSION_GRANTED)) {
                requestCameraPermission();
            } else {
                allowCamera = true;
                init();
            }
        } else {
            allowCamera = true;
            init();
        }

        adInit();

        // Cooee 统计 begin
        initCooeeStatistics();
        // Cooee 统计 begin
    }

    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;
    private boolean allowCamera = false;

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                               REQUEST_PERMISSION_CAMERA_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA_CODE) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            allowCamera = granted;
            if (granted) {
                init();
                resume();
            } else {
                finish();
            }
            Log.i("CaptureActivity", "onRequestPermissionsResult granted=" + granted);
        }
    }

    private void init() {
        try {
            Camera cam = Camera.open();
            cam.release();
            cam = null;
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder
                adb =
                new AlertDialog.Builder(this, android.R.style.Theme_Holo_Dialog);

            adb.setTitle(R.string.dialog_warning);
            adb.setMessage(R.string.dialog_content);
            adb.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            AlertDialog dialog = adb.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            dialog.setCancelable(false);

            allowCamera = false;
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        setContentView(R.layout.camera);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        btn_history = (ImageButton) findViewById(R.id.btn_history);
        btn_album = (ImageButton) findViewById(R.id.btn_album);
        tb_flashlight = (ToggleButton) findViewById(R.id.tb_flashlight);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        btn_history.setOnClickListener(this);
        btn_album.setOnClickListener(this);
        histroyList = new ArrayList<HistoryBean>();
        dbHelper = new DBHelper(getApplicationContext());
        Cursor c = dbHelper.query();
        if (c != null) {
            while (c.moveToNext()) {
                HistoryBean bean = new HistoryBean();
                bean.set_id(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ID)));
                bean.setCurrtime(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_TIME)));
                bean.setText(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_CODE)));
                bean.setType(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_TYPE)));
                histroyList.add(bean);
            }
        }
        tb_flashlight.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(
                CompoundButton buttonView,
                boolean isChecked) {
                if (isChecked) {
                    Parameters param = CameraManager.get().getCamera().getParameters();
                    param.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    CameraManager.get().getCamera().setParameters(param);
                } else {
                    Parameters param = CameraManager.get().getCamera().getParameters();
                    param.setFlashMode(Parameters.FLASH_MODE_OFF);
                    CameraManager.get().getCamera().setParameters(param);
                }
            }
        });

    }

    private void resume() {
        re();
        decodeFormats = null;
        characterSet = null;
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onResume() {
        Log.i("Turbo Scan", "onResume");
        super.onResume();
        if (allowCamera) {
            resume();
        }

        // Cooee 统计 begin
        StatisticsExpandNew
            .use(this, sn, appid, shellid, producttype, getPackageName(), opversion);
        // Cooee 统计 end

        // 友盟推送push
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
        mPushAgent.onAppStart();


    }

    private void re() {
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        Log.i("Turbo Scan", "onPause");
        super.onPause();
        if (allowCamera) {
            if (handler != null) {
                handler.quitSynchronously();
                handler = null;
            }
            CameraManager.get().closeDriver();
        }
    }

    @Override
    protected void onDestroy() {
        if (allowCamera) {
            inactivityTimer.shutdown();
        }
        super.onDestroy();

        if (Constant.mvNativeHandleForScanResult != null) {
            Constant.mvNativeHandleForScanResult.release();
            Constant.mvNativeHandleForScanResult = null;
        }
        if (Constant.mvNativeHandleForScanHistory != null) {
            Constant.mvNativeHandleForScanHistory.release();
            Constant.mvNativeHandleForScanHistory = null;
        }
        Constant.kmobAdJsonStrForScanResult = null;
        Constant.kmobAdJsonStrForScanHistory = null;
        Constant.scanResultAd = null;
        Constant.scanHistoryAd = null;
    }

    /**
     * Handler scan result
     */
    public void handleDecode(
        Result result,
        Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //FIXME
        if (resultString.equals("")) {
            Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            int contentType;
            if (resultString.contains(".")) {
                contentType = 1;
            } else {
                contentType = 2;
            }
            int repeat = isRepeat(resultString);
            if (repeat != -1) {
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.COLUMN_NAME_CODE, resultString);
                cv.put(DBHelper.COLUMN_NAME_TIME, getCurrTime());
                cv.put(DBHelper.COLUMN_NAME_TYPE, contentType);
                dbHelper.update(cv, resultString);
            } else {
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.COLUMN_NAME_CODE, resultString);
                cv.put(DBHelper.COLUMN_NAME_TIME, getCurrTime());
                cv.put(DBHelper.COLUMN_NAME_TYPE, contentType);
                dbHelper.insert(cv);
                HistoryBean bean = new HistoryBean();
                bean.setCurrtime(getCurrTime());
                bean.setText(resultString);
                bean.setType(contentType);
                histroyList.add(bean);
            }
            Toast.makeText(getApplicationContext(), resultString, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CaptureActivity.this, DialogScanDetail.class);
            intent.putExtra("content", resultString);
            intent.putExtra("type", contentType);
            startActivity(intent);
        }
    }

    private void initCamera(
        SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(
        SurfaceHolder holder,
        int format,
        int width,
        int height) {
        Log.i("Turbo Scan", "surfaceChanged");
    }

    @Override
    public void surfaceCreated(
        SurfaceHolder holder) {
        Log.i("Turbo Scan", "surfaceCreated");
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(
        SurfaceHolder holder) {
        Log.i("Turbo Scan", "surfaceDestroyed");
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                                          file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {

        public void onCompletion(
            MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public void onClick(
        View v) {
        switch (v.getId()) {
            case R.id.btn_history:
                Intent intent = new Intent(CaptureActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_album:
                try {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK);
                    //				pickIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                    pickIntent
                        .setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(pickIntent, 11);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data) {
        switch (requestCode) {
            case 11:
                if (data != null) {
                    startPhotoZoom(data.getData());
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_photo),
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (data == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_photo),
                                   Toast.LENGTH_SHORT).show();
                    return;
                }
                int type;
                isBackChoose = false;
                Result rs = decodeWithBitmap(data);
                if (rs == null) {
                    final AlertDialog dlg = new AlertDialog.Builder(CaptureActivity.this).create();
                    dlg.show();
                    Window window = dlg.getWindow();
                    window.setContentView(R.layout.dl_crop);
                    Button
                        back_choose =
                        (Button) window.findViewById(R.id.btn_back_choose);
                    back_choose.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(
                            View v) {
                            isBackChoose = true;
                            Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                      "image/*");
                            startActivityForResult(pickIntent, 11);
                            dlg.dismiss();
                        }
                    });
                    Button back_scan = (Button) window.findViewById(R.id.btn_back_scan);
                    back_scan.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(
                            View v) {
                            dlg.dismiss();
                        }
                    });
                    dlg.setOnDismissListener(new OnDismissListener() {

                        @Override
                        public void onDismiss(
                            DialogInterface dialog) {
                            //							if( handler != null )
                            //							{
                            //								handler.quitSynchronously();
                            //								handler = null;
                            //							}
                            //							re();
                            if (!isBackChoose) {
                                onPause();
                                onResume();
                            }
                        }
                    });
                    Toast.makeText(getApplicationContext(), getString(R.string.decoding_error),
                                   Toast.LENGTH_SHORT).show();
                } else {
                    String s = rs.getText();
                    if (s.contains("http://") || s.contains("https://")) {
                        type = 1;
                    } else {
                        type = 2;
                    }
                    /**
                     * wang
                     */
                    int repeat = isRepeat(s);
                    if (repeat != -1) {
                        ContentValues cv = new ContentValues();
                        cv.put(DBHelper.COLUMN_NAME_CODE, s);
                        cv.put(DBHelper.COLUMN_NAME_TIME, getCurrTime());
                        cv.put(DBHelper.COLUMN_NAME_TYPE, type);
                        dbHelper.update(cv, s);
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put(DBHelper.COLUMN_NAME_CODE, s);
                        cv.put(DBHelper.COLUMN_NAME_TIME, getCurrTime());
                        cv.put(DBHelper.COLUMN_NAME_TYPE, type);
                        dbHelper.insert(cv);
                        HistoryBean bean = new HistoryBean();
                        bean.setCurrtime(getCurrTime());
                        bean.setText(s);
                        bean.setType(type);
                        histroyList.add(bean);
                    }
                    //wang  end
                    Intent intent = new Intent(CaptureActivity.this, DialogScanDetail.class);
                    intent.putExtra("type", type);
                    intent.putExtra("content", s);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(
        Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }

    private Result decodeWithBitmap(
        Intent picdata) {
        Bitmap photo = null;
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            photo = extras.getParcelable("data");
        }
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        RGBLuminanceSource source = new RGBLuminanceSource(photo);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ChecksumException e) {
            e.printStackTrace();
            return null;
        } catch (FormatException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private String getCurrTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    private int isRepeat(
        String text) {
        int repeat = -1;
        if (histroyList.size() == 0) {
            return repeat;
        } else {
            for (int i = 0; i < histroyList.size(); i++) {
                if (text.equals(histroyList.get(i).getText())) {
                    repeat = histroyList.get(i).get_id();
                }
            }
        }
        return repeat;
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(
        int keyCode,
        KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(getApplicationContext(), getString(R.string.exit_toast),
                               Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void adInit() {
        // 每天的首次进入客户端，需要判断是国内IP还是海外
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SharedPreferences sp = getSharedPreferences(Constant.SP_FILE_NAME, Context.MODE_PRIVATE);
        if (sp.getInt(Constant.SP_KEY_DATE, -1) != day) {
            sp.edit().putInt(Constant.SP_KEY_DATE, day).commit();
            adRegionCheck();
        } else {
            //// TODO: 2016/6/1 直接下载广告
            Constant.isDomestic = sp.getBoolean(Constant.SP_KEY_IS_DOMESTIC, false);
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    downloadAd();
                    Looper.loop();
                }
            });
        }
    }

    /**
     * 服务器根据ip判断区域
     */
    private void adRegionCheck() {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                InputStream in = null;
                try {
                    URL url = null;
                    url = new URL("http://nanohome.cn/launcher/get_keywords/get_city.php");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(false);
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("Charset", "UTF-8");

                    urlConnection.connect();
                    in = urlConnection.getInputStream();
                    String jsonString = CommonUtil.inputStream2String(in);
                    JSONObject jsonObject = new JSONObject(jsonString);

                    if ("CN".equals(jsonObject.get("geo_country"))) {
                        Constant.isDomestic = true;
                    } else {
                        Constant.isDomestic = false;
                    }

                    SharedPreferences sp =
                        getSharedPreferences(Constant.SP_FILE_NAME, Context.MODE_PRIVATE);
                    sp.edit().putBoolean(Constant.SP_KEY_IS_DOMESTIC, Constant.isDomestic).commit();

                    downloadAd();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {

                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Looper.loop();
            }
        });
    }

    private void downloadAd() {
        if (Constant.isDomestic) {
            loadKmobAd();
        } else {
            if (CommonUtil
                .isAppInstalled(CaptureActivity.this, "com.facebook.katana")) {
                loadFacebookAd();
            } else {
                initMobvista();
                loadMobvistaNative();
            }
        }
    }

    private void loadKmobAd() {
        loadKmobAdScanResult();
        loadKmobAdScanHistory();
    }

    /**
     * 二维码扫描结果框广告
     */
    private void loadKmobAdScanResult() {
        AdBaseView adBaseView = KmobManager.createNative("20160603120648643", this, 1); // 下载1条广告
        adBaseView.addAdViewListener(new AdViewListener() {
            @Override
            public void onAdShow(String s) {

            }

            @Override
            public void onAdReady(String s) {
                ArrayList<KmobAdNativeData> allData = new ArrayList<KmobAdNativeData>();
                if (s != null) {
                    Constant.kmobAdJsonStrForScanResult = s;
                    try {
                        JSONObject object = new JSONObject(s);// 此时若不是jsonObject，则会抛出异常
                        KmobAdNativeData adData = createNativeData(object);
                        allData.add(adData);
                        Constant.scanResultAd = adData;
                    } catch (Exception e) {
                        try {
                            JSONArray array = new JSONArray(s);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                KmobAdNativeData adData = createNativeData(object);
                                allData.add(adData);
                                Constant.scanResultAd = adData;
                                break;
                            }
                        } catch (Exception e2) {
                            // TODO: handle exception
                        }
                    }
                }
            }

            @Override
            public void onAdFailed(String s) {
                Log.i("KmobAd", "onAdFailed -- " + s);
            }

            @Override
            public void onAdClick(String s) {

            }

            @Override
            public void onAdClose(String s) {

            }

            @Override
            public void onAdCancel(String s) {

            }
        });
    }

    /**
     * 二维码扫描历史记录广告
     */
    private void loadKmobAdScanHistory() {
        AdBaseView adBaseView = KmobManager.createNative("20160603120604643", this, 1); // 下载1条广告
        adBaseView.addAdViewListener(new AdViewListener() {
            @Override
            public void onAdShow(String s) {

            }

            @Override
            public void onAdReady(String s) {
                ArrayList<KmobAdNativeData> allData = new ArrayList<KmobAdNativeData>();
                if (s != null) {
                    Constant.kmobAdJsonStrForScanHistory = s;
                    try {
                        JSONObject object = new JSONObject(s);// 此时若不是jsonObject，则会抛出异常
                        KmobAdNativeData adData = createNativeData(object);
                        allData.add(adData);
                        Constant.scanHistoryAd = adData;
                    } catch (Exception e) {
                        try {
                            JSONArray array = new JSONArray(s);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                KmobAdNativeData adData = createNativeData(object);
                                allData.add(adData);
                                Constant.scanHistoryAd = adData;
                                break;
                            }
                        } catch (Exception e2) {
                            // TODO: handle exception
                        }
                    }
                }
            }

            @Override
            public void onAdFailed(String s) {
                Log.i("KmobAd", "onAdFailed -- " + s);
            }

            @Override
            public void onAdClick(String s) {

            }

            @Override
            public void onAdClose(String s) {

            }

            @Override
            public void onAdCancel(String s) {

            }
        });
    }

    /**
     * 通过广告传入的数据生成一个NativeAdData
     */
    private KmobAdNativeData createNativeData(JSONObject object) {
        String summary = "";
        String headline = "";
        String adcategory = "";
        String appRating = "";
        String adlogo = "";
        String details = "";
        String adlogoWidth = "";
        String adlogoHeight = "";
        String review = "";
        String appinstalls = "";
        String download = "";
        String adplaceid = "";
        String adid = "";
        String clickurl = "";
        String interactiontype = "";
        String open_type = "";
        String hurl = "";
        String hdetailurl = "";
        String pkgname = "";
        String appsize = "";
        String version = "";
        String versionname = "";
        String ctimg = "";
        String hiimg = "";
        String click_record_url = "";
        try {
            if (object.has(NativeAdData.SUMMARY_TAG)) {
                summary = object.getString(NativeAdData.SUMMARY_TAG);
            }
            if (object.has(NativeAdData.HEADLINE_TAG)) {
                headline = object.getString(NativeAdData.HEADLINE_TAG);
            }
            if (object.has(NativeAdData.ADCATEGORY_TAG)) {
                adcategory = object.getString(NativeAdData.ADCATEGORY_TAG);
            }
            if (object.has(NativeAdData.APPRATING_TAG)) {
                appRating = object.getString(NativeAdData.APPRATING_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_TAG)) {
                adlogo = object.getString(NativeAdData.ADLOGO_TAG);
            }
            if (object.has(NativeAdData.DETAILS_TAG)) {
                details = object.getString(NativeAdData.DETAILS_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_WIDTH_TAG)) {
                adlogoWidth = object.getString(NativeAdData.ADLOGO_WIDTH_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_HEIGHT_TAG)) {
                adlogoHeight = object.getString(NativeAdData.ADLOGO_HEIGHT_TAG);
            }
            if (object.has(NativeAdData.REVIEW_TAG)) {
                review = object.getString(NativeAdData.REVIEW_TAG);
            }
            if (object.has(NativeAdData.APPINSTALLS_TAG)) {
                appinstalls = object.getString(NativeAdData.APPINSTALLS_TAG);
            }
            if (object.has(NativeAdData.DOWNLOAD_TAG)) {
                download = object.getString(NativeAdData.DOWNLOAD_TAG);
            }
            if (object.has(NativeAdData.ADPLACE_ID_TAG)) {
                adplaceid = object.getString(NativeAdData.ADPLACE_ID_TAG);
            }
            if (object.has(NativeAdData.AD_ID_TAG)) {
                adid = object.getString(NativeAdData.AD_ID_TAG);
            }
            if (object.has(NativeAdData.CLICKURL_TAG)) {
                clickurl = object.getString(NativeAdData.CLICKURL_TAG);
            }
            if (object.has(NativeAdData.INTERACTION_TYPE_TAG)) {
                interactiontype = object
                    .getString(NativeAdData.INTERACTION_TYPE_TAG);
            }
            if (object.has(NativeAdData.OPEN_TYPE_TAG)) {
                open_type = object.getString(NativeAdData.OPEN_TYPE_TAG);
            }
            if (object.has(NativeAdData.HURL_TAG)) {
                hurl = object.getString(NativeAdData.HURL_TAG);
            }
            if (object.has(NativeAdData.HDETAILURL_TAG)) {
                hdetailurl = object.getString(NativeAdData.HDETAILURL_TAG);
            }
            if (object.has(NativeAdData.PKGNAME_TAG)) {
                pkgname = object.getString(NativeAdData.PKGNAME_TAG);
            }
            if (object.has(NativeAdData.APPSIZE_TAG)) {
                appsize = object.getString(NativeAdData.APPSIZE_TAG);
            }
            if (object.has(NativeAdData.VERSION_TAG)) {
                version = object.getString(NativeAdData.VERSION_TAG);
            }
            if (object.has(NativeAdData.VERSIONNAME_TAG)) {
                versionname = object.getString(NativeAdData.VERSIONNAME_TAG);
            }
            if (object.has(NativeAdData.CTIMG_TAG)) {
                ctimg = object.getString(NativeAdData.CTIMG_TAG);
            }
            if (object.has(NativeAdData.HIIMG_TAG)) {
                hiimg = object.getString(NativeAdData.HIIMG_TAG);
            }
            if (object.has(NativeAdData.CLICK_RECORD_URL_TAG)) {
                click_record_url = object
                    .getString(NativeAdData.CLICK_RECORD_URL_TAG);
            }
            return new KmobAdNativeData(summary, headline, adcategory, appRating,
                                        adlogo, details, adlogoWidth, adlogoHeight, review,
                                        appinstalls, download, adplaceid, adid, clickurl,
                                        interactiontype, open_type, hurl, hdetailurl, pkgname,
                                        appsize, version, versionname, ctimg, hiimg,
                                        click_record_url);
        } catch (Exception e) {
            Log.e("KMOB", "addAdView e " + e.toString());
        }
        return null;
    }

    private void loadFacebookAd() {
        loadFacebookAdScanResult();
        loadFacebookAdScanHistory();
    }

    /**
     * 二维码扫描结果框广告
     */
    private void loadFacebookAdScanResult() {
        final NativeAd nativeAd =
            new NativeAd(CaptureActivity.this, "284400338565208_284401161898459");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.i("FacebookAd", "onError = " + adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd == null || nativeAd != ad) {
                    Log.v("##########", "nativeAd == null || nativeAd != ad");
                    // Race condition, load() called again before last ad was displayed
                    return;
                }
                // Unregister last ad
                nativeAd.unregisterView();

                Constant.scanResultAd = ad;
            }

            @Override
            public void onAdClicked(Ad ad) {

            }
        });
        nativeAd.loadAd();
    }

    /**
     * 二维码扫描历史记录广告
     */
    private void loadFacebookAdScanHistory() {
        final NativeAd nativeAd =
            new NativeAd(CaptureActivity.this, "284400338565208_284400828565159");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.i("FacebookAd", "onError = " + adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd == null || nativeAd != ad) {
                    Log.v("##########", "nativeAd == null || nativeAd != ad");
                    // Race condition, load() called again before last ad was displayed
                    return;
                }
                // Unregister last ad
                nativeAd.unregisterView();

                Constant.scanHistoryAd = ad;
            }

            @Override
            public void onAdClicked(Ad ad) {

            }
        });
        nativeAd.loadAd();
    }


    public void initMobvista() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, String> map =
            sdk.getMVConfigurationMap("22466", "686dfddcac68d078f4de704b947cff0c");
        //如果是gradle打包，修改了applicationId,请在PACKAGE_NAME_MANIFEST中输入AndroidManifest.xml中的package的值
        map.put(MobVistaConstans.PACKAGE_NAME_MANIFEST, "com.cooeeui.nanoqrcodescan");
        sdk.init(map, this);
    }


    public void loadMobvistaNative() {
        loadMobvistaNativeScanResult();
        loadMobvistaNativeScanHistory();
    }


    /**
     * 二维码扫描结果框广告
     */
    public void loadMobvistaNativeScanResult() {
        if (Constant.mvNativeHandleForScanResult == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("813");//AD Unit ID
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "284400338565208_284401161898459");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);//请求广告条数，不设默认为1
            Constant.mvNativeHandleForScanResult =
                new MvNativeHandler(properties, CaptureActivity.this);
            Constant.mvNativeHandleForScanResult
                .setAdListener(new MvNativeHandler.NativeAdListener() {

                    @Override
                    public void onAdLoaded(List<Campaign> campaigns, int template) {
                        Log.i("Mobvista", "onAdLoaded -- " + campaigns.toString());
                        for (int i = 0; i < campaigns.size(); i++) {
                            if (campaigns.get(i) != null) {
                                Constant.scanResultAd = campaigns.get(i);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onAdLoadError(String message) {
                        Log.i("Mobvista", "onAdLoadError -- " + message);
                    }

                    @Override
                    public void onAdClick(Campaign campaign) {

                    }

                    @Override
                    public void onAdFramesLoaded(final List<Frame> list) {

                    }
                });

            //STEP3: Load native ad
            Constant.mvNativeHandleForScanResult.load();
        } else {

            //STEP3: Load native ad
            Constant.mvNativeHandleForScanResult.load();
        }
    }

    /**
     * 二维码扫描历史记录广告
     */
    public void loadMobvistaNativeScanHistory() {
        if (Constant.mvNativeHandleForScanHistory == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("812");//AD Unit ID
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "284400338565208_284400828565159");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);//请求广告条数，不设默认为1
            Constant.mvNativeHandleForScanHistory =
                new MvNativeHandler(properties, CaptureActivity.this);
            Constant.mvNativeHandleForScanHistory
                .setAdListener(new MvNativeHandler.NativeAdListener() {

                    @Override
                    public void onAdLoaded(List<Campaign> campaigns, int template) {
                        Log.i("Mobvista", "onAdLoaded -- " + campaigns.toString());
                        for (int i = 0; i < campaigns.size(); i++) {
                            if (campaigns.get(i) != null) {
                                Constant.scanHistoryAd = campaigns.get(i);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onAdLoadError(String message) {
                        Log.i("Mobvista", "onAdLoadError -- " + message);
                    }

                    @Override
                    public void onAdClick(Campaign campaign) {

                    }

                    @Override
                    public void onAdFramesLoaded(final List<Frame> list) {

                    }
                });

            //STEP3: Load native ad
            Constant.mvNativeHandleForScanHistory.load();
        } else {

            //STEP3: Load native ad
            Constant.mvNativeHandleForScanHistory.load();
        }
    }

    // Cooee 统计
    private void initCooeeStatistics() {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                Config.initConfig(CaptureActivity.this);
                JSONObject tmp = Config.config;
                PackageManager mPackageManager = getPackageManager();
                try {
                    JSONObject config = tmp.getJSONObject("config");
                    sn = config.getString("serialno");
                    appid = config.getString("app_id");

                    opversion = mPackageManager
                        .getPackageInfo(CaptureActivity.this.getPackageName(),
                                        0).versionName;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                shellid = CooeeSdk.cooeeGetCooeeId(CaptureActivity.this);

                StatisticsBaseNew.setApplicationContext(CaptureActivity.this);
                SharedPreferences preferences =
                    getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
                boolean firstRun = preferences.getBoolean(SP_KEY_FIRST_RUN, true);
                if (firstRun) {
                    preferences.edit().putBoolean(SP_KEY_FIRST_RUN, false).commit();
                    StatisticsExpandNew
                        .register(CaptureActivity.this, sn, appid, shellid, producttype,
                                  CaptureActivity.this.getPackageName(), opversion, true);
                } else {
                    StatisticsExpandNew
                        .startUp(CaptureActivity.this, sn, appid, shellid, producttype,
                                 CaptureActivity.this.getPackageName(), opversion, true);
                }
            }
        });
    }
}
