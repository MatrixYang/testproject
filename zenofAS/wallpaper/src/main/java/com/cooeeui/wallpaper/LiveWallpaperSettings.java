package com.cooeeui.wallpaper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class LiveWallpaperSettings extends PreferenceActivity {

    Preference startTurbo = null;
    private static final String ZEN_LAUNCHER_PACKAGE_NAME = "com.cooeeui.zenlauncher";
    private static final String
        ZEN_LAUNCHER_ACTIVITY_NAME =
        "com.cooeeui.brand.zenlauncher.Launcher";

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.lwp_settings);
        startTurbo = (Preference) findPreference("preference_launch_launhcer");
        startTurbo.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startZenlauncherActivity(LiveWallpaperSettings.this);
                return true;
            }
        });
    }

    private void startZenlauncherActivity(Context context) {
        ComponentName componetName = new ComponentName(ZEN_LAUNCHER_PACKAGE_NAME,
                                                       ZEN_LAUNCHER_ACTIVITY_NAME);
        Intent intent = new Intent();
        intent.setComponent(componetName);
        context.startActivity(intent);
        finish();
    }
}
