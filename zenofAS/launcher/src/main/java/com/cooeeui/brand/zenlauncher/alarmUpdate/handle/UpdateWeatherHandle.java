package com.cooeeui.brand.zenlauncher.alarmUpdate.handle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.cooeeui.brand.zenlauncher.alarmUpdate.service.DataService;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.CityResult;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.NumberClockHelper;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.Parameter;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.Weather;

public class UpdateWeatherHandle extends Handler {

    private Context mContext;
    private SharedPreferences sharedPreferences;

    private Bundle mBundle;
    private Weather mWeather;
    private CityResult mCityResult;
    //2个小时
    private static final long TIME_ALARM_SUCCESS = 1000 * 60 * 60 * 2;
    private static final long TIME_ALARM_FAILED = 1000 * 60 * 30;
    private static final long TIME_POSITON_FAILED = 1000 * 60 * 10;

    public static UpdateWeatherHandle mHandler = null;
    public static final int MSG_SUCCESS = 1;
    public static final int MSG_FAILURE = 2;
    public static final int MSG_NETWORK_FAILURE = 3;
    public static final int MSG_ALARM_SUCCESS = 4;
    public static final int MSG_ALARM_FAILURE = 5;
    public static final int MSG_POSTITON_SUCCESS = 6;
    public static final int MSG_POSTITON_FAILURE = 7;

    public UpdateWeatherHandle(final Context context) {
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    /**
     * 确保只在主线程中调用
     */
    public static UpdateWeatherHandle getInstance(Context context) {
        if (mHandler == null) {
            mHandler = new UpdateWeatherHandle(context);
        }
        return mHandler;
    }

    public static UpdateWeatherHandle getHandle() {
        return mHandler;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SUCCESS:
                mBundle = (Bundle) msg.obj;
                mWeather = (Weather) mBundle
                    .getSerializable(Parameter.SerializableWeather);
                NumberClockHelper.saveWeather(mContext,
                                              sharedPreferences,
                                              mWeather);
                break;
            case MSG_ALARM_SUCCESS:
                mBundle = (Bundle) msg.obj;
                mWeather = (Weather) mBundle
                    .getSerializable(Parameter.SerializableWeather);
                NumberClockHelper.saveWeather(mContext,
                                              sharedPreferences,
                                              mWeather);
                setAlarm(TIME_ALARM_SUCCESS);
                break;
            case MSG_POSTITON_SUCCESS:
                //已手动设置过城市，抛弃自动定位结果
                String
                    currentCityName =
                    sharedPreferences.getString(Parameter.currentCityName, null);
                if (currentCityName != null) {
                    return;
                }
                mBundle = (Bundle) msg.obj;
                mWeather = (Weather) mBundle
                    .getSerializable(Parameter.SerializableWeather);
                mCityResult = (CityResult) mBundle
                    .getSerializable(Parameter.SerializableCityResult);
                NumberClockHelper.saveWeather(mContext,
                                              sharedPreferences,
                                              mWeather);
                NumberClockHelper.setCityResult(sharedPreferences, mCityResult);
                setAlarm(TIME_ALARM_SUCCESS);
                break;
            case MSG_POSTITON_FAILURE:
                setAlarm(TIME_POSITON_FAILED);
                break;
            case MSG_ALARM_FAILURE:
                setAlarm(TIME_ALARM_FAILED);
                break;
            case MSG_FAILURE:
                break;
            case MSG_NETWORK_FAILURE:
                break;
        }
    }

    private void setAlarm(long interval) {
        long now = System.currentTimeMillis();
        Time time = new Time();
        time.set(now + interval);
        long nextUpdate = time.toMillis(true);
        DataService.startAlarmDateService(mContext, nextUpdate, DataService.ALARM_WEATHER,
                                          DataService.WEATHER);
    }
}
