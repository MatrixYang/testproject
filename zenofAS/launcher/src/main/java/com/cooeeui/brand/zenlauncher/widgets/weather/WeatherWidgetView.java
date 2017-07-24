package com.cooeeui.brand.zenlauncher.widgets.weather;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.NetworkAvailableUtils;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.NumberClockHelper;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.Parameter;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.WeatherConditionCodes;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.WeatherCurveActivity;
import com.cooeeui.brand.zenlauncher.widgets.NanoWidgetUtils;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/3/10.
 */
public class WeatherWidgetView extends FrameLayout implements View.OnClickListener {

    private Context mContext;
    private View mainView;
    private ImageView widget_refresh;
    private ImageView widget_unknow;
    private ImageView widget_fu_tmp0;
    private ImageView widget_fu_tmp1;
    private ImageView widget_fu_tmp2;
    private ImageView widget_fu_unit;
    private ImageView widget_tmp1;
    private ImageView widget_tmp2;
    private ImageView widget_unit;
    private TextView curr_condition;
    private TextView curr_temperature;
    private TextView first_week;
    private ImageView first_condition;
    private TextView first_tmp;
    private TextView second_week;
    private ImageView second_condition;
    private TextView second_tmp;
    private TextView third_week;
    private ImageView third_condition;
    private TextView third_tmp;
    private SharedPreferences sharepreference = null;
    private ObjectAnimator animator;
    private int widgetId;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NumberClockHelper.ACTION_SAVE_WEATHER_DATA_FINISH.equals(action)) {
                updateWeatherView();
            } else if (NanoWidgetUtils.ACTION_WIDGET_DELETE.equals(action)) {
                if (intent.getIntExtra("widget_id", 0) == widgetId) {
                    Log.v("ACTION_WIDGET_DELETE", "WeatherWidgetView ACTION_WIDGET_DELETE");
                    finish();
                }
            }
        }
    };

    public WeatherWidgetView(Context context, int id) {
        super(context);
        mContext = context;
        widgetId = id;
        mainView = LayoutInflater.from(context).inflate(R.layout.weather_widget_layout, null);
        widget_refresh = (ImageView) mainView.findViewById(R.id.widget_refresh);
        widget_unknow = (ImageView) mainView.findViewById(R.id.widget_unknow);
        widget_fu_tmp0 = (ImageView) mainView.findViewById(R.id.widget_fu_tmp0);
        widget_fu_tmp1 = (ImageView) mainView.findViewById(R.id.widget_fu_tmp1);
        widget_fu_tmp2 = (ImageView) mainView.findViewById(R.id.widget_fu_tmp2);
        widget_fu_unit = (ImageView) mainView.findViewById(R.id.widget_fu_unit);
        widget_tmp1 = (ImageView) mainView.findViewById(R.id.widget_zheng_tmp1);
        widget_tmp2 = (ImageView) mainView.findViewById(R.id.widget_zheng_tmp2);
        widget_unit = (ImageView) mainView.findViewById(R.id.widget_zheng_unit);
        curr_condition = (TextView) mainView.findViewById(R.id.curr_condition);
        curr_temperature = (TextView) mainView.findViewById(R.id.curr_temperature);
        first_week = (TextView) mainView.findViewById(R.id.first_week);
        first_condition = (ImageView) mainView.findViewById(R.id.first_condition);
        first_tmp = (TextView) mainView.findViewById(R.id.first_tmp);
        second_week = (TextView) mainView.findViewById(R.id.second_week);
        second_condition = (ImageView) mainView.findViewById(R.id.second_condition);
        second_tmp = (TextView) mainView.findViewById(R.id.second_tmp);
        third_week = (TextView) mainView.findViewById(R.id.third_week);
        third_condition = (ImageView) mainView.findViewById(R.id.third_condition);
        third_tmp = (TextView) mainView.findViewById(R.id.third_tmp);
        addView(mainView);
        sharepreference = PreferenceManager
            .getDefaultSharedPreferences(context);

        updateWeatherView();

        setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(NumberClockHelper.ACTION_SAVE_WEATHER_DATA_FINISH);
        filter.addAction(NanoWidgetUtils.ACTION_WIDGET_DELETE);
        context.registerReceiver(mBroadcastReceiver, filter);
    }

    private void updateWeatherView() {
        if (sharepreference.getBoolean("numberweatherstate", false)) {
            String unit = null;
            unit = sharepreference.getString(Parameter.currentunit, Parameter.DEFAULT_UNIT);
            if (Parameter.UNIT_F.equals(unit)) {
                unit = Parameter.SHOW_UNIT_F;
                widget_fu_unit.setImageResource(R.drawable.widget_unit_f);
                widget_unit.setImageResource(R.drawable.widget_unit_f);
            } else if (Parameter.UNIT_C.equals(unit)) {
                unit = Parameter.SHOW_UNIT_C;
                widget_fu_unit.setImageResource(R.drawable.widget_unit_c);
                widget_unit.setImageResource(R.drawable.widget_unit_c);
            }
            String currTmp = sharepreference.getString("numberweathercurrenttmp",
                                                       null);
            Log.v("WeatherWidgetView", "currTmp = " + currTmp);
            if (currTmp != null && !currTmp.equals("")) {
                if (animator != null) {
                    animator.cancel();
                }
                widget_refresh.setVisibility(GONE);
                widget_unknow.setVisibility(GONE);
                int tmp = Integer.parseInt(currTmp);
                int tmp1 = Math.abs(tmp) / 10;
                int tmp2 = Math.abs(tmp) % 10;
                if (tmp >= 0) {
                    widget_fu_tmp0.setVisibility(GONE);
                    widget_fu_tmp1.setVisibility(GONE);
                    widget_fu_tmp2.setVisibility(GONE);
                    widget_fu_unit.setVisibility(GONE);
                    widget_unit.setVisibility(VISIBLE);
                    if (tmp1 > 0) {
                        widget_tmp1.setVisibility(VISIBLE);
                        widget_tmp2.setVisibility(VISIBLE);
                        widget_tmp1.setImageResource(R.drawable.time_0 + tmp1);
                        widget_tmp2.setImageResource(R.drawable.time_0 + tmp2);
                    } else {
                        widget_tmp1.setVisibility(VISIBLE);
                        widget_tmp2.setVisibility(GONE);
                        widget_tmp1.setImageResource(R.drawable.time_0 + tmp2);
                    }
                } else {
                    widget_tmp1.setVisibility(GONE);
                    widget_tmp2.setVisibility(GONE);
                    widget_unit.setVisibility(GONE);
                    widget_fu_unit.setVisibility(VISIBLE);
                    widget_fu_tmp0.setVisibility(VISIBLE);
                    if (tmp1 > 0) {
                        widget_fu_tmp1.setVisibility(VISIBLE);
                        widget_fu_tmp2.setVisibility(VISIBLE);
                        widget_fu_tmp1.setImageResource(R.drawable.time_0 + tmp1);
                        widget_fu_tmp2.setImageResource(R.drawable.time_0 + tmp2);
                    } else {
                        widget_fu_tmp1.setVisibility(VISIBLE);
                        widget_fu_tmp2.setVisibility(GONE);
                        widget_fu_tmp1.setImageResource(R.drawable.time_0 + tmp2);
                    }
                }
            } else {
                widget_fu_tmp0.setVisibility(GONE);
                widget_fu_tmp1.setVisibility(GONE);
                widget_fu_tmp2.setVisibility(GONE);
                widget_fu_unit.setVisibility(GONE);
                widget_tmp1.setVisibility(GONE);
                widget_tmp2.setVisibility(GONE);
                widget_unit.setVisibility(GONE);

                if (NetworkAvailableUtils.isNetworkAvailable(mContext)) {
                    widget_unknow.setVisibility(GONE);
                    widget_refresh.setVisibility(VISIBLE);
                    animator =
                        ObjectAnimator
                            .ofFloat(widget_refresh, "rotation", 0.0f, 10 * 360.0f);
                    animator.setDuration(10000);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.addListener(new AnimatorListenerAdapter() {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            widget_refresh.setVisibility(GONE);
                            widget_unknow.setVisibility(VISIBLE);
                            animator.cancel();
                        }
                    });
                    animator.start();
                } else {
                    widget_unknow.setVisibility(VISIBLE);
                }
            }
            String code = sharepreference.getString(
                "numberweathercode", null);
            Log.v("WeatherWidgetView", "code = " + code);
            curr_condition.setText(NumberClockHelper.getCurrentWeatherTitle(mContext, code));

            String firstWeek = sharepreference.getString(
                "numberlistweatherweek0", null);
            String firstCode = sharepreference.getString(
                "numberlistweathercode0", null);
            String firstHigh = sharepreference.getString(
                "numberlistweatherhighTmp0", null);
            String firstLow = sharepreference.getString(
                "numberlistweatherlowTmp0", null);
            first_week.setText(NumberClockHelper.LanWeek(mContext, firstWeek));
            first_condition.setImageResource(codeForSmallPath(firstCode));
            first_tmp.setText(firstLow + "/" + firstHigh + unit);
            curr_temperature.setText(firstLow + "/" + firstHigh + unit);

            String secondWeek = sharepreference.getString(
                "numberlistweatherweek1", null);
            String secondCode = sharepreference.getString(
                "numberlistweathercode1", null);
            String secondHigh = sharepreference.getString(
                "numberlistweatherhighTmp1", null);
            String secondLow = sharepreference.getString(
                "numberlistweatherlowTmp1", null);
            second_week.setText(NumberClockHelper.LanWeek(mContext, secondWeek));
            second_condition.setImageResource(codeForSmallPath(secondCode));
            second_tmp.setText(secondLow + "/" + secondHigh + unit);

            String thirdWeek = sharepreference.getString(
                "numberlistweatherweek2", null);
            String thirdCode = sharepreference.getString(
                "numberlistweathercode2", null);
            String thirdHigh = sharepreference.getString(
                "numberlistweatherhighTmp2", null);
            String thirdLow = sharepreference.getString(
                "numberlistweatherlowTmp2", null);
            third_week.setText(NumberClockHelper.LanWeek(mContext, thirdWeek));
            third_condition.setImageResource(codeForSmallPath(thirdCode));
            third_tmp.setText(thirdLow + "/" + thirdHigh + unit);
        } else {
            Log.v("WeatherWidgetView", "numberweatherstate = false");
            if (NetworkAvailableUtils.isNetworkAvailable(mContext)) {
                widget_unknow.setVisibility(GONE);
                widget_refresh.setVisibility(VISIBLE);
                animator =
                    ObjectAnimator
                        .ofFloat(widget_refresh, "rotation", 0.0f, 10 * 360.0f);
                animator.setDuration(10000);
                animator.setInterpolator(new LinearInterpolator());
                animator.addListener(new AnimatorListenerAdapter() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        widget_refresh.setVisibility(GONE);
                        widget_unknow.setVisibility(VISIBLE);
                        animator.cancel();
                    }
                });
                animator.start();
            } else {
                widget_unknow.setVisibility(VISIBLE);
            }
        }
    }

    private int codeForSmallPath(String weathercode) {
        int code = Integer.parseInt(weathercode);
        Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int ResorceId = 0;
        if (hour >= 18) {
            switch (code) {
                case WeatherConditionCodes.TORNADO:
                case WeatherConditionCodes.TROPICAL_STORM:
                case WeatherConditionCodes.HURRICANE:
                case WeatherConditionCodes.BLUSTERY:
                case WeatherConditionCodes.WINDY:
                case WeatherConditionCodes.COLD:
                case WeatherConditionCodes.SEVERE_THUNDERSTORMS:
                case WeatherConditionCodes.THUNDERSTORMS:
                case WeatherConditionCodes.SHOWERS_1:
                case WeatherConditionCodes.SHOWERS_2:
                case WeatherConditionCodes.ISOLATED_THUNDERSTORMS:
                case WeatherConditionCodes.SCATTERED_THUNDERSTORMS_1:
                case WeatherConditionCodes.SCATTERED_THUNDERSTORMS_2:
                case WeatherConditionCodes.SCATTERED_SHOWERS:
                case WeatherConditionCodes.THUNDERSHOWERS:
                case WeatherConditionCodes.ISOLATED_THUNDERSHOWERS:
                case WeatherConditionCodes.MIXED_RAIN_AND_SNOW:
                case WeatherConditionCodes.MIXED_RAIN_AND_SLEET:
                case WeatherConditionCodes.MIXED_SNOW_AND_SLEET:
                case WeatherConditionCodes.HAIL:
                case WeatherConditionCodes.SLEET:
                case WeatherConditionCodes.MIXED_RAIN_AND_HAIL:
                case WeatherConditionCodes.DUST:
                case WeatherConditionCodes.SMOKY:
                case WeatherConditionCodes.FOGGY:
                case WeatherConditionCodes.HAZE:
                case WeatherConditionCodes.CLEAR_NIGHT:
                case WeatherConditionCodes.SUNNY:
                case WeatherConditionCodes.FAIR_NIGHT:
                case WeatherConditionCodes.FAIR_DAY:
                case WeatherConditionCodes.HOT:
                    ResorceId = R.drawable.widget_moon;
                    break;
                case WeatherConditionCodes.FREEZING_DRIZZLE:
                case WeatherConditionCodes.DRIZZLE:
                case WeatherConditionCodes.FREEZING_RAIN:
                    ResorceId = R.drawable.widget_rainshowerslate;
                    break;
                case WeatherConditionCodes.SNOW_FLURRIES:
                case WeatherConditionCodes.SCATTERED_SNOW_SHOWERS:
                case WeatherConditionCodes.SNOW_SHOWERS:
                case WeatherConditionCodes.LIGHT_SNOW_SHOWERS:
                case WeatherConditionCodes.BLOWING_SNOW:
                case WeatherConditionCodes.SNOW:
                case WeatherConditionCodes.HEAVY_SNOW_1:
                case WeatherConditionCodes.HEAVY_SNOW_2:
                    ResorceId = R.drawable.widget_snowshowerslate;
                    break;
                case WeatherConditionCodes.CLOUDY:
                case WeatherConditionCodes.MOSTLY_CLOUDY_NIGHT:
                case WeatherConditionCodes.MOSTLY_CLOUDY_DAY:
                case WeatherConditionCodes.PARTLY_CLOUDY_NIGHT:
                case WeatherConditionCodes.PARTLY_CLOUDY_DAY:
                case WeatherConditionCodes.PARTLY_CLOUDY:
                    ResorceId = R.drawable.widget_latecloudy;
                    break;
                default:
                    ResorceId = R.drawable.widget_unknow;
                    break;
            }
        } else {
            switch (code) {
                case WeatherConditionCodes.TORNADO:
                case WeatherConditionCodes.TROPICAL_STORM:
                case WeatherConditionCodes.HURRICANE:
                case WeatherConditionCodes.BLUSTERY:
                case WeatherConditionCodes.WINDY:
                case WeatherConditionCodes.COLD:
                    ResorceId = R.drawable.widget_jufeng;
                    break;
                case WeatherConditionCodes.SEVERE_THUNDERSTORMS:
                case WeatherConditionCodes.THUNDERSTORMS:
                case WeatherConditionCodes.SHOWERS_1:
                case WeatherConditionCodes.SHOWERS_2:
                case WeatherConditionCodes.ISOLATED_THUNDERSTORMS:
                case WeatherConditionCodes.SCATTERED_THUNDERSTORMS_1:
                case WeatherConditionCodes.SCATTERED_THUNDERSTORMS_2:
                case WeatherConditionCodes.SCATTERED_SHOWERS:
                case WeatherConditionCodes.THUNDERSHOWERS:
                case WeatherConditionCodes.ISOLATED_THUNDERSHOWERS:
                    ResorceId = R.drawable.widget_thunderstorms;
                    break;
                case WeatherConditionCodes.MIXED_RAIN_AND_SNOW:
                case WeatherConditionCodes.MIXED_RAIN_AND_SLEET:
                case WeatherConditionCodes.MIXED_SNOW_AND_SLEET:
                    ResorceId = R.drawable.widget_sleet;
                    break;
                case WeatherConditionCodes.FREEZING_DRIZZLE:
                case WeatherConditionCodes.DRIZZLE:
                case WeatherConditionCodes.FREEZING_RAIN:
                    ResorceId = R.drawable.widget_smallrain;
                    break;
                case WeatherConditionCodes.SNOW_FLURRIES:
                case WeatherConditionCodes.SCATTERED_SNOW_SHOWERS:
                case WeatherConditionCodes.SNOW_SHOWERS:
                    ResorceId = R.drawable.widget_baosnow;
                    break;
                case WeatherConditionCodes.LIGHT_SNOW_SHOWERS:
                case WeatherConditionCodes.BLOWING_SNOW:
                case WeatherConditionCodes.SNOW:
                    ResorceId = R.drawable.widget_smallsnow;
                    break;
                case WeatherConditionCodes.HAIL:
                case WeatherConditionCodes.SLEET:
                case WeatherConditionCodes.MIXED_RAIN_AND_HAIL:
                    ResorceId = R.drawable.widget_bingbao;
                    break;
                case WeatherConditionCodes.DUST:
                case WeatherConditionCodes.SMOKY:
                    ResorceId = R.drawable.widget_sand;
                    break;
                case WeatherConditionCodes.FOGGY:
                case WeatherConditionCodes.HAZE:
                    ResorceId = R.drawable.widget_fog;
                    break;
                case WeatherConditionCodes.CLOUDY:
                case WeatherConditionCodes.MOSTLY_CLOUDY_NIGHT:
                case WeatherConditionCodes.MOSTLY_CLOUDY_DAY:
                case WeatherConditionCodes.PARTLY_CLOUDY_NIGHT:
                case WeatherConditionCodes.PARTLY_CLOUDY_DAY:
                case WeatherConditionCodes.PARTLY_CLOUDY:
                    ResorceId = R.drawable.widget_cloudyday;
                    break;
                case WeatherConditionCodes.CLEAR_NIGHT:
                case WeatherConditionCodes.SUNNY:
                case WeatherConditionCodes.FAIR_NIGHT:
                case WeatherConditionCodes.FAIR_DAY:
                case WeatherConditionCodes.HOT:
                    ResorceId = R.drawable.widget_sunny;
                    break;
                case WeatherConditionCodes.HEAVY_SNOW_1:
                case WeatherConditionCodes.HEAVY_SNOW_2:
                    ResorceId = R.drawable.widget_bigsnow;
                    break;
                default:
                    ResorceId = R.drawable.widget_unknow;
                    break;
            }
        }
        return ResorceId;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        try {
            Intent intent = new Intent(mContext, WeatherCurveActivity.class);
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, StringUtil.getString(mContext, R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
    }

    public void finish() {
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }
}
