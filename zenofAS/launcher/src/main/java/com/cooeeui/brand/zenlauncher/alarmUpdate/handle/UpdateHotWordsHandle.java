package com.cooeeui.brand.zenlauncher.alarmUpdate.handle;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;

import com.cooeeui.brand.zenlauncher.alarmUpdate.service.DataService;

/**
 * Created by cuiqian on 2016/3/31.
 */
public class UpdateHotWordsHandle extends Handler {

    private static UpdateHotWordsHandle mHandler = null;
    public static final int MSG_UPDATE = 1;
    //2小时
    private static final long TIME_UPDATE = 1000 * 60 * 60 * 2;

    private Context mContext;

    /**
     * 确保在主线程中初始化
     */
    public static UpdateHotWordsHandle getInstance(Context context) {
        if (mHandler == null) {
            mHandler = new UpdateHotWordsHandle(context);
        }
        return mHandler;
    }

    public static UpdateHotWordsHandle getHandle() {
        return mHandler;
    }

    public UpdateHotWordsHandle(Context context) {
        mContext = context;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE:
                setAlarm(TIME_UPDATE);
                break;
        }
    }

    private void setAlarm(long interval) {
        long now = System.currentTimeMillis();
        Time time = new Time();
        time.set(now + interval);
        long nextUpdate = time.toMillis(true);
        DataService.startAlarmDateService(mContext, nextUpdate, DataService.ALARM_HOTWORDS,
                                          DataService.HOTWORDS);
    }
}
