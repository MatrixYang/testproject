package com.cooeeui.brand.zenlauncher.alarmUpdate.handle;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;

import com.cooeeui.brand.zenlauncher.alarmUpdate.service.DataService;

/**
 * Created by cuiqian on 2016/3/31.
 */
public class UpdateMobvistaAdHandle extends Handler {
    private Context mContext;
    private static UpdateMobvistaAdHandle mHandler = null;
    public static final int MSG_APPWALL_PRELOAD = 1;
    //5个小时
    private static final long TIME_APPWALL_PRELOAD = 1000 * 60 * 60 * 5;

    /**
     * 确保在主线程中初始化
     * @param context
     * @return
     */
    public static UpdateMobvistaAdHandle getInstance(Context context) {
        if (mHandler == null) {
            mHandler = new UpdateMobvistaAdHandle(context);
        }
        return mHandler;
    }
    public static UpdateMobvistaAdHandle getHandle() {
        return mHandler;
    }
    public UpdateMobvistaAdHandle(Context context) {
        mContext = context;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_APPWALL_PRELOAD:
                setAlarm(TIME_APPWALL_PRELOAD);
                break;
        }
    }

    private void setAlarm(long interval) {
        long now = System.currentTimeMillis();
        Time time = new Time();
        time.set(now + interval);
        long nextUpdate = time.toMillis(true);
        DataService.startAlarmDateService(mContext, nextUpdate, DataService.ALARM_MOBVISTA,
                                          DataService.MOBVISTA_APPWALL_PRELOAD);
    }
}
