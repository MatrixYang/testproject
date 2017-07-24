package com.cooeeui.statistics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by user on 2016/5/16.
 */
public class Config {

    public static final String CONFIG_FILE_NAME = "config.ini";
    public static final String PREFERENCE_KEY_CONFIG = "config";
    public static final String PREFERENCE_KEY_CONFIG_DOMAIN = "domain";
    public static final String PREFERENCE_KEY_CONFIG_SERIALNO = "serialno";
    public static final String PREFERENCE_KEY_CONFIG_APPID = "app_id";
    public static final String PREFERENCE_KEY_CONFIG_TEMPLATEID = "template_id";
    public static final String PREFERENCE_KEY_CONFIG_CHANNELID = "channel_id";
    private static Context mContext;
    public static JSONObject config;

    public static void initConfig(Context context) {
        mContext = context;
        config = getConfig(CONFIG_FILE_NAME);
        if (config != null) {
            SharedPreferences prefs =
                mContext.getSharedPreferences(PREFERENCE_KEY_CONFIG, Activity.MODE_WORLD_READABLE);
            try {
                JSONObject tmp = config.getJSONObject("config");
                final String serialno = tmp.getString("serialno");
                final String domain = tmp.getString("domain");
                final String app_id = tmp.getString("app_id");
                final String template_id = tmp.getString("template_id");
                final String channel_id = tmp.getString("channel_id");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(PREFERENCE_KEY_CONFIG_DOMAIN, domain);
                edit.putString(PREFERENCE_KEY_CONFIG_SERIALNO, serialno);
                edit.putString(PREFERENCE_KEY_CONFIG_APPID, app_id);
                edit.putString(PREFERENCE_KEY_CONFIG_TEMPLATEID, template_id);
                edit.putString(PREFERENCE_KEY_CONFIG_CHANNELID, channel_id);
                edit.commit();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static JSONObject getConfig(String fileName) {
        AssetManager assetManager = mContext.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(fileName);
            String config = readTextFile(inputStream);
            JSONObject jObject;
            try {
                jObject = new JSONObject(config);
                return jObject;
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        return null;
    }

    public static String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
        }
        return outputStream.toString();
    }

}
