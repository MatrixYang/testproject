package com.cooeeui.brand.zenlauncher.widget.weatherclock;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class WeatherClockGroup extends LinearLayout {

    private LinearLayout mLinearGroupClock;
    private LinearLayout mLinearGroupDate;
    private ImageView mImageHourHigh;
    private ImageView mImageHourLow;
    private ImageView mImageMinuteHigh;
    private ImageView mImageMinuteLow;
    private TextView mTextDate;
    private static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;
    private static final String ACTION_TIME_CHANGED = Intent.ACTION_TIME_CHANGED;
    private static final String ACTION_TIME_TICK = Intent.ACTION_TIME_TICK;
    private MyTimeBroadCast myTimeBroadCast = null;
    private IntentFilter intentFilter;
    private Context mContext;
    private ClickIntent clickIntent = null;
    private boolean isRegister = false;
    public static final String TAG_CLOCK = "clock";
    public static final String TAG_CALENDAR = "calendar";
    private int mLastHourHigh = -1;
    private int mLasthourLow = -1;
    private int mLastminuteHigh = -1;
    private int mLastminuteLow = -1;
    private int[] clockImages = new int[]{
        R.drawable.time_0, R.drawable.time_1, R.drawable.time_2, R.drawable.time_3,
        R.drawable.time_4, R.drawable.time_5, R.drawable.time_6, R.drawable.time_7,
        R.drawable.time_8, R.drawable.time_9
    };
    private int mLastWeek = -1;
    private int mLastMonth = -1;
    private int mLastDay = -1;
    ArrayList<String> listWeeks = new ArrayList<>();
    ArrayList<String> listMonths = new ArrayList<>();
    private String days;

    public WeatherClockGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.home_clock, this);
        initString();
    }


    private void initString() {
        listWeeks.clear();
        listWeeks.add(StringUtil.getString(mContext, R.string.sevenweek_full));
        listWeeks.add(StringUtil.getString(mContext, R.string.firstweek_full));
        listWeeks.add(StringUtil.getString(mContext, R.string.secondweek_full));
        listWeeks.add(StringUtil.getString(mContext, R.string.thirdweek_full));
        listWeeks.add(StringUtil.getString(mContext, R.string.forthweek_full));
        listWeeks.add(StringUtil.getString(mContext, R.string.fiveweek_full));
        listWeeks.add(StringUtil.getString(mContext, R.string.sixweek_full));

        listMonths.clear();
        listMonths.add(StringUtil.getString(mContext, R.string.first_month));
        listMonths.add(StringUtil.getString(mContext, R.string.second_month));
        listMonths.add(StringUtil.getString(mContext, R.string.third_month));
        listMonths.add(StringUtil.getString(mContext, R.string.forth_month));
        listMonths.add(StringUtil.getString(mContext, R.string.fifth_month));
        listMonths.add(StringUtil.getString(mContext, R.string.sixth_month));
        listMonths.add(StringUtil.getString(mContext, R.string.seventh_month));
        listMonths.add(StringUtil.getString(mContext, R.string.eighth_month));
        listMonths.add(StringUtil.getString(mContext, R.string.ninth_month));
        listMonths.add(StringUtil.getString(mContext, R.string.tenth_month));
        listMonths.add(StringUtil.getString(mContext, R.string.eleventh_month));
        listMonths.add(StringUtil.getString(mContext, R.string.twelfth_month));

        days = StringUtil.getString(mContext, R.string.date_day);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        clickIntent = new ClickIntent(mContext);
        mLinearGroupClock = (LinearLayout) findViewById(R.id.ll_group_clock);
        mLinearGroupClock.setTag(TAG_CLOCK);
        mLinearGroupClock.setOnClickListener(clickIntent);

        mLinearGroupDate = (LinearLayout) findViewById(R.id.ll_group_date);
        mLinearGroupDate.setTag(TAG_CALENDAR);
        mLinearGroupDate.setOnClickListener(clickIntent);

        mImageHourHigh = (ImageView) findViewById(R.id.iv_hour_high);
        mImageHourLow = (ImageView) findViewById(R.id.iv_hour_low);
        mImageMinuteHigh = (ImageView) findViewById(R.id.iv_minute_high);
        mImageMinuteLow = (ImageView) findViewById(R.id.iv_minute_low);
        mTextDate = (TextView) findViewById(R.id.tv_date);

        myTimeBroadCast = new MyTimeBroadCast();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATE_CHANGED);
        intentFilter.addAction(ACTION_TIME_CHANGED);
        intentFilter.addAction(ACTION_TIME_TICK);
    }

    public void register() {
        if (!isRegister) {
            isRegister = true;
            mContext.registerReceiver(myTimeBroadCast, intentFilter);
        }
    }

    public void unRegister() {
        if (isRegister) {
            isRegister = false;
            mContext.unregisterReceiver(myTimeBroadCast);
        }
    }

    public void show() {
        setVisibility(View.VISIBLE);
        changeTimeAndDate();
        register();
    }

    public void hide() {
        setVisibility(View.INVISIBLE);
        unRegister();
    }

    public void changeTimeAndDate(boolean force) {
        initString();
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int week = c.get(Calendar.DAY_OF_WEEK);

        setClockImageByTime(hour, minute);
        setDateTextByTime(week, month, day, force);
    }

    public void changeTimeAndDate() {
        changeTimeAndDate(false);
    }

    private void setClockImageByTime(int hour, int minute) {
        ContentResolver cv = Launcher.getInstance().getContentResolver(); // 获取当前系统设置
        String strTimeFormat = android.provider.Settings.System.getString(cv,
                                                                          android.provider.Settings.System.TIME_12_24);// 获取当前手机时间制式是24小时制式还是12小时制式
        int hourHigh;
        int hourLow;
        if ("24".equals(strTimeFormat)) {// 手机时间制式为24小时制式时
            hourHigh = hour / 10;
            hourLow = hour % 10;
        } else {// 手机时间制式为12小时制式时由24小时制式转化为12小时制式
            hourHigh = (hour % 12 == 0 ? 12 : hour % 12) / 10;
            hourLow = (hour % 12 == 0 ? 12 : hour % 12) % 10;
        }

        if (mLastHourHigh != hourHigh) {
            mLastHourHigh = hourHigh;
            mImageHourHigh.setBackgroundResource(clockImages[hourHigh]);
        }
        if (mLasthourLow != hourLow) {
            mLasthourLow = hourLow;
            mImageHourLow.setBackgroundResource(clockImages[hourLow]);
        }

        int minuteHigh = minute / 10;
        int minuteLow = minute % 10;
        if (mLastminuteHigh != minuteHigh) {
            mLastminuteHigh = minuteHigh;
            mImageMinuteHigh.setBackgroundResource(clockImages[minuteHigh]);
        }
        if (mLastminuteLow != minuteLow) {
            mLastminuteLow = minuteLow;
            mImageMinuteLow.setBackgroundResource(clockImages[minuteLow]);
        }
    }

    public void setDateTextByTime(int week, int month, int day, boolean force) {
        if (force || mLastWeek != week || mLastMonth != month || mLastDay != day) {
            mLastWeek = week;
            mLastMonth = month;
            mLastDay = day;
            String text = listWeeks.get(week - 1) + " " + " " + " " + listMonths.get(month) + day + days;
            mTextDate.setText(text);
        }
    }
    private class MyTimeBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_TIME_TICK) || action.equals(ACTION_DATE_CHANGED)
                || action.equals(ACTION_TIME_CHANGED)
                || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                changeTimeAndDate();
            }
        }

    }

    public void setup(Launcher launcher) {
        mLinearGroupClock.setOnLongClickListener(launcher);
        mLinearGroupDate.setOnLongClickListener(launcher);
        clickIntent.setup(launcher);
    }
}
