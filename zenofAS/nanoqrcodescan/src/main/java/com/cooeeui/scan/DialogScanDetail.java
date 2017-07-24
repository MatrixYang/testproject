package com.cooeeui.scan;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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


public class DialogScanDetail extends Activity implements OnClickListener {

    private RelativeLayout mRlAdUnit;
    private LinearLayout ll_dl_detail;
    private TextView tv_title;
    private TextView tv_content;
    private TextView btn_open;
    private TextView btn_copy;
    private TextView btn_share;
    private String content;
    private int contentType;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (Constant.scanResultAd != null) {
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
        setContentView(R.layout.dl_scan);
        getWindow()
            .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById();
        setlistener();
        init();
        if (Constant.scanResultAd != null) {
            fillTheAdLayout();
        } else {
            pollingAdList();
        }

        // 友盟推送push
        PushAgent.getInstance(this).onAppStart();
    }

    private void init() {
        content = getIntent().getStringExtra("content");
        contentType = getIntent().getIntExtra("type", 2);
        if (contentType == 1) {
            tv_title
                .setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(
                    R.drawable.browser_icon), null,
                                                         null, null);
            tv_title.setText(R.string.string_interlinkage);
        } else {
            tv_title
                .setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(
                    R.drawable.text_icon), null,
                                                         null, null);
            tv_title.setText(R.string.string_text);
        }
        tv_content.setText(content);
    }

    private void findViewById() {
        mRlAdUnit = (RelativeLayout) findViewById(R.id.ad_unit);
        ll_dl_detail = (LinearLayout) findViewById(R.id.ll_dl_detail);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_content = (TextView) findViewById(R.id.tv_content);
        btn_open = (TextView) findViewById(R.id.btn_open);
        btn_copy = (TextView) findViewById(R.id.btn_copy);
        btn_share = (TextView) findViewById(R.id.btn_share);
    }

    private void setlistener() {
        ll_dl_detail.setOnClickListener(this);
        btn_open.setOnClickListener(this);
        btn_copy.setOnClickListener(this);
        btn_share.setOnClickListener(this);
    }

    @Override
    public void onClick(
        View v) {
        switch (v.getId()) {
            case R.id.ll_dl_detail:
                break;
            case R.id.btn_open:
                if (contentType == 1) {
                    open(content);
                } else {
                    AlertDialog.Builder builder = new Builder(DialogScanDetail.this);
                    builder.setMessage(content);
                    builder.setTitle(getResources().getString(R.string.string_text));
                    builder.setNegativeButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;
            case R.id.btn_copy:
                copy(content);
                break;
            case R.id.btn_share:
                share();
                break;
        }
    }

    private void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(shareIntent);
    }

    private void copy(
        String content) {
        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content);
        Toast
            .makeText(getApplicationContext(), getString(R.string.copy_success), Toast.LENGTH_SHORT)
            .show();
    }

    private void open(
        String content) {
        Uri uri = Uri.parse(content);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    /**
     * 填充广告布局
     */
    private void fillTheAdLayout() {
        mRlAdUnit.setVisibility(View.VISIBLE);

        if (Constant.scanResultAd instanceof Campaign) {
            final ImageView nativeAdCover = (ImageView) findViewById(R.id.native_ad_cover);
            final ImageView nativeAdIcon = (ImageView) findViewById(R.id.native_ad_icon);
            final TextView nativeAdTitle = (TextView) findViewById(R.id.native_ad_title);
            final TextView nativeAdBody = (TextView) findViewById(R.id.native_ad_body);

            nativeAdBody.setText(((Campaign) Constant.scanResultAd).getAppDesc());
            nativeAdTitle.setText(((Campaign) Constant.scanResultAd).getAppName());

            if (contentType != 1) {
                btn_open.setOnClickListener(null);
                Constant.mvNativeHandleForScanResult
                    .registerView(btn_open, (Campaign) Constant.scanResultAd);
            }

            Constant.mvNativeHandleForScanResult
                .registerView(nativeAdCover, (Campaign) Constant.scanResultAd);

            final String urlIcon = ((Campaign) Constant.scanResultAd).getIconUrl();
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
            final String urlImage = ((Campaign) Constant.scanResultAd).getImageUrl();
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
        } else if (Constant.scanResultAd instanceof NativeAd) {
            // facebook ad begin
            ImageView nativeAdCover = (ImageView) findViewById(R.id.native_ad_cover);
            ImageView nativeAdIcon = (ImageView) findViewById(R.id.native_ad_icon);
            TextView nativeAdTitle = (TextView) findViewById(R.id.native_ad_title);
            TextView nativeAdBody = (TextView) findViewById(R.id.native_ad_body);
            Button nativeAdCallToAction = (Button) findViewById(R.id.native_ad_call_to_action);

            // Setting the Text
            nativeAdCallToAction
                .setText(((NativeAd) Constant.scanResultAd).getAdCallToAction());
            nativeAdCallToAction.setVisibility(View.VISIBLE);
            nativeAdTitle.setText(((NativeAd) Constant.scanResultAd).getAdTitle());
            nativeAdBody.setText(((NativeAd) Constant.scanResultAd).getAdBody());

            // Downloading and setting the ad icon.
            NativeAd.Image adIcon = ((NativeAd) Constant.scanResultAd).getAdIcon();
            NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

            NativeAd.Image adCoverImage =
                ((NativeAd) Constant.scanResultAd).getAdCoverImage();
            NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdCover);

            ((NativeAd) Constant.scanResultAd).registerViewForInteraction(mRlAdUnit);
            //facebook ad end
        } else if (Constant.scanResultAd instanceof KmobAdNativeData) {
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

            final KmobAdNativeData nativeData = (KmobAdNativeData) Constant.scanResultAd;

            // 广告展示情况
            KmobManager.onNativeAdShow(nativeData.getAdplaceid(), nativeData.getAdid());

            nativeAdCover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 广告点击情况
                    KmobManager
                        .onClickDone(nativeData.getAdid(), Constant.kmobAdJsonStrForScanResult,
                                     false);
                }
            });

            if (contentType != 1) {
                btn_open.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 广告点击情况
                        KmobManager
                            .onClickDone(nativeData.getAdid(), Constant.kmobAdJsonStrForScanResult,
                                         false);
                    }
                });
            }

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
