package com.cooeeui.nanobooster.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cooeeui.nanobooster.MainActivity;
import com.cooeeui.nanobooster.services.BoosterAccessibilityService;
import com.cooeeui.nanobooster.services.FloatWindowService;

public class FloatWindowFinishReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION_FLOAT_WINDOW_FINISH =
        "com.cooeeui.intent.action.FLOAT_WINDOW_FINISH";


    public FloatWindowFinishReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String acion = intent.getAction();
        if (INTENT_ACTION_FLOAT_WINDOW_FINISH.equals(acion)) {
            if (BoosterAccessibilityService.isDeepCleaning) {
                BoosterAccessibilityService.isDeepCleaning = false;
                Intent i = new Intent(context, FloatWindowService.class);
                i.putExtra("removeDeepCleanWindow", true);

                context.startService(i);
                MainActivity.showRate_alert_dialog();
            }
        }
    }
}
