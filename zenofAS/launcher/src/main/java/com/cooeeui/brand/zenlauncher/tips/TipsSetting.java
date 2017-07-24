package com.cooeeui.brand.zenlauncher.tips;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cooeeui.basecore.utilities.DateUtil;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TipsSetting extends BaseActivity implements OnClickListener,
                                                         OnCheckedChangeListener,
                                                         SwitchPagedView.onPageChangedListener,
                                                         LauncherAppState.OnUnlockTimeChangedListener {

    private FrameLayout mTipsBack;
    private ToggleButton mTipsSwitch;

    private SwitchPagedView mSwitchPagedView;

    private ListView mListView_day;
    private Gallery gallery_dates;

    SimpleGalleryAdapter mSimpleGalleryAdapter;
    TopListAdapter mTopListAdapter;

    private int records = 0;
    public static final int GETREORDS = 11;
    public static final int SETVIEW = 22;
    public static final int SETDATA = 33;
    public static final int DIALOGSHOW = 44;
    public static final int DIALOGDISMISS = 55;

    private ArrayList<String> datelist = new ArrayList<String>();
    private MyHandler mHandler;
    public static int mCurrentPagePosition = 0;

    // 设置圆的信息
    public void setCircleInfo(View view, int Delta) {
        TextCircleViewInfo tipCircleInfo = null;
        if (Delta == 0) {
            tipCircleInfo = new TextCircleViewInfo();
            tipCircleInfo.setPhone_time(TipsPopup.userTime);
            tipCircleInfo.setUnlock_times(TipsPopup.unlockCount);
        } else {
            tipCircleInfo = TipsSettingDataUtil.geteTipCircleInfoByTime(this, Delta);
        }
        float phone_time = tipCircleInfo.getPhone_timeWithMinute();
        String unit;
        String value = "";
        if (phone_time > 59) {
            float f = (float) (Math.round(phone_time / 60 * 10)) / 10;
            value = String.valueOf(f);
            unit = StringUtil.getString(this, R.string.tips_phonetime_unit_hour);
        } else {
            float f = (float) (Math.round(phone_time * 10)) / 10;
            value = String.valueOf(f);
            unit = StringUtil.getString(this, R.string.tips_phonetime_unit_min);
        }
        double d = Double.valueOf(value);
        int a = (int) d;
        if (a == d) {
            value = a + "";
        }
        ((TextCircleView) view.findViewById(R.id.unlocktimes)).setText(String.valueOf(tipCircleInfo
                                                                                          .getUnlock_times()));
        ((TextCircleView) view.findViewById(R.id.phonetimes)).setText(value);
        ((TextView) view.findViewById(R.id.tip_phonetime_unit_text)).setText(unit);
        ((TextCircleView) view.findViewById(R.id.unlocktimes)).setColor(getResources().getColor(
            tipCircleInfo.getUnlockColorByType()));
        ((TextCircleView) view.findViewById(R.id.phonetimes)).setColor(getResources().getColor(
            tipCircleInfo.getPhoneTimeColor()));
    }

    class MyHandler extends Handler {

        private WeakReference<TipsSetting> mOuter;

        public MyHandler(TipsSetting activity) {
            // 弱引用
            mOuter = new WeakReference<TipsSetting>(activity);
        }

        public void handleMessage(Message msg) {
            TipsSetting mq = mOuter.get();
            if (mq == null) {
                return;
            }

            switch (msg.what) {
                case DIALOGSHOW:
                    Dialog dialog = new Dialog(mq);
                    dialog.setTitle("title");
                    dialog.show();
                    break;
                case DIALOGDISMISS:
                    break;
                case GETREORDS:
                    // 查出记录总数
                    new AsyncTask<TipsSetting, Integer, String>() {
                        TipsSetting ts = null;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected String doInBackground(TipsSetting... params) {
                            ts = params[0];
                            return TipsSettingDataUtil.getMinTime(ts);
                        }

                        @Override
                        protected void onPostExecute(String mintime) {
                            super.onPostExecute(mintime);
                            // 用户初次安装，数据库还未有记录
                            if (mintime == null) {
                                ts.records = 1;
                            } else {
                                ts.records = DateUtil.getIntervalDays(DateUtil.getNowTime(),
                                                                      mintime) + 1;
                            }
                            // 加载gallery记录
                            obtainMessage(SETVIEW).sendToTarget();
                        }
                    }.execute(mq);
                    break;
                case SETVIEW:
                    mCurrentPagePosition = mq.records - 1;
                    for (int i = 0; i < mq.records; i++) {
                        int Delta = mq.records - 1 - i;
                        mq.datelist.add(DateUtil.getMonthByDalterDay(-Delta));
                        View view = LayoutInflater.from(mq).inflate(R.layout.tip_day, null);
                        TextView textView = (TextView) view.findViewById(R.id.unlocktime_tip);
                        String text = StringUtil.getString(mq, R.string.tips_unlocktimes);
                        textView.setText(text);
                        textView = (TextView) view.findViewById(R.id.unlocktime_tip_unit_text);
                        text = StringUtil.getString(mq, R.string.tips_unlocktimes_unit);
                        textView.setText(text);
                        textView = (TextView) view.findViewById(R.id.phonetime_tip);
                        text = StringUtil.getString(mq, R.string.tips_phonetime);
                        textView.setText(text);
                        textView = (TextView) view.findViewById(R.id.tip_phonetime_unit_text);
                        text = StringUtil.getString(mq, R.string.tips_phonetime_unit_hour);
                        textView.setText(text);
                        textView = (TextView) view.findViewById(R.id.top_title_text);
                        text = StringUtil.getString(mq, R.string.tips_top_title);
                        textView.setText(text);
                        textView = (TextView) view.findViewById(R.id.myempty);
                        text = StringUtil.getString(mq, R.string.tips_nodata_warning);
                        textView.setText(text);
                        mq.mSwitchPagedView.addView(view);
                    }
                    mq.mSimpleGalleryAdapter = new SimpleGalleryAdapter(mq, mq.datelist);
                    mq.gallery_dates.setAdapter(mq.mSimpleGalleryAdapter);
                    mq.gallery_dates.setSelection(mq.records - 1);
                    mq.mSwitchPagedView.setCurrentPage(mq.records - 1);
                    obtainMessage(SETDATA, mq.records - 1).sendToTarget();
                    break;
                case SETDATA:
                    final int position = (Integer) msg.obj;
                    final View child = mq.mSwitchPagedView.getChildAt(position);
                    mq.mListView_day = (ListView) child.findViewById(R.id.top_list_day);

                    new AsyncTask<TipsSetting, Integer, ArrayList<TopAppInfo>>() {
                        int Delta = 0;
                        TipsSetting mq = null;

                        @Override
                        protected ArrayList<TopAppInfo> doInBackground(TipsSetting... params) {
                            mq = params[0];
                            Delta = mq.records - 1 - position;
                            ArrayList<TopAppInfo> topAppInfoCurrent = null;
                            try {
                                // 判断长度，设置为空
                                topAppInfoCurrent = TipsSettingDataUtil.getTopAppInfoByDay(mq,
                                                                                           -Delta);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return topAppInfoCurrent;
                        }

                        @Override
                        protected void onPostExecute(ArrayList<TopAppInfo> topAppInfoDay) {
                            super.onPostExecute(topAppInfoDay);
                            if (topAppInfoDay.isEmpty()) {
                                mq.mListView_day.setAdapter(null);
                                TextView emptyview = (TextView) mq.findViewById(R.id.myempty);
                                mq.mListView_day.setEmptyView(emptyview);
                            } else {
                                mq.mTopListAdapter = new TopListAdapter(mq, topAppInfoDay);
                                mq.mListView_day.setAdapter(mq.mTopListAdapter);
                            }
                            mq.setCircleInfo(child, -Delta);
                        }
                    }.execute(mq);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_setting);
        mHandler = new MyHandler(TipsSetting.this);
        mTipsBack = (FrameLayout) findViewById(R.id.tips_back);
        mTipsSwitch = (ToggleButton) findViewById(R.id.tips_switch);
        mTipsSwitch.setChecked(SettingPreference.getTips());
        mTipsBack.setOnClickListener(this);
        mTipsSwitch.setOnCheckedChangeListener(this);

        TextView textView = (TextView) findViewById(R.id.tips_setting_text);
        String text = StringUtil.getString(this, R.string.tips_setting);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.tips_switch_name);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.sub_title);
        text = StringUtil.getString(this, R.string.tips_setting_subtitle);
        textView.setText(text);

        mSwitchPagedView = (SwitchPagedView) findViewById(R.id.switch_page);
        mSwitchPagedView.setOnPageChangedListener(this);

        this.gallery_dates = (Gallery) findViewById(R.id.gallery_dates);
        this.gallery_dates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TipsSetting.this.mSimpleGalleryAdapter.setSelectItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.gallery_dates.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSwitchPagedView.snapToPage(position);
            }
        });
        mHandler.obtainMessage(GETREORDS).sendToTarget();
    }

//    private void getTestData(ArrayList<TopAppInfo> topAppsDay) {
//        String s = "Day";
//        for (int i = 0; i < 10; i++) {
//            TopAppInfo object = new TopAppInfo();
//            object.setAppName(s + i);
//            object.setAppIcon(getResources().getDrawable(R.drawable.ic_launcher));
//            object.setMax(100);
//            object.setAppUsedTime(i + 10);
//            topAppsDay.add(object);
//        }
//    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        switch (button.getId()) {
            case R.id.tips_switch:
                SettingPreference.setTips(isChecked);
                if (isChecked) {
                    // 智能提醒设置页面中开关打开次数
                    MobclickAgent.onEvent(this, "SmartReminderOpen");
                } else {
                    // 智能提醒设置页面中开关关闭次数
                    MobclickAgent.onEvent(this, "SmartReminderClose");
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tips_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void setCurrentPageDate(int position) {
        if (mCurrentPagePosition != position) {
            mCurrentPagePosition = position;
            this.gallery_dates.setSelection(position);
            mHandler.obtainMessage(SETDATA, position).sendToTarget();
        }
    }

    @Override
    protected void onPause() {
        LauncherAppState.isNeedRefresh = true;
        LauncherAppState.mOnUnlockTimeChangedListener = this;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LauncherAppState.isNeedRefresh = false;
        LauncherAppState.mOnUnlockTimeChangedListener = null;
        super.onDestroy();
    }

    @Override
    public void onUnlockChanged() {
        mHandler.obtainMessage(SETDATA, mCurrentPagePosition).sendToTarget();
    }

}
