package com.cooeeui.brand.zenlauncher.mobvista;

import android.content.Context;
import android.os.Handler;

import com.cooeeui.brand.zenlauncher.alarmUpdate.handle.UpdateMobvistaAdHandle;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.MobVistaSDKFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuiqian on 2016/3/31.
 */
public class MobvistaAdHelper {
    public static void mobvistaAppWallPreload(Context context) {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, Object> preloadMap = new HashMap<String, Object>();
        preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_APPWALL);
        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, "187");
        sdk.preload(preloadMap);

        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, "216");
        sdk.preload(preloadMap);

        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, "218");
        sdk.preload(preloadMap);

        Handler handler = UpdateMobvistaAdHandle.getHandle();
        if (handler != null){
            handler.obtainMessage(UpdateMobvistaAdHandle.MSG_APPWALL_PRELOAD).sendToTarget();
        }
    }
}
