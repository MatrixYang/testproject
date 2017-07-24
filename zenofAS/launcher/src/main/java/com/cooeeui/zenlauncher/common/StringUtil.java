package com.cooeeui.zenlauncher.common;

import android.content.Context;
import android.content.res.Resources;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class StringUtil {

    public static HashMap<String, String> mStringMap = new HashMap<String, String>();
    public static boolean isFull;

    public static final String FILE_NAME = "/strings.xml";

    public static final int LAN_US = 0;
    public static final int LAN_RU = 1;
    public static final int LAN_PT = 2;
    public static final int LAN_IT = 3;
    public static final int LAN_FR = 4;
    public static final int LAN_DE = 5;
    public static final int LAN_ES = 6;
    public static final int LAN_IN = 7;
    public static final int LAN_TR = 8;
    public static final int LAN_PL = 9;
    public static final int LAN_CN = 10;
    public static final int LAN_TW = 11;

    public static final int LAN_AR = 12;
    public static final int LAN_EL = 13;
    public static final int LAN_RO = 14;
    public static final int LAN_CS = 15;

    public static final int LAN_COUNT = 16;

    public static String getDir(int lan) {
        String dir = "values";
        switch (lan) {
            case LAN_RU:
                dir = "values-ru";
                break;
            case LAN_PT:
                dir = "values-pt";
                break;
            case LAN_IT:
                dir = "values-it";
                break;
            case LAN_FR:
                dir = "values-fr";
                break;
            case LAN_DE:
                dir = "values-de";
                break;
            case LAN_ES:
                dir = "values-es";
                break;
            case LAN_IN:
                dir = "values-in-rID";
                break;
            case LAN_TR:
                dir = "values-r-rTR";
                break;
            case LAN_PL:
                dir = "values-pl";
                break;
            case LAN_CN:
                dir = "values-zh-rCN";
                break;
            case LAN_TW:
                dir = "values-zh-rTW";
                break;
            case LAN_AR:
                dir = "values-ar";
                break;
            case LAN_EL:
                dir = "values-el";
                break;
            case LAN_RO:
                dir = "values-ro";
                break;
            case LAN_CS:
                dir = "values-cs";
                break;

        }
        return dir;
    }


    public static void readXml(InputStream inStream) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inStream, "UTF-8");
        mStringMap.clear();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if ("string".equals(name)) {
                    mStringMap.put(parser.getAttributeValue(0), parser.nextText());
                }
            }
            eventType = parser.next();
        }
        isFull = true;
    }

    public static void loadXml(Context context, int lan) {
        if (lan == LAN_US) {
            return;
        }

        String path = context.getFilesDir().getAbsolutePath() + getDir(lan) + FILE_NAME;
        File file = new File(path);
        if (file.exists()) {
            try {
                FileInputStream in = new FileInputStream(file);
                readXml(in);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getString(Context context, int id) {
        Resources res = context.getResources();
        if (isFull) {
            String name = res.getResourceEntryName(id);
            if (mStringMap.containsKey(name)) {
                return mStringMap.get(name);
            }
        }
        return res.getString(id);
    }

    public static void clearMap() {
        mStringMap.clear();
        isFull = false;
    }

    public static void setStringMap(HashMap<String,String> map) {
        mStringMap = map;
        isFull = true;
    }
}
