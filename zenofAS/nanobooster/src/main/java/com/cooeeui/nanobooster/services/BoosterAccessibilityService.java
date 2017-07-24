package com.cooeeui.nanobooster.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cooeeui.nanobooster.MainActivity;
import com.cooeeui.nanobooster.broadcast.BoosterCompleteReceiver;
import com.cooeeui.nanobooster.broadcast.FloatWindowFinishReceiver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2016/1/13.
 */
public class BoosterAccessibilityService extends AccessibilityService {

    private static final String TAG = BoosterAccessibilityService.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static boolean isDeepCleaning = false;

    private FloatWindowFinishReceiver mReceiver;

    private HashSet<String> mForceStopTextSet;
    private HashSet<String> mForceStopViewIdSet;
    private HashSet<String> mOkTextSet;
    private HashSet<String> mOkViewIdSet;

    private int mBoosterAppCount;


    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) {
            Log.i(TAG, "onCreate");
        }

        mReceiver = new FloatWindowFinishReceiver();
        IntentFilter filter =
            new IntentFilter(FloatWindowFinishReceiver.INTENT_ACTION_FLOAT_WINDOW_FINISH);
        registerReceiver(mReceiver, filter);

        initHashSet();
        Intent intent = new Intent(getApplicationContext(), FloatWindowService.class);
        startService(intent);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (DEBUG) {
            Log.i(TAG, "onServiceConnected");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) {
            Log.i(TAG, "onStartCommand");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DEBUG) {
            Log.i(TAG, "onConfigurationChanged");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Log.i(TAG, "onDestroy");
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        processKillApplication(event);
    }

    @Override
    public void onInterrupt() {
        if (DEBUG) {
            Log.i(TAG, "onInterrupt");
        }
    }

    private void initHashSet() {
        mForceStopTextSet = new HashSet<>();
        mForceStopTextSet.add("FORCE STOP");
        mForceStopTextSet.add("Force Stop");
        mForceStopTextSet.add("Force stop");
        mForceStopTextSet.add("common_force_stop");
        mForceStopTextSet.add("finish_application");
        mForceStopTextSet.add("End");
        mForceStopTextSet.add("强制停止");
        mForceStopTextSet.add("强行停止");
        mForceStopTextSet.add("结束运行");
        mForceStopTextSet.add("強制停止");
        mForceStopTextSet.add("結束操作");

        mForceStopViewIdSet = new HashSet<>();
        mForceStopViewIdSet.add("com.android.settings:id/force_stop_button");
        mForceStopViewIdSet.add("com.android.settings:id/left_button");
        mForceStopViewIdSet.add("miui:id/v5_icon_menu_bar_primary_item");

        mOkTextSet = new HashSet<>();
        mOkTextSet.add("ok");
        mOkTextSet.add("confirm");
        mOkTextSet.add("确定");
        mOkTextSet.add("确认");
        mOkTextSet.add("好");
        mOkTextSet.add("確定");
        mOkTextSet.add("確認");

        mOkViewIdSet = new HashSet<>();
        mOkViewIdSet.add("android:id/button1");
    }

    private void processKillApplication(AccessibilityEvent event) {
        if (!isDeepCleaning) {
            mBoosterAppCount = 0;
            return;
        }

        if (DEBUG) {
            Log.i(TAG, "processKillApplication： " + event.getPackageName());
        }

        if (event.getSource() != null) {
            if ("com.android.settings".equals(event.getPackageName())) {
                String className = (String) event.getClassName();
                Object titleObject = "";
                if (event.getText().size() > 0) {
                    titleObject = (CharSequence) event.getText().get(0);
                }

                if (("com.android.settings.applications.InstalledAppDetailsTop".equals(className))
                    || ("com.android.settings.SubSettings".equals(className))
                    || "App info".equals((CharSequence) titleObject)
                    || "应用信息".equals((CharSequence) titleObject)
                    || "应用程序信息".equals((CharSequence) titleObject)) {
                    handleForceStopButton(event.getSource());
                } else if (("android.app.AlertDialog".equals(className))
                           || ("com.htc.widget.HtcAlertDialog".equals(className))
                           || ("com.yulong.android.view.dialog.AlertDialog".equals(className))) {
                    handleOkButton(event.getSource());
                }

                Log.i("yezhennan", "mBoosterAppCount = " + mBoosterAppCount);
                Log.i("yezhennan", "MainActivity.runningAppSize = " + MainActivity.runningAppSize);
                if (mBoosterAppCount >= (MainActivity.runningAppSize - 1)) {
                    MainActivity.mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendBroadcast(
                                new Intent(BoosterCompleteReceiver.INTENT_ACTION_BOOSTER_COMPLETE));
                        }
                    }, 1500);
                }
            }
        }
    }

    private boolean handleForceStopButton(AccessibilityNodeInfo accessibilityNodeInfo) {
        boolean handleRst = false;

        if (DEBUG) {
            Log.i(TAG, "handleForceStopButton 111");
        }

        ArrayList localList = new ArrayList();
        // 根据按钮的id来寻找
        if (Build.VERSION.SDK_INT >= 18) {
            Iterator stopIterator = mForceStopViewIdSet.iterator();
            while (stopIterator.hasNext()) {
                List nodeList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(
                    (String) stopIterator.next());
                if (nodeList != null && nodeList.size() > 0) {
                    localList.addAll(nodeList);
                    if (DEBUG) {
                        Log.i(TAG, "handleForceStopButton 222");
                    }
                    break;
                }
            }
        }
        // 根据按钮的文字标题来寻找
        if (localList.size() <= 0) {
            Iterator stopIterator = mForceStopTextSet.iterator();
            while (stopIterator.hasNext()) {
                List nodeList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(
                    (String) stopIterator.next());
                if (nodeList != null && nodeList.size() > 0) {
                    localList.addAll(nodeList);
                    if (DEBUG) {
                        Log.i(TAG, "handleForceStopButton 333");
                    }
                    break;
                }
            }
        }
        if (localList.size() > 0) {
            Iterator iterator = localList.iterator();
            while (iterator.hasNext()) {
                AccessibilityNodeInfo localAccessibilityNodeInfo =
                    (AccessibilityNodeInfo) iterator.next();
                if (localAccessibilityNodeInfo.isClickable() && localAccessibilityNodeInfo
                    .isEnabled()) {
                    localAccessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    handleRst = true;
                    if (DEBUG) {
                        Log.i(TAG, "handleForceStopButton 444");
                    }
                }
                localAccessibilityNodeInfo.recycle();
            }
        }
        performGlobalAction(GLOBAL_ACTION_BACK);
        if (DEBUG) {
            Log.i(TAG, "handleForceStopButton GLOBAL_ACTION_BACK");
        }
        return handleRst;
    }

    private boolean handleOkButton(AccessibilityNodeInfo accessibilityNodeInfo) {
        boolean handleRst = false;

        if (DEBUG) {
            Log.i(TAG, "handleOkButton 111");
        }

        ArrayList localList = new ArrayList();
        // 根据按钮的id来寻找
        if (Build.VERSION.SDK_INT >= 18) {
            Iterator okIterator = mOkViewIdSet.iterator();
            while (okIterator.hasNext()) {
                List nodeList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(
                    (String) okIterator.next());
                if (nodeList != null && nodeList.size() > 0) {
                    localList.addAll(nodeList);
                    if (DEBUG) {
                        Log.i(TAG, "handleOkButton 222");
                    }
                    break;
                }
            }
        }
        // 根据按钮的文字标题来寻找
        if (localList.size() <= 0) {
            Iterator okIterator = mOkTextSet.iterator();
            while (okIterator.hasNext()) {
                List list = accessibilityNodeInfo.findAccessibilityNodeInfosByText(
                    (String) okIterator.next());
                if (list != null && list.size() > 0) {
                    localList.addAll(list);
                    if (DEBUG) {
                        Log.i(TAG, "handleOkButton 333");
                    }
                    break;
                }
            }
        }

        if (localList.size() > 0) {
            Iterator iterator = localList.iterator();
            while (iterator.hasNext()) {
                AccessibilityNodeInfo localAccessibilityNodeInfo =
                    (AccessibilityNodeInfo) iterator.next();

                if (localAccessibilityNodeInfo.isClickable() && localAccessibilityNodeInfo
                    .isEnabled()) {
                    localAccessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    handleRst = true;
                    if (DEBUG) {
                        Log.i(TAG, "handleOkButton 444");
                    }
                    mBoosterAppCount++;
                }
                localAccessibilityNodeInfo.recycle();
            }
        }
        return handleRst;
    }
}
