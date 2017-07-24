package com.cooeeui.nanobooster.test;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.nanobooster.R;
import com.cooeeui.nanobooster.common.util.CommonUtil;
import com.cooeeui.nanobooster.common.util.MemoryUtil;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestActivity extends Activity {

    public static boolean needDeepClean;


    private ActivityManager mActivityManager;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private ImageView mFloatLayout;

    private TextView mTotalMemory;
    private TextView mAviliableMemory;
    private TextView mUsedMemory;
    private TextView mUsedPercentMemory;

    private TextView mAccessibilityLabel;
    private ListView mListView;
    private ArrayList<String> mPkgNameList = new ArrayList<>();

    // ad begin
    private final int mAdCountMax = 3;
    private RelativeLayout mAdUnit;
    private RelativeLayout mAdUnit2;
    // Mobvista ad begin
    private MvNativeHandler nativeHandle;
    private ArrayList<Campaign> mNativeCampaign = new ArrayList<>();
    // Mobvista ad end
    // ad end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        initViews();
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        // ad begin
        // Mobvista ad begin
        initMobvista();
        loadMobvistaNative();
        // Mobvista ad end

        // facebook ad
        if (CommonUtil.isAppInstalled(TestActivity.this, "com.facebook.katana")) {
            showFacebookAd();
        }
        // ad end
    }


    @Override
    protected void onResume() {
        super.onResume();

        changeLabelStatus();
        needDeepClean = false;
    }


    private void initViews() {
        mTotalMemory = (TextView) findViewById(R.id.tv_total_memory);
        mAviliableMemory = (TextView) findViewById(R.id.tv_aviliable_memory);
        mUsedMemory = (TextView) findViewById(R.id.tv_used_memory);
        mUsedPercentMemory = (TextView) findViewById(R.id.tv_used_percent_memory);

        mAccessibilityLabel = (TextView) findViewById(R.id.tv_accessibility);
//        mListView = (ListView) findViewById(R.id.lv_running_app);

        // ad begin
        mAdUnit = (RelativeLayout) findViewById(R.id.ad_unit);
        mAdUnit2 = (RelativeLayout) findViewById(R.id.ad_unit2);
        // ad end
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.bt_start_calc:
                float totalMemory = MemoryUtil.getTotalMemory(this) / 1024f / 1024f;
                totalMemory = (float) (Math.round(totalMemory * 100)) / 100;
                mTotalMemory.setText(String.valueOf(totalMemory) + " GB");

                float available = MemoryUtil.getAvailableMemory(this) / 1024f;
                available = (float) (Math.round(available * 100)) / 100;
                mAviliableMemory.setText(String.valueOf(available) + " MB");

                float used = MemoryUtil.getUsedMemory(this) / 1024f / 1024f;
                used = (float) (Math.round(used * 100)) / 100;
                mUsedMemory.setText(String.valueOf(used) + " GB");

                mUsedPercentMemory.setText(MemoryUtil.getUsedPercentValue(this));
                break;

            case R.id.bt_open_accessibility:
                openAccessibilitySetting();
                break;

            case R.id.bt_clear_memory:
                MemoryUtil.cleanMemory(this);
                break;

//            case R.id.bt_deep_clear_memory:
//                deepClearMemory();
//                break;
//
//            case R.id.bt_get_running_app:
//                getRunningApp();
//                break;
        }

    }

    private void getRunningApp() {
        if (Build.VERSION.SDK_INT >= 21) {
            List<AndroidAppProcess> list = ProcessManager.getRunningAppProcesses();
            for (AndroidAppProcess process : list) {
                if (!process.foreground) {
                    mPkgNameList.add(process.getPackageName());
                }
            }
            RunningAppAdapter appAdapter = new RunningAppAdapter(mPkgNameList);
            mListView.setAdapter(appAdapter);
        } else {
            List<ActivityManager.RunningAppProcessInfo> list =
                mActivityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo info : list) {
                if (info.importance
                    >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    mPkgNameList.add(info.processName);
                }
            }
            RunningAppAdapter appAdapter = new RunningAppAdapter(mPkgNameList);
            mListView.setAdapter(appAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mWindowManager != null && mFloatLayout != null) {
            mWindowManager.removeViewImmediate(mFloatLayout);
        }
    }

    private void changeLabelStatus() {
        mAccessibilityLabel.setText(isAccessibleEnabled() ? "辅助功能已启用" : "辅助功能未启用");
    }

    public void openAccessibilitySetting() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    public void deepClearMemory() {
        mFloatLayout = new ImageView(this);
        mFloatLayout.setBackgroundColor(Color.BLACK);
        mFloatLayout.setAlpha(0.5f);

        mWindowManager.addView(mFloatLayout, mWindowParams);

        for (String pkgName : mPkgNameList) {
            Intent killIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri packageURI = Uri.parse("package:" + pkgName);
            killIntent.setData(packageURI);
            startActivity(killIntent);
        }

        needDeepClean = true;
    }

    private boolean isAccessibleEnabled() {
        AccessibilityManager manager =
            (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = manager.getEnabledAccessibilityServiceList(
            AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo info : runningServices) {
            if (info.getId().equals(getPackageName()
                                    + "/.services.BoosterAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    class RunningAppAdapter extends BaseAdapter {

        private ArrayList<String> mPackageList;

        public RunningAppAdapter(ArrayList<String> packageList) {
            mPackageList = packageList;
        }

        @Override
        public int getCount() {
            return mPackageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(TestActivity.this);
            }
            ((TextView) convertView).setText(mPackageList.get(position));
            return convertView;
        }
    }

    // ad begin
    // Mobvista ad begin
    public void initMobvista() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, String> map =
            sdk.getMVConfigurationMap("22466", "686dfddcac68d078f4de704b947cff0c");
        sdk.init(map, this);
    }

    public void loadMobvistaNative() {
        if (nativeHandle == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("544");
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "826581090784415_870544966388027");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, mAdCountMax);//请求广告条数，不设默认为1
            nativeHandle = new MvNativeHandler(properties, TestActivity.this);
            nativeHandle.setAdListener(new MvNativeHandler.NativeAdListener() {

                @Override
                public void onAdLoaded(List<Campaign> campaigns, int template) {

                    mNativeCampaign.clear();
                    for (int i = 0; i < campaigns.size(); i++) {
                        if (campaigns.get(i) != null) {
                            mNativeCampaign.add(campaigns.get(i));
                        }
                    }

                    final ImageView nativeAdCover =
                        (ImageView) mAdUnit.findViewById(R.id.native_ad_cover);
                    final ImageView nativeAdIcon =
                        (ImageView) mAdUnit.findViewById(R.id.native_ad_icon);
                    final TextView nativeAdTitle =
                        (TextView) mAdUnit.findViewById(R.id.native_ad_title);
                    final TextView nativeAdBody =
                        (TextView) mAdUnit.findViewById(R.id.native_ad_body);

                    nativeAdBody.setText(mNativeCampaign.get(0).getAppDesc());
                    nativeAdTitle.setText(mNativeCampaign.get(0).getAppName());
                    //mobvista绑定点击事件
                    nativeHandle.registerView(nativeAdCover, mNativeCampaign.get(0));
                    final String urlIcon = mNativeCampaign.get(0).getIconUrl();
                    Picasso.with(TestActivity.this)
                        .load(urlIcon)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon);
                    // Downloading and setting the ad icon.
                    final String urlImage = mNativeCampaign.get(0).getImageUrl();
                    Picasso.with(TestActivity.this)
                        .load(urlImage)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdCover);
                }

                @Override
                public void onAdLoadError(String message) {
                    Log.i("", "onAdLoadError : " + message);
                }

                @Override
                public void onAdClick(Campaign campaign) {

                }
            });

            //STEP3: Load native ad
            nativeHandle.load();
        } else {

            //STEP3: Load native ad
            nativeHandle.load();
        }
    }
    // Mobvista ad end

    // facebook ad begin
    private void showFacebookAd() {
        final NativeAd nativeAd =
            new NativeAd(TestActivity.this, "826581090784415_870544966388027");
        nativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
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

                ImageView nativeAdCover = (ImageView) mAdUnit2.findViewById(R.id.native_ad_cover);
                ImageView nativeAdIcon = (ImageView) mAdUnit2.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = (TextView) mAdUnit2.findViewById(R.id.native_ad_title);
                TextView nativeAdBody = (TextView) mAdUnit2.findViewById(R.id.native_ad_body);
                Button nativeAdCallToAction =
                    (Button) mAdUnit2.findViewById(R.id.native_ad_call_to_action);

                // Setting the Text
                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                nativeAdCallToAction.setVisibility(View.VISIBLE);
                nativeAdTitle.setText(nativeAd.getAdTitle());
                nativeAdBody.setText(nativeAd.getAdBody());

                // Downloading and setting the ad icon.
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

                NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
                NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdCover);

                nativeAd.registerViewForInteraction(mAdUnit2);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }
        });
        nativeAd.loadAd();
    }
    // facebook ad end
    // ad end

}
