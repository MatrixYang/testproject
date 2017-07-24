package com.cooeeui.zenlauncher.common.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsageUtil;
import com.cooeeui.brand.zenlauncher.http.NotificationDownloadTask;
import com.cooeeui.brand.zenlauncher.mobvista.MobvistaCampaignInfo;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.brand.zenlauncher.searchbar.SearchBarGroup;
import com.cooeeui.brand.zenlauncher.settings.VersionUpdateNotification;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.mobvista.msdk.out.MvNativeHandler;
import com.umeng.analytics.MobclickAgent;

public class AlertDialogUtil {

    private final String GPURL = "https://play.google.com/store/apps/details?id=";
    private final String NOTIFICATION_SERVICE_PACKAGE_NAME = "com.cooeeui.notificationservice";

    private AlertDialog mAlertDialog;
    private Activity mLauncher;
    private TextView tv_Download;
    private TextView tv_Cancel;
    private LinearLayout mVersionDetail;
    private TextView mVersionName;
    private String mVersionStr;
    private TextView mVersionUpdate;
    private TextView mVersionUpdateCancel;

    private String nationalPlugURL = "http://www.coolauncher.cn/zen/NotificationService.apk";
    private static int NOTIFICATIONID = 11;
    private MvNativeHandler mNativeHandle;
    private MobvistaCampaignInfo mCampaignInfo;

    public void setMobVistaNativeAdUse(MvNativeHandler nativeHandle, MobvistaCampaignInfo campaignInfo) {
        mNativeHandle = nativeHandle;
        mCampaignInfo = campaignInfo;
    }

    public enum AlertDialogType {
        TYPE_NOTIFICATION,
        TYPE_FAVORITE_PROMPT,
        TYPE_FAVORITE_OK,
        TYPE_VERSION_UPDATE,
        TYPE_FAVORITE_SCAN,
        TYPE_WIDGET_MOBVISTA_NATIVE_AD
    }

    public AlertDialogUtil(Activity activity) {
        mLauncher = activity;
    }


    /**
     * @param cancel Whether the dialog should be canceled when touched outside the window.
     */
    public void showAlertDialog(boolean cancel, final boolean keyEnable, AlertDialogType type,
                                int layoutId) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(mLauncher).create();
        }

        mAlertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyEnable) {
                    return false;
                } else {
                    return true;
                }

            }
        });
        mAlertDialog.setCanceledOnTouchOutside(cancel);
        mAlertDialog.show();
        Window window = mAlertDialog.getWindow();
        window.setContentView(layoutId);
        window.setGravity(Gravity.CENTER_HORIZONTAL);
        switch (type) {
            case TYPE_NOTIFICATION:
                initString(window);
                break;
            case TYPE_FAVORITE_PROMPT:
                initOpenAvtivityString(window);
                break;
            case TYPE_FAVORITE_OK:
                initJustOkString(window);
                break;
            case TYPE_VERSION_UPDATE:
                init(window);
                break;
            case TYPE_FAVORITE_SCAN:
                initScan(window);
                break;
            case TYPE_WIDGET_MOBVISTA_NATIVE_AD:
                initNativeAdAlert(window);
                break;
        }
    }

    private void initNativeAdAlert(Window window) {
        ImageView iv_banner = (ImageView) window.findViewById(R.id.iv_banner);
        ImageView iv_icon = (ImageView) window.findViewById(R.id.iv_icon);
        TextView tv_appDesc = (TextView) window.findViewById(R.id.tv_appDesc);
        TextView tv_appName = (TextView) window.findViewById(R.id.tv_appName);

        iv_banner.setImageBitmap(mCampaignInfo.getBannerBitmap());
        iv_icon.setImageBitmap(mCampaignInfo.getIconBitmap());
        tv_appDesc.setText(mCampaignInfo.getCampaign().getAppDesc());
        tv_appName.setText(mCampaignInfo.getCampaign().getAppName());

        Button bt_showDetail = (Button) window.findViewById(R.id.bt_showDetail);
        mNativeHandle.registerView(bt_showDetail, mCampaignInfo.getCampaign());
    }

    public boolean isAlertDialogShowing() {
        if (mAlertDialog != null) {
            return mAlertDialog.isShowing();
        }

        return false;
    }

    public void cancel() {
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
        }
    }


    private void initOpenAvtivityString(Window window) {
        ImageView zen_logo = (ImageView) window.findViewById(R.id.zen_logo);
        zen_logo.setVisibility(View.GONE);
        TextView textView = (TextView) window.findViewById(R.id.alter_plugin_name);
        textView.setVisibility(View.GONE);

        textView = (TextView) window.findViewById(R.id.altert_launcher_text);
        String text = StringUtil.getString(mLauncher, R.string.usage_notice);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.alter_content);
        text = StringUtil.getString(mLauncher, R.string.usage_alter_content_setting);
        textView.setText(text);

        tv_Download = (TextView) window.findViewById(R.id.tv_download);
        text = StringUtil.getString(mLauncher, R.string.usage_alter_go_now);
        tv_Download.setText(text);
        tv_Download.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoActivity();
                mAlertDialog.cancel();
            }
        });

        tv_Cancel = (TextView) window.findViewById(R.id.tv_cancel);
        text = StringUtil.getString(mLauncher, R.string.usage_alter_later);
        tv_Cancel.setText(text);
        tv_Cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });
    }

    private void initString(Window window) {
        TextView textView = (TextView) window.findViewById(R.id.alter_plugin_name);
        String text = StringUtil.getString(mLauncher, R.string.alter_plugin_name);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.altert_launcher_text);
        text = StringUtil.getString(mLauncher, R.string.zen_launcher);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.alter_content);
        text = StringUtil.getString(mLauncher, R.string.alter_content);
        textView.setText(text);

        tv_Download = (TextView) window.findViewById(R.id.tv_download);
        text = StringUtil.getString(mLauncher, R.string.alter_download);
        tv_Download.setText(text);
        tv_Download.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoDownloadAPK();
                mAlertDialog.cancel();
            }
        });

        tv_Cancel = (TextView) window.findViewById(R.id.tv_cancel);
        text = StringUtil.getString(mLauncher, R.string.alter_cancel);
        tv_Cancel.setText(text);
        tv_Cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });
    }

    private void initJustOkString(Window window) {
        ImageView zen_logo = (ImageView) window.findViewById(R.id.zen_logo);
        zen_logo.setVisibility(View.GONE);
        TextView textView = (TextView) window.findViewById(R.id.alter_plugin_name);
        textView.setVisibility(View.GONE);

        textView = (TextView) window.findViewById(R.id.altert_launcher_text);
        String text = StringUtil.getString(mLauncher, R.string.usage_notice);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.alter_content);
        text = StringUtil.getString(mLauncher, R.string.usage_alter_content_nano);
        textView.setText(text);

        tv_Download = (TextView) window.findViewById(R.id.tv_download);
        text = StringUtil.getString(mLauncher, R.string.usage_alter_ok);
        tv_Download.setText(text);
        tv_Download.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });

        tv_Cancel = (TextView) window.findViewById(R.id.tv_cancel);
        text = StringUtil.getString(mLauncher, R.string.usage_alter_later);
        tv_Cancel.setText(text);
        tv_Cancel.setVisibility(View.GONE);
    }

    private void init(Window window) {
        mVersionDetail = (LinearLayout) window.findViewById(R.id.ll_version_info_detail);
        mVersionName = (TextView) window.findViewById(R.id.tv_alert_head_version);
        final CheckBox checkBox = (CheckBox) window.findViewById(R.id.cb_version_update_check);

        TextView textView = (TextView) window.findViewById(R.id.tv_alert_head_launcher);
        String text = StringUtil.getString(mLauncher, R.string.zen_launcher);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.tv_version_update_prompt);
        text = StringUtil.getString(mLauncher, R.string.version_update_prompt);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.tv_update_checkbox);
        text = StringUtil.getString(mLauncher, R.string.version_update_checkbox);
        textView.setText(text);

        mVersionUpdate = (TextView) window.findViewById(R.id.tv_version_update);
        text = StringUtil.getString(mLauncher, R.string.version_update);
        mVersionUpdate.setText(text);
        mVersionUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                skip2GooglePlay(mLauncher,mLauncher.getPackageName());
                mAlertDialog.cancel();
                //自更新更新按钮点击次数
                MobclickAgent.onEvent(mLauncher, "Autoupdateclick");
            }
        });

        mVersionUpdateCancel = (TextView) window.findViewById(R.id.tv_version_update_cancel);
        text = StringUtil.getString(mLauncher, R.string.alter_cancel);
        mVersionUpdateCancel.setText(text);
        mVersionUpdateCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    VersionUpdateNotification.clearNotification(mLauncher);
                }
                LauncherPreference.setVersionUpdateForgetStatus(true);
                int version = CommonUtil.versionNameString2Int(mVersionStr);
                LauncherPreference.setVersionUpdateForgotVersion(version);
                mAlertDialog.cancel();
                //自更新取消按钮点击次数
                MobclickAgent.onEvent(mLauncher, "Autoupdatelater");
            }
        });
    }

    /**
     * 二维码dialog
     */
    private void initScan(Window window) {
        //二维码描述
        TextView textView = (TextView) window.findViewById(R.id.tv_version_update_prompt);
        String text = StringUtil.getString(mLauncher, R.string.tv_scan_prompt);
        textView.setText(text);

        textView = (TextView) window.findViewById(R.id.tv_Later);
        text = StringUtil.getString(mLauncher, R.string.tv_scan_later);
        textView.setText(text);

        tv_Download = (TextView) window.findViewById(R.id.tv_Download);
        text = StringUtil.getString(mLauncher, R.string.tv_scan_download);
        tv_Download.setText(text);
        tv_Download.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                skip2GooglePlay(mLauncher, SearchBarGroup.SCAN_PACKAGENAME);
                mAlertDialog.cancel();
                //友盟统计二维码点击下载次数
                MobclickAgent.onEvent(mLauncher, "QRCodeDownload");
            }
        });

        tv_Cancel = (TextView) window.findViewById(R.id.tv_Later);
        tv_Cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });

    }

    ///
    public void fillUpdateVersionDetail(String versionName, String versionInfo) {
        mVersionStr = versionName;
        mVersionName.setText("V " + versionName);

        if (mVersionDetail != null) {

            if (versionInfo == null || versionInfo.equals("")) {
                return;
            }

            mVersionDetail.setVisibility(View.VISIBLE);

            String detail[] = versionInfo.split(";");
            TextView detailTitle = new TextView(mLauncher);
            LinearLayout.LayoutParams paraTitle = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            int marginLeft = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.version_update_detail_title_marginLeft);
            int textSize = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.version_update_detail_title);
            paraTitle.setMargins(marginLeft, 0, marginLeft, 0); //left,top,right, bottom
            detailTitle.setLayoutParams(paraTitle);
            detailTitle.setText(detail[0]);
            detailTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            detailTitle.setTextColor(0xff545860);
            mVersionDetail.addView(detailTitle);

            int marginTop = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.version_update_detail_item_marginTop);
            marginLeft = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.version_update_detail_item_marginLeft);
            textSize = mLauncher.getResources()
                .getDimensionPixelSize(R.dimen.version_update_detail_item);
            LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            for (int i = 1; i < detail.length; i++) {
                TextView detailItem = new TextView(mLauncher);
                para.setMargins(marginLeft, marginTop, marginLeft, 0); //left,top,right, bottom
                detailItem.setLayoutParams(para);
                detailItem.setText(detail[i]);
                detailItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                detailItem.setTextColor(0xff7e8085);
                mVersionDetail.addView(detailItem);
            }

        }
    }


    private void gotoDownloadAPK() {
        if (FlavorController.National) {
            new NotificationDownloadTask(mLauncher, NOTIFICATIONID++,
                                         StringUtil.getString(mLauncher,
                                                              R.string.alter_plugin_name),
                                         R.mipmap.ic_launcher).execute(nationalPlugURL);
        } else if (CommonUtil.isGooglePlayStoreInstalled(mLauncher)) {
            String url = GPURL + NOTIFICATION_SERVICE_PACKAGE_NAME;
            boolean rst = CommonUtil.openWithGooglePlayStore(mLauncher, url);
            if (!rst) {
                Toast.makeText(mLauncher,
                               StringUtil.getString(mLauncher, R.string.activity_not_found),
                               Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mLauncher,
                           StringUtil.getString(mLauncher, R.string.google_play_not_install),
                           Toast.LENGTH_LONG).show();
        }
    }

    private void skip2GooglePlay(Context context,String packageName) {

        if (CommonUtil.isGooglePlayStoreInstalled(context)) {
            String url = GPURL +packageName;
            boolean rst = CommonUtil.openWithGooglePlayStore(context, url);
            if (!rst) {
                Toast.makeText(context, StringUtil.getString(context, R.string.activity_not_found),
                               Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, StringUtil.getString(context, R.string.google_play_not_install),
                           Toast.LENGTH_LONG).show();
        }
    }

    private void gotoActivity() {
        UsageUtil.startUsageSettingActivity(mLauncher, Launcher.REQUEST_USAGE_SETTING_ALERT);
        Intent intent = new Intent(LauncherConstants.ACTION_USAGE_SETTING_TIP_SHOW);
        mLauncher.sendBroadcast(intent);
    }

}
