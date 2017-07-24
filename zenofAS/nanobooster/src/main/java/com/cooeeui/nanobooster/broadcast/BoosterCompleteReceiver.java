package com.cooeeui.nanobooster.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BoosterCompleteReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_BOOSTER_COMPLETE =
        "com.cooeeui.intent.action.BOOSTER_COMPLETE";


    public BoosterCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String acion = intent.getAction();
        if (INTENT_ACTION_BOOSTER_COMPLETE.equals(acion)) {
//            FloatWindow.getInstance().stopAnim(); // 停止动画
            Log.i("yezhennan", "INTENT_ACTION_BOOSTER_COMPLETE");
        }
    }
}
