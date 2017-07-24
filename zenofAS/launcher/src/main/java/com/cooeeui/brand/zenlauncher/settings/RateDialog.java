package com.cooeeui.brand.zenlauncher.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.util.Calendar;
import java.util.Date;

public class RateDialog implements OnClickListener, OnDismissListener, OnKeyListener {

    private static final String GPURL = "https://play.google.com/store/apps/details?id=";

    private Context mContext;
    private AlertDialog mDialog;
    private RelativeLayout rlButtonEncourage;
    private RelativeLayout rlButtonFeedback;
    private RelativeLayout rlButtonLater;

    public static final int AUTO_REMIND_COUNT_MAX = 4;
    public int rateCount;
    private Date specifiedDate;
    private static final int THREE_DAYS = 3;

    public RateDialog(Context context) {
        this.mContext = context;
        mDialog = new AlertDialog.Builder(mContext).create();
    }

    public void showAlertDialog() {
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.setOnKeyListener(this);
        Window window = mDialog.getWindow();
        window.setContentView(R.layout.zen_setting_rate_dialog);
        window.setGravity(Gravity.CENTER);
        rlButtonEncourage = (RelativeLayout) window.findViewById(R.id.rl_rate_encourage);
        rlButtonEncourage.setOnClickListener(this);
        rlButtonFeedback = (RelativeLayout) window.findViewById(R.id.rl_rate_feedback);
        rlButtonFeedback.setOnClickListener(this);
        rlButtonLater = (RelativeLayout) window.findViewById(R.id.rl_rate_later);
        rlButtonLater.setOnClickListener(this);
        SettingPreference.setRateAutoRemindCount(SettingPreference.getRateAutoRemindCount() + 1);
        TextView textView = (TextView) window.findViewById(R.id.rate_button_encourage_text);
        String text = StringUtil.getString(mContext, R.string.zs_rate_button_encourage);
        textView.setText(text);
        textView = (TextView) window.findViewById(R.id.rate_button_feedback_text);
        text = StringUtil.getString(mContext, R.string.zs_rate_button_feedback);
        textView.setText(text);
        textView = (TextView) window.findViewById(R.id.rate_button_later_text);
        text = StringUtil.getString(mContext, R.string.zs_rate_button_later);
        textView.setText(text);
        textView = (TextView) window.findViewById(R.id.rate_launcher_text);
        text = StringUtil.getString(mContext, R.string.zen_launcher);
        textView.setText(text);
    }

    public boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        }
        return false;
    }

    public void cancel() {
        if (mDialog != null) {
            mDialog.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.rl_rate_encourage:
                SettingPreference.setRateAutoRemindCount(AUTO_REMIND_COUNT_MAX + 1);
                mDialog.cancel();
                skip2GooglePlay(mContext);

                // 五星好评鼓励一下
                break;

            case R.id.rl_rate_feedback:
                SettingPreference.setRateAutoRemindCount(AUTO_REMIND_COUNT_MAX + 1);
                // mContext.startActivity(new Intent(mContext,
                // ZenSettingFeedBack.class));
                Resources resources = mContext.getResources();
                String[] receiver = new String[]{
                    "nanolauncher@gmail.com"
                };
                String subject = "Feedback on Nano Launcher";
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("message/rfc822");
                // 设置邮件发收人
                email.putExtra(Intent.EXTRA_EMAIL, receiver);
                // 设置邮件标题
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                // 设置邮件内容
                email.putExtra(Intent.EXTRA_TEXT, "");
                // 调用系统的邮件系统
                mContext.startActivity(
                    Intent.createChooser(email,
                                         StringUtil.getString(mContext,
                                                              R.string.zs_Email_Feedback_Choose_Client)));
                mDialog.cancel();

                // 五星好评吐槽
                break;

            case R.id.rl_rate_later:
                rateLater();
                mDialog.cancel();

                // 五星好评以后再说
                break;
        }
    }

    public void rateLater() {
        rateCount = 0;
        if (specifiedDate == null) {
            specifiedDate = new Date(System.currentTimeMillis());
        }
        SettingPreference.setRateAutoRemindSpecifiedDate(System.currentTimeMillis());
        SettingPreference.setRateAutoRemindFirstTime(System.nanoTime());
    }


    private void skip2GooglePlay(Context context) {

        if (CommonUtil.isGooglePlayStoreInstalled(context)) {
            String url = GPURL + context.getPackageName();
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

    @SuppressWarnings("deprecation")
    public boolean isThreeDaysAgo() {
        if (specifiedDate != null) {
            specifiedDate.setTime(SettingPreference.getRateAutoRemindSpecifiedDate());
            Date date = new Date(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -THREE_DAYS);
            boolean rst = specifiedDate.before(new Date(
                (calendar.get(Calendar.YEAR) - 1900),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                date.getHours(), date.getMinutes(),
                date.getSeconds()));
            return rst;
        } else {
            return true;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // TODO Auto-generated method stub
        rateLater();
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            rateLater();
        }
        return false;
    }

}
