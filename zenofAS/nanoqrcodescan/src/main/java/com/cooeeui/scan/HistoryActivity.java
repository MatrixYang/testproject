package com.cooeeui.scan;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.ad.KmobAdNativeData;
import com.cooeeui.nanoqrcodescan.R;
import com.cooeeui.utils.Constant;
import com.facebook.ads.NativeAd;
import com.kmob.kmobsdk.KmobManager;
import com.mobvista.msdk.out.Campaign;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.umeng.message.PushAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends Activity {

    private ImageView iv_back;
    private RelativeLayout mRlAdUnit;
    private LinearLayoutListView list;
    private TextView tv;
    private HistoryListAdapter adapter;
    private DBHelper dbHelper;
    private List<HistoryBean> historyList;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (Constant.scanHistoryAd != null) {
                fillTheAdLayout();
            } else {
                pollingAdList();
            }
        }
    };

    @Override
    protected void onCreate(
        Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historical_records);
        findViewById();
        setListener();
        if (Constant.scanHistoryAd != null) {
            fillTheAdLayout();
        } else {
            pollingAdList();
        }

        dbHelper = new DBHelper(getApplicationContext());
        historyList = new ArrayList<HistoryBean>();
        Cursor c = dbHelper.query();
        if (c != null) {
            while (c.moveToNext()) {
                HistoryBean bean = new HistoryBean();
                bean.set_id(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_ID)));
                bean.setText(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_CODE)));
                bean.setType(c.getInt(c.getColumnIndex(DBHelper.COLUMN_NAME_TYPE)));
                bean.setCurrtime(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME_TIME)));
                historyList.add(bean);
            }
        }
        adapter = new HistoryListAdapter(HistoryActivity.this, historyList, list);
        if (historyList.size() == 0) {
            tv.setText(getString(R.string.no_history_record));
            tv.setGravity(Gravity.CENTER);
        }
        list.setAdapter(adapter);

        // 友盟推送push
        PushAgent.getInstance(this).onAppStart();
    }

    private void findViewById() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        list = (LinearLayoutListView) findViewById(R.id.list);
        tv = (TextView) findViewById(R.id.empty);
        mRlAdUnit = (RelativeLayout) findViewById(R.id.ad_unit);
    }

    private void setListener() {
        iv_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(
                View v) {
                HistoryActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    private void downloadLauncher() {
        Uri playUri =
            Uri.parse("https://play.google.com/store/apps/details?id=com.cooeeui.zenlauncher");
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, playUri);
        if (isPlayStoreInstalled()) {
            browserIntent
                .setClassName("com.android.vending", "com.android.vending.AssetBrowserActivity");
        }
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(browserIntent);
    }

    private boolean isPlayStoreInstalled() {
        String playPkgName = "com.android.vending";
        try {
            PackageInfo
                pckInfo =
                getPackageManager().getPackageInfo(playPkgName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 填充广告布局
     */
    private void fillTheAdLayout() {
        mRlAdUnit.setVisibility(View.VISIBLE);

        if (Constant.scanHistoryAd instanceof Campaign) {
            final ImageView nativeAdCover = (ImageView) findViewById(R.id.native_ad_cover);
            final ImageView nativeAdIcon = (ImageView) findViewById(R.id.native_ad_icon);
            final TextView nativeAdTitle = (TextView) findViewById(R.id.native_ad_title);
            final TextView nativeAdBody = (TextView) findViewById(R.id.native_ad_body);

            nativeAdBody.setText(((Campaign) Constant.scanHistoryAd).getAppDesc());
            nativeAdTitle.setText(((Campaign) Constant.scanHistoryAd).getAppName());

            Constant.mvNativeHandleForScanHistory
                .registerView(nativeAdCover, (Campaign) Constant.scanHistoryAd);

            final String urlIcon = ((Campaign) Constant.scanHistoryAd).getIconUrl();
            Picasso.with(this)
                .load(urlIcon)
                .skipMemoryCache()
                .error(R.drawable.wallpaper_default)
                .placeholder(R.drawable.wallpaper_default)
                .into(nativeAdIcon, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("Mobvista", "Picasso onSuccess urlIcon = " + urlIcon);
                    }

                    @Override
                    public void onError() {
                        Log.i("Mobvista", "Picasso onError urlIcon = " + urlIcon);
                    }
                });
            // Downloading and setting the ad icon.
            final String urlImage = ((Campaign) Constant.scanHistoryAd).getImageUrl();
            Picasso.with(this)
                .load(urlImage)
                .skipMemoryCache()
                .error(R.drawable.wallpaper_default)
                .placeholder(R.drawable.wallpaper_default)
                .into(nativeAdCover, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("Mobvista", "Picasso onSuccess urlImage = " + urlImage);
                    }

                    @Override
                    public void onError() {
                        Log.i("Mobvista", "Picasso onError urlImage = " + urlImage);
                    }
                });
        } else if (Constant.scanHistoryAd instanceof NativeAd) {
            // facebook ad begin
            ImageView nativeAdCover = (ImageView) findViewById(R.id.native_ad_cover);
            ImageView nativeAdIcon = (ImageView) findViewById(R.id.native_ad_icon);
            TextView nativeAdTitle = (TextView) findViewById(R.id.native_ad_title);
            TextView nativeAdBody = (TextView) findViewById(R.id.native_ad_body);
            Button nativeAdCallToAction = (Button) findViewById(R.id.native_ad_call_to_action);

            // Setting the Text
            nativeAdCallToAction
                .setText(((NativeAd) Constant.scanHistoryAd).getAdCallToAction());
            nativeAdCallToAction.setVisibility(View.VISIBLE);
            nativeAdTitle.setText(((NativeAd) Constant.scanHistoryAd).getAdTitle());
            nativeAdBody.setText(((NativeAd) Constant.scanHistoryAd).getAdBody());

            // Downloading and setting the ad icon.
            NativeAd.Image adIcon = ((NativeAd) Constant.scanHistoryAd).getAdIcon();
            NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

            NativeAd.Image adCoverImage =
                ((NativeAd) Constant.scanHistoryAd).getAdCoverImage();
            NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdCover);

            ((NativeAd) Constant.scanHistoryAd).registerViewForInteraction(mRlAdUnit);
            //facebook ad end
        } else if (Constant.scanHistoryAd instanceof KmobAdNativeData) {
            if (mRlAdUnit.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) mRlAdUnit.getLayoutParams();
                params.height =
                    getResources().getDimensionPixelSize(R.dimen.ad_unit_height_domestic);
                mRlAdUnit.setLayoutParams(params);
            } else if (mRlAdUnit.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mRlAdUnit.getLayoutParams();
                params.height =
                    getResources().getDimensionPixelSize(R.dimen.ad_unit_height_domestic);
                mRlAdUnit.setLayoutParams(params);
            }

            final ImageView nativeAdCover = (ImageView) findViewById(R.id.native_ad_cover);
            findViewById(R.id.bottom_mask).setVisibility(View.GONE);
            findViewById(R.id.native_ad_icon).setVisibility(View.GONE);
            findViewById(R.id.native_ad_call_to_action).setVisibility(View.GONE);

            final KmobAdNativeData nativeData = (KmobAdNativeData) Constant.scanHistoryAd;

            // 广告展示情况
            KmobManager.onNativeAdShow(nativeData.getAdplaceid(), nativeData.getAdid());

            nativeAdCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 广告点击情况
                    KmobManager
                        .onClickDone(nativeData.getAdid(), Constant.kmobAdJsonStrForScanHistory,
                                     false);
                }
            });
            String cimg = nativeData.getCtimg();
            String url = null;
            try {
                JSONArray ctimgArray = new JSONArray(cimg);
                if (ctimgArray != null && ctimgArray.length() > 0) {
                    JSONObject object = ctimgArray.getJSONObject(0);
                    url = object.getString("url");

                    final String finalUrl = url;
                    Picasso.with(this)
                        .load(url)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdCover, new Callback() {
                            @Override
                            public void onSuccess() {
                                Log.i("KmobAd", "Picasso onSuccess url = " + finalUrl);
                            }

                            @Override
                            public void onError() {
                                Log.i("KmobAd", "Picasso onError url = " + finalUrl);
                            }
                        });
                }
            } catch (Exception e) {

            }
        }
    }

    private void pollingAdList() {
        mHandler.postDelayed(mRunnable, 1000);
    }

}
