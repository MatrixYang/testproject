package com.cooeeui.wallpaper.util;

import android.content.Context;

import java.util.UUID;

public class Installation {

    public synchronized static String id(Context context) {
        return getMyUUID(context);
    }

    public static String getMyUUID(
        Context context) {
        final String androidId;
        androidId = ""
                    + android.provider.Settings.Secure.getString(context.getContentResolver(),
                                                                 android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
        String uniqueId = deviceUuid.toString();

        return uniqueId;
    }

}
