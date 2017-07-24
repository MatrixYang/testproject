package com.cooeeui.brand.zenlauncher.searchbar;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.alarmUpdate.handle.UpdateHotWordsHandle;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Random;

public class SearchHotWords {

    private static String TAG = "SearchHotWords";
    private static ArrayList<String> mHotWordList = new ArrayList<String>();
    private final static String
        mHotWordsUrl =
        "http://nanohome.cn/get_keywords/geo_getcitywords.php";

    public static synchronized void hotWords(Context context) {
        Log.i(TAG, "hotWords");
        try {
            String result = null;
            HttpPost request = new HttpPost(mHotWordsUrl);
            HttpResponse httpResp = new DefaultHttpClient()
                .execute(request);
            if (httpResp.getStatusLine().getStatusCode() == 200) {
                byte[] data = new byte[2048];
                data = EntityUtils.toByteArray((HttpEntity) httpResp
                    .getEntity());
                ByteArrayInputStream bais = new ByteArrayInputStream(
                    data);
                result = new String(data, "UTF-8");

                JSONObject jsonObject = new JSONObject(result);
                String keyword = jsonObject.getString("keyword");

                String[]
                    fields =
                    keyword.substring(1, keyword.length() - 1).split(",");//去除首尾, []
                Log.i(TAG, " fields.length: " + fields.length);

                mHotWordList.clear();
                for (int i = 0; i < fields.length; i++) {
                    mHotWordList
                        .add(fields[i].substring(1, fields[i].length() - 1));//去除首尾, “”
                }
            }

        } catch (Exception e) {
            Log.v(TAG,
                  "UnsupportedEncodingException...." + e.toString());
        }

        Handler handler = UpdateHotWordsHandle.getHandle();
        if (handler != null) {
            handler.obtainMessage(UpdateHotWordsHandle.MSG_UPDATE).sendToTarget();
        }
    }

    public static synchronized void hotWordsThread(final Context context) {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                hotWords(context);
            }
        });

    }

    public static int getHotWordsSize() {
        return mHotWordList.size();
    }

    public static String getHotWords() {
        if (mHotWordList.size() == 0) {
            return null;
        }

        Random random = new Random(System.currentTimeMillis());
        int index = random.nextInt(mHotWordList.size());
        return mHotWordList.get(index);
    }
}