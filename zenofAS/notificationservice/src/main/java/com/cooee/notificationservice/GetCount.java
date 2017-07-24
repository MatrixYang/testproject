package com.cooee.notificationservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;

import com.cooee.smsandcallsaidl.GetSmsAndCalls;

public class GetCount extends GetSmsAndCalls.Stub {

    private String TAG = "GetCount";
    private int mNewSmsCount = 0;
    private Context mContext;
    private final String SMS_ACTION_NAME = "com.cooee.notification.boradcast_sms";
    private final String CALL_ACTION_NAME = "com.cooee.notification.boradcast_call";
    private MissedCallContentObserver newCallContentObserver;
    private MissedSMSContentObserver newMmsContentObserver;

    public GetCount(Context context) {
        mContext = context;
        newCallContentObserver = new MissedCallContentObserver(mContext, new Handler());
        newMmsContentObserver = new MissedSMSContentObserver(new Handler());
        registerSMSObserver();
        registerCallsObserver();

    }

    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public int getSMSCount() throws RemoteException {
        return getAllSmsCount();
    }

    @Override
    public int getCallsCount() throws RemoteException {
        return getAllMissCall();
    }

    /**
     * 未读短信观察者 观察未读短信的数目变化 1. 观察 发送来的未读短信的数目变化 2. 观察 查看过后的未读短信的数目变化
     *
     * @author leexingwang
     */
    public class MissedSMSContentObserver extends ContentObserver {

        public MissedSMSContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            mNewSmsCount = getNewSmsCount() + getNewMmsCount();
            Intent intent = new Intent();
            intent.setAction(SMS_ACTION_NAME);
            intent.putExtra("smsCount", mNewSmsCount);
            mContext.sendBroadcast(intent);
            Log.i("smsandcall", "发出源sms:  " + mNewSmsCount);
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
            Cursor csr = ctx.getContentResolver().query(Calls.CONTENT_URI, new String[]{
                Calls.NUMBER, Calls.TYPE, Calls.NEW
            }, null, null, Calls.DEFAULT_SORT_ORDER);
            if (csr != null) {
                if (csr.moveToFirst()) {
                    int type = csr.getInt(csr.getColumnIndex(Calls.TYPE));
                    switch (type) {
                        case Calls.MISSED_TYPE:
                            Intent intent = new Intent();
                            intent.setAction(CALL_ACTION_NAME);
                            intent.putExtra("callCount", getAllMissCall());
                            ctx.sendBroadcast(intent);
                            Log.i("smsandcall", "发出源calls:  " + getAllMissCall());
                            break;
                        case Calls.INCOMING_TYPE:
                            Log.v("Calls", "incoming type");
                            break;
                        case Calls.OUTGOING_TYPE:
                            Log.v("Calls", "outgoing type");
                            break;
                    }
                }
                // release resource
                csr.close();
            }
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }
    }

    /**
     * 注册未接来电观察者
     */
    private void registerCallsObserver() {
        unregisterCallsObserver();
        mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false,
                                                              newCallContentObserver
        );
    }

    /**
     * 注册未读短信观察者
     */
    @SuppressLint("NewApi")
    private void registerSMSObserver() {
        unregisterSMSObserver();
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true,
                                                              newMmsContentObserver);
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true,
                                                              newMmsContentObserver);
    }

    private synchronized void unregisterCallsObserver() {
        try {
            if (newCallContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(newCallContentObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }

    }

    private synchronized void unregisterSMSObserver() {
        try {
            if (newMmsContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver);
            }
            if (newMmsContentObserver != null) {
                mContext.getContentResolver().unregisterContentObserver(newMmsContentObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }
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
                                                                Calls.TYPE
                                                            }, " type=? and new=?", new String[]{
                Calls.MISSED_TYPE + "", "1"
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
        mNewSmsCount = getNewSmsCount() + getNewMmsCount();
        return mNewSmsCount;
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

}
