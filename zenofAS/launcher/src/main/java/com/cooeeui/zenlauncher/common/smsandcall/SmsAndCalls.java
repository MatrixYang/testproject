package com.cooeeui.zenlauncher.common.smsandcall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.util.Log;

import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.Launcher;

/**
 * 未读信息和未接电话管理类
 */
public class SmsAndCalls {

    private final String TAG = "SmsAndCalls";
    private Context mContext;
    private Handler mHandler;
    private MissedCallContentObserver mMissedCallContentObserver;
    private MissedSMSContentObserver mMissedSMSContentObserver;

    public SmsAndCalls(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        mMissedCallContentObserver = new MissedCallContentObserver(context, handler);
        mMissedSMSContentObserver = new MissedSMSContentObserver(context, handler);
        registerCallsObserver();
        registerSMSObserver();
    }

    public int getSMSCount() {
        return getAllSmsCount();
    }

    public int getCallsCount() {
        return getAllMissCall();
    }

    /**
     * 得到所有未接电话的数目
     *
     * @return 未接电话的数目
     */
    private int getAllMissCall() {
        int result = 0;
        Cursor cursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.TYPE
                }, " type=? and new=?", new String[]{
                        CallLog.Calls.MISSED_TYPE + "", "1"
                }, "date desc");

        if (cursor != null) {
            result = cursor.getCount();
            cursor.close();
        }
        return result;
    }

    /**
     * 得到所有未读短信和彩信的数目
     *
     * @return 未读短信和彩信的数目
     */
    private int getAllSmsCount() {
        return getNewSmsCount() + getNewMmsCount();
    }

    private int getNewSmsCount() {
        int result = 0;
        Cursor csr = mContext.getContentResolver().query(Uri.parse("content://sms"), null,
                                                         "type = 1 and read = 0", null, null);
        if (csr != null) {
            result = csr.getCount();
            csr.close();
        }
        return result;
    }

    private int getNewMmsCount() {
        int result = 0;
        Cursor csr = mContext.getContentResolver().query(Uri.parse("content://mms/inbox"),
                                                         null, "read = 0", null, null);
        if (csr != null) {
            result = csr.getCount();
            csr.close();
        }
        return result;
    }

    /**
     * 注册未接来电观察者
     */
    private void registerCallsObserver() {
        unregisterCallsObserver();
        mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false,
                                                              mMissedCallContentObserver);
    }

    /**
     * 注册未读短信观察者
     */
    @SuppressLint("NewApi")
    private void registerSMSObserver() {
        unregisterSMSObserver();
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true,
                                                              mMissedSMSContentObserver);
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true,
                                                              mMissedSMSContentObserver);
    }

    private synchronized void unregisterCallsObserver() {
        try {
            if (mMissedCallContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(mMissedCallContentObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }

    }

    private synchronized void unregisterSMSObserver() {
        try {
            if (mMissedSMSContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(mMissedSMSContentObserver);
            }
            if (mMissedSMSContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(mMissedSMSContentObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }
    }

    private void sendRefreshMessage(int what, int arg) {
        mHandler.removeMessages(what);
        Message msg = mHandler.obtainMessage(what);
        msg.arg1 = arg;
        mHandler.sendMessage(msg);
    }

    /**
     * 未读短信观察者 观察未读短信的数目变化 1. 观察 发送来的未读短信的数目变化 2. 观察 查看过后的未读短信的数目变化
     *
     * @author leexingwang
     */
    public class MissedSMSContentObserver extends ContentObserver {
        private Context ctx;

        public MissedSMSContentObserver(Context context, Handler handler) {
            super(handler);
            ctx = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (Launcher.getInstance() != null) {
                ThreadUtil.execute(new Runnable() {
                    @Override
                    public void run() {
                        final int num = getAllSmsCount();
                        sendRefreshMessage(Launcher.MSG_SMS_CHANGE, num);
                    }
                });

            }
        }
    }

    /**
     * 未接来电观察者 观察未接来电的数目变化 1. 观察 发送来的未接来电的数目变化 2. 观察 查看过后的未接来电的数目变化
     *
     * @author leexingwang
     */
    public class MissedCallContentObserver extends ContentObserver {

        private Context ctx;

        public MissedCallContentObserver(Context context, Handler handler) {
            super(handler);
            ctx = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    int num = queryDatabase();
                    if (num >= 0) {
                        sendRefreshMessage(Launcher.MSG_CALL_CHANGE, num);
                    }
                }
            });

        }

        private int queryDatabase() {
            int result = -1;
            Cursor csr = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{
                CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.NEW
            }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            if (csr != null) {
                if (csr.moveToFirst()) {
                    int type = csr.getInt(csr.getColumnIndex(CallLog.Calls.TYPE));
                    switch (type) {
                        case CallLog.Calls.MISSED_TYPE:
                            Log.v("Calls", "missed type");
                            result = getAllMissCall();
                            break;
                        case CallLog.Calls.INCOMING_TYPE:
                            Log.v("Calls", "incoming type");
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            Log.v("Calls", "outgoing type");
                            break;
                    }
                }
                // release resource
                csr.close();
            }
            return result;
        }
    }
}
