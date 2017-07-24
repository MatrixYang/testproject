package com.cooeeui.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsBaseNew;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooeeui.statistics.Config;
import com.cooeeui.wallpaper.flowlib.AsymmetricGridView;
import com.cooeeui.wallpaper.flowlib.AsymmetricGridViewAdapter;
import com.cooeeui.wallpaper.flowlib.Utils;
import com.cooeeui.wallpaper.http.DownloadListThread;
import com.cooeeui.wallpaper.local.WallPaperLocalActivity;
import com.cooeeui.wallpaper.model.ItemInfo;
import com.cooeeui.wallpaper.model.ListInfo;
import com.cooeeui.wallpaper.util.CommonTools;
import com.cooeeui.wallpaper.util.CommonUtil;
import com.cooeeui.wallpaper.util.ThreadUtil;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.Frame;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.mobvista.msdk.out.MvNativeHandler.NativeAdListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnlineWallpaperActivity extends Activity implements OnClickListener,
                                                                 DownloadListThread.LoadSuccessListener,
                                                                 AdapterView.OnItemClickListener {

    public static final String EXTRA_DOWNLOAD_URL = "download_url";
    public static final String EXTRA_DOWNLOAD_NAME = "download_name";
    public static final int NETERROR = 33;
    public static final int NETSUCCESS = 44;
    public static final String TAG = "OnlineWallpaperActivity";

    public static final String SP_FILE_NAME = "wallpaper";
    public static final String SP_KEY_FIRST_RUN = "first_run";

    //保存加载的所有记录
    public List<ItemInfo> allInfo;
    private LinearLayout linearLayoutErrorView;
    private DownloadListThread thread;
    private int max = 0;
    private ImageView imgLocal;
    private LinearLayout ll_loading_gone;
    private ImageView lv_loading_gone;
    private boolean isDataLoadSuccess = false;

    private final ItemUtils demoUtils = new ItemUtils();
    private DefaultListAdapter adapter;
    private AsymmetricGridView listView;

    private MvNativeHandler nativeHandle;
    private ArrayList<Campaign> mNativeCampaign = new ArrayList<>();
    private ArrayList<Integer> mAdPosition = new ArrayList<>();
    private final int mAdCountMax = 3;

    // Cooee 统计 begin
    private String sn;
    private String appid;
    private String shellid;
    private int producttype = 3;
    private String productname = "nano壁纸";
    private String opversion;
    // Cooee 统计 end

    public static boolean isDomestic;

    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NETSUCCESS:
                    if (isDataLoadSuccess) {
                        ll_loading_gone.setVisibility(View.GONE);
                    }

                    if (isDomestic) {
                        // Kmob广告begin
                        initKmobAd();
                        // Kmob广告end
                    } else {
                        if (CommonTools
                            .isAppInstalled(OnlineWallpaperActivity.this, "com.facebook.katana")) {
                            // facebookd ad begin
                            showFacebookAd(0);
                            showFacebookAd(1);
                            showFacebookAd(2);
                            // facebookd ad end
                        } else {
                            loadNative(); // Mobvista
                        }

                    }

                    break;
                case NETERROR:
                    ll_loading_gone.setVisibility(View.GONE);
                    linearLayoutErrorView.setVisibility(View.VISIBLE);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_artbook_activity);

        listView = (AsymmetricGridView) findViewById(R.id.listView);
        //刷新view
        ll_loading_gone = (LinearLayout) findViewById(R.id.ll_loading_gone);
        lv_loading_gone = (ImageView) findViewById(R.id.iv_loading_gone);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tips_circle);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (lv_loading_gone != null) {
            lv_loading_gone.startAnimation(operatingAnim);
        }
        linearLayoutErrorView = (LinearLayout) findViewById(R.id.linearNetError);
        linearLayoutErrorView.setOnClickListener(this);
        imgLocal = (ImageView) findViewById(R.id.wallpaper_local);
        imgLocal.setOnClickListener(this);

        thread = new DownloadListThread(this);
        thread.setListener(this);
        thread.start();

        adapter = new DefaultListAdapter(this);

        listView.setRequestedColumnCount(3);
        listView.setRequestedHorizontalSpacing(
            Utils.dpToPx(this, 3));
        listView.setAdapter(new AsymmetricGridViewAdapter(this, listView, adapter));
        listView.setDebugging(false);
        listView.setOnItemClickListener(this);

        initMobvista();

        // Kpush begin
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                CooeeSdk.initCooeeSdk(getApplicationContext());
            }
        });
        // Kpush end

        // Cooee 统计 begin
        initCooeeStatistics();
        // Cooee 统计 begin

        // 友盟推送
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.enable();
        PushAgent.getInstance(this).onAppStart();
    }

    private void initCooeeStatistics() {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                Config.initConfig(OnlineWallpaperActivity.this);
                JSONObject tmp = Config.config;
                PackageManager mPackageManager = getPackageManager();
                try {
                    JSONObject config = tmp.getJSONObject("config");
                    sn = config.getString("serialno");
                    appid = config.getString("app_id");

                    opversion = mPackageManager
                        .getPackageInfo(OnlineWallpaperActivity.this.getPackageName(),
                                        0).versionName;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                shellid = CooeeSdk.cooeeGetCooeeId(OnlineWallpaperActivity.this);

                StatisticsBaseNew.setApplicationContext(OnlineWallpaperActivity.this);
                SharedPreferences preferences =
                    getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
                boolean firstRun = preferences.getBoolean(SP_KEY_FIRST_RUN, true);
                if (firstRun) {
                    preferences.edit().putBoolean(SP_KEY_FIRST_RUN, false).commit();
                    StatisticsExpandNew
                        .register(OnlineWallpaperActivity.this, sn, appid, shellid, producttype,
                                  productname, opversion, true);
                } else {
                    StatisticsExpandNew
                        .startUp(OnlineWallpaperActivity.this, sn, appid, shellid, producttype,
                                 productname, opversion, true);
                }
            }
        });
    }


    public void initMobvista() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, String> map =
            sdk.getMVConfigurationMap("22466", "686dfddcac68d078f4de704b947cff0c");
        //如果是gradle打包，修改了applicationId,请在PACKAGE_NAME_MANIFEST中输入AndroidManifest.xml中的package的值
        map.put(MobVistaConstans.PACKAGE_NAME_MANIFEST, "com.cooeeui.wallpaper");
        sdk.init(map, this);
    }

    public void loadNative() {
        if (nativeHandle == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("731");//AD Unit ID
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "1005802856193798_1005803122860438");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, mAdCountMax);//请求广告条数，不设默认为1
            nativeHandle = new MvNativeHandler(properties, OnlineWallpaperActivity.this);
            nativeHandle.setAdListener(new NativeAdListener() {

                @Override
                public void onAdLoaded(List<Campaign> campaigns, int template) {
                    Log.i(TAG, "onAdLoaded");
                    mNativeCampaign.clear();
                    for (int i = 0; i < campaigns.size(); i++) {
                        if (campaigns.get(i) != null) {
                            mNativeCampaign.add(campaigns.get(i));
                        }
                    }

                    //i 为 0, 1, 2 ，广告位为15, 31, 47
                    for (int i = 0; i < mNativeCampaign.size() && i < mAdCountMax; i++) {
                        int position = 16 * (i + 1) - 1;
                        allInfo.add(position, new ItemInfo());
                        mAdPosition.add(position);
                        adapter.setNativeAd(position, mNativeCampaign.get(i));
                        //facebook广告加载次数
//                        MobclickAgent
//                            .onEvent(OnlineWallpaperActivity.this, "FacebookAdloadtimes");
                        // Cooee 统计 begin
                        StatisticsExpandNew
                            .onCustomEvent(OnlineWallpaperActivity.this, "AD_PV", sn, appid,
                                           shellid, 4, productname);
                        // Cooee 统计 end
                    }

                    if (isDataLoadSuccess) {
                        adapter.setMobVistaNativeHandle(nativeHandle);
                        adapter.setItems(
                            demoUtils.moarItems(max + mAdPosition.size(), mAdPosition.size(),
                                                mAdPosition));
                    }
                }

                @Override
                public void onAdLoadError(String message) {
                    Log.i(TAG, "onAdLoadError message: " + message);
                }

                @Override
                public void onAdClick(Campaign campaign) {
                    Log.i(TAG, "onAdClick(Campaign campaign)");
                    // Cooee 统计 begin
                    StatisticsExpandNew
                        .onCustomEvent(OnlineWallpaperActivity.this, "AD_click", sn, appid, shellid,
                                       4, productname);
                    // Cooee 统计 end
                }

                @Override
                public void onAdFramesLoaded(final List<Frame> list) {

                }
            });

            //STEP3: Load native ad
            nativeHandle.load();
        } else {

            //STEP3: Load native ad
            nativeHandle.load();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wallpaper_local:
                Intent intent = new Intent(this, WallPaperLocalActivity.class);
                startActivity(intent);
                break;
            case R.id.linearNetError:
                if (CommonTools.isNetworkAvailable(this)) {
                    linearLayoutErrorView.setVisibility(View.INVISIBLE);
                    ll_loading_gone.setVisibility(View.VISIBLE);
                    thread = new DownloadListThread(this);
                    thread.setListener(this);
                    thread.start();
                }
                break;
        }
    }

    @Override
    public void onSuccess(List<ListInfo> info) {
        isDataLoadSuccess = true;
        max = info.get(0).getItemList().size();
        allInfo = info.get(0).getItemList();

        if (adapter != null) {
            adapter.setItems(demoUtils.moarItems(max, 0, null));
        }

        adRegionCheck();
    }

    @Override
    public void onFailed() {
        mHanlder.obtainMessage(NETERROR).sendToTarget();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nativeHandle != null) {
            nativeHandle.release();
        }
    }

    @Override
    public void onItemClick(@NotNull AdapterView<?> parent, @NotNull View view,
                            int position, long id) {
        String url = String.valueOf((allInfo.get(position)).getResurl());
        String name = url.substring(url.lastIndexOf("/") + 1);
        Intent intent = new Intent(OnlineWallpaperActivity.this,
                                   DetailAndCropActivity.class);
        intent.putExtra(EXTRA_DOWNLOAD_URL, url);
        intent.putExtra(EXTRA_DOWNLOAD_NAME, name);
        startActivity(intent);
    }

    /**
     * 加载facebook广告
     */
    private int adNum = 0;
    private int[] adIndex = {15, 31, 47};
    //    private ArrayList<Integer> mAdPosition = new ArrayList<>();
    private boolean nativeAdLoaded[] = {false, false, false};

    private void showFacebookAd(final int index) {
        final NativeAd nativeAd =
            new NativeAd(OnlineWallpaperActivity.this, "1005802856193798_1005803122860438");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                nativeAdLoaded[index] = true;
                if (isDataLoadSuccess && nativeAdLoaded[0] && nativeAdLoaded[1]
                    && nativeAdLoaded[2]) {
                    adapter.setItems(demoUtils.moarItems(max + adNum, adNum, mAdPosition));
                }
                Log.v("##########", "Ad failed to load: " + adError.getErrorMessage());
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
                allInfo.add(adIndex[adNum], new ItemInfo());
                adapter.setNativeAd(adIndex[adNum], nativeAd);
                mAdPosition.add(adIndex[adNum]);
                adNum++;
                nativeAdLoaded[index] = true;
                if (isDataLoadSuccess && nativeAdLoaded[0] && nativeAdLoaded[1]
                    && nativeAdLoaded[2]) {
                    adapter.setItems(demoUtils.moarItems(max + adNum, adNum, mAdPosition));
                }
                //facebook广告加载次数
//                MobclickAgent.onEvent(OnlineWallpaperActivity.this, "FacebookAdloadtimes");

                // Cooee 统计 begin
                StatisticsExpandNew
                    .onCustomEvent(OnlineWallpaperActivity.this, "AD_PV", sn, appid, shellid, 4,
                                   productname);
                // Cooee 统计 end
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.i(TAG, "onAdClicked(Ad ad)");
                //facebook广告点击次数
//                MobclickAgent.onEvent(OnlineWallpaperActivity.this, "FacebookAdclicktimes");

                // Cooee 统计 begin
                StatisticsExpandNew
                    .onCustomEvent(OnlineWallpaperActivity.this, "AD_click", sn, appid, shellid, 4,
                                   productname);
                // Cooee 统计 end
            }
        });
        nativeAd.loadAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 友盟
        MobclickAgent.onResume(this);

        // Cooee 统计 begin
        StatisticsExpandNew
            .use(this, sn, appid, shellid, producttype, productname, opversion);
        // Cooee 统计 end
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 友盟
        MobclickAgent.onPause(this);
    }

    private void adRegionCheck() {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {

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
                    Log.i("yezhennan", jsonString);
                    if ("CN".equals(jsonObject.get("geo_country"))) {
                        isDomestic = true;
                    }

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

                mHanlder.obtainMessage(NETSUCCESS).sendToTarget();

            }
        });
    }

    // Kmob广告begin
    private String mKmobAdJsonStr;

    private void initKmobAd() {
        Log.i("yezhennan", "initKmobAd");
        AdBaseView adBaseView = KmobManager.createNative("20160517020511584", this, mAdCountMax);
        adBaseView.addAdViewListener(new AdViewListener() {
            @Override
            public void onAdShow(String s) {

            }

            @Override
            public void onAdReady(String s) {
                ArrayList<NativeData> allData = new ArrayList<NativeData>();
                if (s != null) {
                    mKmobAdJsonStr = s;
                    try {
                        Log.i("yezhennan", "JSONObject 1");

                        JSONObject object = new JSONObject(s);// 此时若不是jsonObject，则会抛出异常
                        NativeData adData = CreatNativeData.createNativeData(object);
                        allData.add(adData);

                        Log.i("yezhennan", "JSONObject");
                    } catch (Exception e) {
                        try {
                            Log.i("yezhennan", "JSONArray 1");
                            JSONArray array = new JSONArray(s);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                NativeData adData = CreatNativeData.createNativeData(object);
                                allData.add(adData);
                            }
                            Log.i("yezhennan", "JSONArray");

                            //i 为 0, 1, 2 ，广告位为15, 31, 47
                            for (int i = 0; i < allData.size() && i < mAdCountMax; i++) {
                                int position = 16 * (i + 1) - 1;
                                allInfo.add(position, new ItemInfo());
                                mAdPosition.add(position);
                                adapter.setNativeAd(position, allData.get(i));

                                KmobManager.onNativeAdShow(
                                    allData.get(i).getAdplaceid(),
                                    allData.get(i).getAdid());

                                // Cooee 统计 begin
                                StatisticsExpandNew
                                    .onCustomEvent(OnlineWallpaperActivity.this, "AD_PV", sn, appid,
                                                   shellid, 4, productname);
                                // Cooee 统计 end
                            }

                            if (isDataLoadSuccess) {
                                adapter.setItems(demoUtils.moarItems(max + mAdPosition.size(),
                                                                     mAdPosition.size(),
                                                                     mAdPosition));
                            }
                        } catch (Exception e2) {
                            // TODO: handle exception
                        }
                    }
                }
            }

            @Override
            public void onAdFailed(String s) {
                Log.i("yezhennan", "onAdFailed = " + s);
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

    public void onKmobAdClick(String adid) {

        KmobManager.onClickDone(adid, mKmobAdJsonStr, false);

        // Cooee 统计 begin
        StatisticsExpandNew
            .onCustomEvent(OnlineWallpaperActivity.this, "AD_click", sn, appid, shellid, 4,
                           productname);
        // Cooee 统计 end
    }
    // Kmob广告end
}
