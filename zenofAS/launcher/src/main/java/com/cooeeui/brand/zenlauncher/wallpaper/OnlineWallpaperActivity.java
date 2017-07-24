package com.cooeeui.brand.zenlauncher.wallpaper;

import android.content.Intent;
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
import android.widget.TextView;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.basecore.utilities.NetworkAvailableUtils;
import com.cooeeui.brand.zenlauncher.wallpaper.http.DownloadListThread;
import com.cooeeui.brand.zenlauncher.wallpaper.local.WallPaperLocalActivity;
import com.cooeeui.brand.zenlauncher.wallpaper.model.ItemInfo;
import com.cooeeui.brand.zenlauncher.wallpaper.model.ListInfo;
import com.cooeeui.wallpaper.flowlib.AsymmetricGridView;
import com.cooeeui.wallpaper.flowlib.AsymmetricGridViewAdapter;
import com.cooeeui.wallpaper.flowlib.Utils;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.MvNativeHandler;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.facebook.ads.AdListener;

public class OnlineWallpaperActivity extends BaseActivity implements OnClickListener,
                                                                     DownloadListThread.LoadSuccessListener,
                                                                     DialogUtil.DialogCancelListener,
                                                                     AdapterView.OnItemClickListener {

    public static final String EXTRA_DOWNLOAD_URL = "download_url";
    public static final String EXTRA_DOWNLOAD_NAME = "download_name";
    public static final int NETERROR = 33;
    public static final int NETSUCCESS = 44;
    public static final String TAG = "OnlineWallpaperActivity";
    //保存加载的所有记录
    public static List<ItemInfo> allInfo;
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
    private HashMap<String, String> stringMap = new HashMap<>();


    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NETSUCCESS:
                    if (isDataLoadSuccess) {
                        ll_loading_gone.setVisibility(View.GONE);
                    }
                    // facebook ad begin
                    if (CommonUtil
                        .isAppInstalled(OnlineWallpaperActivity.this, "com.facebook.katana")) {
                        showFacebookAd(0);
                        showFacebookAd(1);
                        showFacebookAd(2);
                    } else {
                        loadNative();
                    }
                    // facebookd ad end

                    break;
                case NETERROR:
                    ll_loading_gone.setVisibility(View.GONE);
                    if (ll_loading_gone.getVisibility() == View.GONE) {
                        linearLayoutErrorView.setVisibility(View.VISIBLE);
                    }
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_artbook_activity);

        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("wallpaper_string") != null) {
            stringMap =
                (HashMap<String, String>) getIntent().getSerializableExtra("wallpaper_string");
            if (stringMap != null && stringMap.size() > 0) {
                StringUtil.setStringMap(stringMap);
            }
        }

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
        ((TextView) findViewById(R.id.tv_wallpaper_zen_wallpaper))
            .setText(StringUtil.getString(this, R.string.wallpaper_zen_wallpaper));
        ((TextView) findViewById(R.id.tv_wallpaper_net_error)).setText(
            StringUtil.getString(this, R.string.wallpaper_check_net));
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
    }

    public void loadNative() {
        if (nativeHandle == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("544");
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "826581090784415_870544966388027");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, mAdCountMax);//请求广告条数，不设默认为1
            nativeHandle = new MvNativeHandler(properties, OnlineWallpaperActivity.this);
            nativeHandle.setAdListener(new MvNativeHandler.NativeAdListener() {

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
                        MobclickAgent
                            .onEvent(OnlineWallpaperActivity.this, "FacebookAdloadtimes");
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
                    Log.i(TAG, "onAdClick");
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
                if (NetworkAvailableUtils.isNetworkAvailable(this)) {
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
        mHanlder.obtainMessage(NETSUCCESS).sendToTarget();
        max = info.get(0).getItemList().size();
        allInfo = info.get(0).getItemList();

        if (adapter != null) {
            adapter.setItems(demoUtils.moarItems(max, 0, null));
        }
    }

    @Override
    public void onFailed() {
        mHanlder.obtainMessage(NETERROR).sendToTarget();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            // 友盟
//            MobclickAgent.onKillProcess(this);
//            System.exit(0);
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

        // 友盟
//        MobclickAgent.onKillProcess(this);
//        System.exit(0);
    }

    @Override
    public void onCancelDialog() {
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
    private boolean nativeAdLoaded[] = {false, false, false};

    private void showFacebookAd(final int index) {
        final NativeAd
            nativeAd =
            new NativeAd(OnlineWallpaperActivity.this, "826581090784415_870544966388027");
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
                MobclickAgent.onEvent(OnlineWallpaperActivity.this, "FacebookAdloadtimes");
            }

            @Override
            public void onAdClicked(Ad ad) {
                //facebook广告点击次数
                MobclickAgent.onEvent(OnlineWallpaperActivity.this, "FacebookAdclicktimes");
            }
        });
        nativeAd.loadAd();
    }
}
