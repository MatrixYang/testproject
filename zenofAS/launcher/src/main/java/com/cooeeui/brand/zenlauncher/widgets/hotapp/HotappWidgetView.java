package com.cooeeui.brand.zenlauncher.widgets.hotapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cooeeui.basecore.utilities.NetworkAvailableUtils;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.mobvista.MobvistaCampaignInfo;
import com.cooeeui.brand.zenlauncher.widgets.NanoWidgetUtils;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.ui.AlertDialogUtil;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.MvNativeHandler;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/22.
 */
public class HotappWidgetView extends FrameLayout implements View.OnClickListener {

    private static String TAG = "HotappWidgetView";
    private Context mContext;
    private View mainView;
    private ImageView hotapp_ad;
    private ImageView hotapp_icon;
    private MvNativeHandler nativeHandle;

    private MobvistaCampaignInfo campaignInfoLoaded;
    private MobvistaCampaignInfo campaignInfoLoading;
    private int widgetId;

    private Handler mHandler = new Handler();
    /**
     * 每次更新广告相隔的时间, 10分钟
     */
    private static long MOBVISTA_NATIVE_AD_NOCE_TIME = 10 * 60 * 1000;
    /**
     * 一条广告有效期， 55分钟
     */
    private static long MOBVISTA_NATIVE_AD_VALID_TIME = 55 * 60 * 1000;
    /**
     * 上次弹出广告的时间
     */
    private long mobvistaNativeAdLastTime = 0;

    /**
     * 广告正在加载
     */
    private boolean mobvistaNativeAdLoading = false;

    AlertDialogUtil mAlertDialogUtil;

    public HotappWidgetView(Context context, int id) {
        super(context);
        mContext = context;
        widgetId = id;
        mAlertDialogUtil = new AlertDialogUtil(Launcher.getInstance());
        mainView = LayoutInflater.from(context).inflate(R.layout.hotapp_widget_layout, null);
        addView(mainView);
        hotapp_ad = (ImageView) mainView.findViewById(R.id.hotapp_ad);
        hotapp_icon = (ImageView) mainView.findViewById(R.id.hotapp_icon);

        hotapp_icon.setOnClickListener(this);
        hotapp_ad.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NanoWidgetUtils.ACTION_WIDGET_LOAD_MOBVISTA_NATIVE_AD);
        filter.addAction(NanoWidgetUtils.ACTION_WIDGET_DELETE);
        context.registerReceiver(mBroadcastReceiver, filter);

        loadMobvistaNativeAd();
        updateNativeAdView();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NanoWidgetUtils.ACTION_WIDGET_LOAD_MOBVISTA_NATIVE_AD.equals(action)) {
                loadMobvistaNativeAd();
                updateNativeAdView();
            } else if (NanoWidgetUtils.ACTION_WIDGET_DELETE.equals(action)) {
                if (intent.getIntExtra("widget_id", 0) == widgetId) {
                    Log.v("ACTION_WIDGET_DELETE", "HotappWidgetView ACTION_WIDGET_DELETE");
                    releaseAd();
                    finish();
                }
            }
        }
    };

    private void updateNativeAd() {
        //当前广告加载完成
        if (campaignInfoLoading != null && campaignInfoLoading.isLoaded()) {
            //释放上一个广告
            if (campaignInfoLoaded != null) {
                campaignInfoLoaded.release();
            }

            campaignInfoLoaded = campaignInfoLoading;
            campaignInfoLoading = null;
        }
    }

    private void updateNativeAdView() {
        if (isCampaignOk()) {
            hotapp_ad.setImageBitmap(campaignInfoLoaded.getBannerBitmap());
            mAlertDialogUtil.setMobVistaNativeAdUse(nativeHandle, campaignInfoLoaded);
        } else {
            hotapp_ad.setImageResource(R.drawable.widget_hotapp_default);
        }
    }

    private void loadMobvistaNativeAd() {
        if (!NetworkAvailableUtils.isWifi(mContext)) {
            return;
        }

        if ((mobvistaNativeAdLastTime != 0) && (
            System.currentTimeMillis() - mobvistaNativeAdLastTime < MOBVISTA_NATIVE_AD_NOCE_TIME)) {
            return;
        }

        if (mobvistaNativeAdLoading) {
            return;
        }

        mobvistaNativeAdLoading = true;
        loadNative();
    }

    public void loadNative() {
        if (nativeHandle == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("220");
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "826581090784415_829867877122403");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, 1);//请求广告条数，不设默认为1
            nativeHandle = new MvNativeHandler(properties, mContext);
            nativeHandle.setAdListener(new MvNativeHandler.NativeAdListener() {

                @Override
                public void onAdLoaded(List<Campaign> campaigns, int template) {
                    Log.i(TAG, "onAdLoaded");
                    if (campaigns != null && campaigns.size() > 0) {
                        campaignInfoLoading = new MobvistaCampaignInfo();
                        campaignInfoLoading.setCampaign(campaigns.get(0));
                        ThreadUtil.execute(imgLoadThreadRunable);
                    }
                }

                @Override
                public void onAdLoadError(String message) {
                    Log.i(TAG, "onAdLoadError message: " + message);
                    mobvistaNativeAdLoading = false;
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

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Runnable imgLoadThreadRunable = new Runnable() {
        @Override
        public void run() {
            Bitmap bitmap = null;
            //主线程把对象设置为null后，子线程仍然在运行
            if (campaignInfoLoading == null || campaignInfoLoading.getCampaign() == null) {
                return;
            }
            //下载banner大图
            bitmap = getBitmapFromURL(campaignInfoLoading.getCampaign().getImageUrl());
            if (bitmap == null) {
                mobvistaNativeAdLoading = false;
                Log.i(TAG, "imageurl bitmap is null");
                return;
            }

            if (campaignInfoLoading == null) {
                return;
            }
            campaignInfoLoading.setBannerBitmap(bitmap);

            if (campaignInfoLoading == null || campaignInfoLoading.getCampaign() == null) {
                return;
            }
            //下载icon小图
            bitmap = getBitmapFromURL(campaignInfoLoading.getCampaign().getImageUrl());
            if (bitmap == null) {
                Log.i(TAG, "imageurl bitmap is null");
                mobvistaNativeAdLoading = false;
                return;
            }

            if (campaignInfoLoading == null) {
                return;
            }
            campaignInfoLoading.setIconBitmap(bitmap);
            campaignInfoLoading.setLoaded(true);

            mobvistaNativeAdLastTime = System.currentTimeMillis();
            mobvistaNativeAdLoading = false;

            mHandler.post(updateAdRunable);
            mHandler.removeCallbacks(validAdRunable);
            mHandler.postDelayed(validAdRunable, MOBVISTA_NATIVE_AD_VALID_TIME);
        }
    };

    private Runnable updateAdRunable = new Runnable() {
        @Override
        public void run() {
            updateNativeAd();
            updateNativeAdView();
        }
    };

    private Runnable validAdRunable = new Runnable() {
        @Override
        public void run() {
            campaignInfoLoaded.setValid(false);
            updateNativeAdView();
        }
    };

    private void releaseAd() {
        mHandler.removeCallbacks(validAdRunable);
        mHandler.removeCallbacks(updateAdRunable);
        if (nativeHandle != null) {
            nativeHandle.release();
        }
        mAlertDialogUtil.setMobVistaNativeAdUse(null, null);
        if (campaignInfoLoaded != null) {
            campaignInfoLoaded.release();
            campaignInfoLoaded = null;
        }
        if (campaignInfoLoading != null) {
            campaignInfoLoading.release();
            campaignInfoLoading = null;
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.hotapp_ad:
                onAdAreaClick();
                break;
            case R.id.hotapp_icon:
                onIconAreaClick();
                break;
        }

    }

    private void onIconAreaClick() {
        startHotApp();
        //从插件入口进入hotapp
        MobclickAgent.onEvent(mContext, "Hotappclickinwidget");
    }

    private boolean isCampaignOk() {
        return campaignInfoLoaded != null && campaignInfoLoaded.isLoaded() && campaignInfoLoaded
            .isValid();
    }

    private void onAdAreaClick() {
        if (isCampaignOk()) {
            mAlertDialogUtil.showAlertDialog(false, true,
                                             AlertDialogUtil.AlertDialogType.TYPE_WIDGET_MOBVISTA_NATIVE_AD,
                                             R.layout.alter_dialog_widget_mobvista_ad);
        } else {
            startHotApp();
        }
    }

    private void startHotApp() {
        //MobvistaSDK *start*//
        Class<?> aClass;
        try {
            aClass = Class
                .forName("com.mobvista.msdk.shell.MVActivity");
            Intent intent1 = new Intent(mContext, aClass);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra(MobVistaConstans.PROPERTIES_UNIT_ID, "218");
            intent1.putExtra(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_ID,
                             R.drawable.mobvista_wall_hot_app_img_logo);
            mContext.startActivity(intent1);
        } catch (ClassNotFoundException e) {
            e.fillInStackTrace();
        } finally {

        }
        //MobvistaSDK *end*//
    }

    public void finish() {
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }
}
