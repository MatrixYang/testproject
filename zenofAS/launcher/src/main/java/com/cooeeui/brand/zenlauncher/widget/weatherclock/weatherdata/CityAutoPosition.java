package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooeeui.brand.zenlauncher.Launcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Created by cuiqian on 2016/3/9.
 */
public class CityAutoPosition {

    private static String TAG = "CityAutoPosition";
    private final static String mUrl = "http://nanohome.cn/launcher/get_keywords/get_city.php";
    private static String mCity = null;
    private static String mCountry = null;

    public static synchronized void autoPosition(final Context context) {
        Log.i(TAG, "autoPosition");
        try {
            String result = null;
            HttpPost request = new HttpPost(mUrl);
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
                //获取定位国家
                mCountry = jsonObject.getString("geo_countryName");
                Log.i(TAG, "countryName: " + mCountry);
                //获取定位城市
                mCity = jsonObject.getString("geo_city");
                Log.i(TAG, "city: " + mCity);
            }
        } catch (Exception e) {
            Log.v(TAG, "UnsupportedEncodingException...." + e.toString());
        }
    }

    public static void locateCity(final Context context, final String cityName,
                                  final String country) {

        if (cityName == null || country == null){
            YahooClient.weatherDataFail(Parameter.FLUSH_POSITION);
            return;
        }

        List<CityResult> cityResultList = YahooClient.getCityList(cityName,
                                                                  Launcher.getInstance(),
                                                                  "en");
        if (cityResultList.isEmpty()) {
            YahooClient.weatherDataFail(Parameter.FLUSH_POSITION);
            return;
        }

        boolean success = false;
        for (int i = 0; i < cityResultList.size(); i++) {
            //定位获取的英文城市和国家和citylist中的对比
            if (cityName.equals(cityResultList.get(i).getCityName())
                && country.equals(cityResultList.get(i).getCountry())) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                YahooClient.getWeatherInfo(cityResultList.get(i),
                                           sp.getString(Parameter.currentunit,
                                                        Parameter.DEFAULT_UNIT),
                                           context,
                                           Parameter.FLUSH_POSITION);
                success = true;
                break;
            }

        }

        if (!success){
            YahooClient.weatherDataFail(Parameter.FLUSH_POSITION);
        }
    }

    public static void weatherAutoPosition(final Context context) {
        if (mCity == null || mCountry == null) {
            autoPosition(context);
        }
        locateCity(context, mCity, mCountry);
    }
}
