package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.cooeeui.brand.zenlauncher.alarmUpdate.handle.UpdateWeatherHandle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class YahooClient {

    public static String YAHOO_GEO_URL = "http://where.yahooapis.com/v1";
    //public static String YAHOO_WEATHER_URL = "http://weather.yahooapis.com/forecastrss";
    public static String YAHOO_WEATHER_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    public static int FORECAST_DAY_MIN = 5;
    private static String
        APPID =
        "dj0yJmk9cHlhcHpjcTZhYVhoJmQ9WVdrOVdGTklXRmhoTlRRbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD0wZA--";

    public static List<CityResult> getCityList(final String cityName,
                                               final Context mContext) {
        return getCityList(cityName, mContext, null);
    }

    public static List<CityResult> getCityList(final String cityName,
                                               final Context mContext, final String lang) {
        final List<CityResult> result = new ArrayList<CityResult>();
        if (NumberClockHelper.isHaveInternet(mContext)) {
            HttpURLConnection yahooHttpConn = null;
            try {
                String query = makeQueryCityURL(cityName, lang);
                yahooHttpConn = (HttpURLConnection) (new URL(query))
                    .openConnection();
                yahooHttpConn.setConnectTimeout(5000);
                yahooHttpConn.setReadTimeout(5000);
                yahooHttpConn.connect();
                int statue = yahooHttpConn.getResponseCode();
                if (statue == HttpURLConnection.HTTP_OK) {
                    XmlPullParser parser = XmlPullParserFactory.newInstance()
                        .newPullParser();
                    parser.setInput(new InputStreamReader(yahooHttpConn
                                                              .getInputStream()));
                    int event = parser.getEventType();
                    CityResult cty = null;
                    String tagName = null;
                    String currentTag = null;
                    while (event != XmlPullParser.END_DOCUMENT) {
                        tagName = parser.getName();
                        if (event == XmlPullParser.START_TAG) {
                            if (tagName.equalsIgnoreCase("html")
                                || tagName.equalsIgnoreCase("h1")) {
                                if (SearcherCityActivity.mHandler != null) {
                                    Handler handler = SearcherCityActivity.mHandler;
                                    int msgWhat = SearcherCityActivity.MSG_NETWORK_FAILURE;
                                    handler.obtainMessage(msgWhat).sendToTarget();
                                }
                                break;
                            }
                            if (tagName.equals("place")) {
                                cty = new CityResult();
                            }
                            currentTag = tagName;
                        } else if (event == XmlPullParser.TEXT) {
                            if ("woeid".equals(currentTag)) {
                                cty.setWoeid(parser.getText());
                            } else if ("name".equals(currentTag)) {
                                cty.setCityName(parser.getText());
                            } else if ("country".equals(currentTag)) {
                                cty.setCountry(parser.getText());
                            }
                        } else if (event == XmlPullParser.END_TAG) {
                            if ("place".equals(tagName)) {
                                result.add(cty);
                            }
                        }
                        event = parser.next();
                    }
                } else {
                    cityListFailed();
                }
            } catch (Exception e) {
                cityListFailed();
                e.printStackTrace();
            } finally {
                try {
                    yahooHttpConn.disconnect();
                } catch (Throwable ignore) {
                }
            }
        } else {
            cityListFailed();
        }
        return result;
    }


    private static void cityListFailed(){
        if (SearcherCityActivity.mHandler != null) {
            Handler handler = SearcherCityActivity.mHandler;
            int msgWhat = SearcherCityActivity.MSG_NETWORK_FAILURE;
            handler.obtainMessage(msgWhat).sendToTarget();
        }
    }
    public static void getWeatherInfo(CityResult result, String unit,
                                      Context context, int what) {
        Weather weather = null;

        if (NumberClockHelper.isHaveInternet(context)) {
            HttpURLConnection yahooHttpConn = null;
            try {
                String query = makeWeatherURL(result.getWoeid(), unit);
                yahooHttpConn = (HttpURLConnection) (new URL(query))
                    .openConnection();
                yahooHttpConn.setConnectTimeout(5000);
                yahooHttpConn.setReadTimeout(5000);
                yahooHttpConn.connect();
                int statue = yahooHttpConn.getResponseCode();
                if (statue == HttpURLConnection.HTTP_OK) {
                    weather = parseResponse(yahooHttpConn.getInputStream());
                    //list中的城市名称
                    weather.setResultCity(result.getCityName());
                    loadSuccess(weather, what, result);
                } else {
                    networkFail( what);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    yahooHttpConn.disconnect();
                } catch (Throwable ignore) {
                }
            }
        }

        if (weather == null) {
            networkFail(what);
        }
    }

    private static void networkFail(int what) {
        int msgWhat = UpdateWeatherHandle.MSG_ALARM_FAILURE;
        Handler handler = UpdateWeatherHandle.getHandle();
        switch (what) {
            case Parameter.FLUSH_CITY:
                msgWhat = SearcherCityActivity.MSG_NETWORK_FAILURE;
                handler = SearcherCityActivity.mHandler;
                break;
            case Parameter.FLUSH_CURVE:
                msgWhat = WeatherCurveActivity.MSG_NETWORK_FAILURE;
                handler = WeatherCurveActivity.mHandler;
                break;
            case Parameter.FLUSH_CF:
                msgWhat = WeatherCurveActivity.MSG_NETWORK_FAILURE;
                handler = WeatherCurveActivity.mHandler;
                break;
            case Parameter.FLUSH_POSITION:
                //定位功能需要更新天气主页面的ui
                msgWhat = WeatherCurveActivity.MSG_NETWORK_FAILURE;
                handler = WeatherCurveActivity.mHandler;
                if (handler != null) {
                    handler.obtainMessage(msgWhat).sendToTarget();
                }
                msgWhat = UpdateWeatherHandle.MSG_POSTITON_FAILURE;
                handler = UpdateWeatherHandle.getHandle();
                break;
            case Parameter.FLUSH_ALARM:
                msgWhat = UpdateWeatherHandle.MSG_ALARM_FAILURE;
                handler = UpdateWeatherHandle.getHandle();
                break;
            case Parameter.FLUSH_JUST:
                msgWhat = UpdateWeatherHandle.MSG_NETWORK_FAILURE;
                handler = UpdateWeatherHandle.getHandle();
                break;
        }

        if (handler != null) {
            handler.obtainMessage(msgWhat).sendToTarget();
        }
    }

    private static void loadSuccess(Weather weather, int what, CityResult result) {
        if (NumberClockHelper.checkWeatherData(weather)) {
            weatherDataSuccess(weather, result, what);
        } else {
            weatherDataFail(what);
        }
    }

    private static void weatherDataSuccess(Weather weather, CityResult result,
                                           int what) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Parameter.SerializableWeather, weather);
        bundle.putSerializable(Parameter.SerializableCityResult, result);

        int msgWhat = UpdateWeatherHandle.MSG_SUCCESS;
        Handler handler = UpdateWeatherHandle.getHandle();
        switch (what) {
            case Parameter.FLUSH_CITY:
                msgWhat = SearcherCityActivity.MSG_SUCCESS;
                handler = SearcherCityActivity.mHandler;
                break;
            case Parameter.FLUSH_CURVE:
                msgWhat = WeatherCurveActivity.MSG_SUCCESS;
                handler = WeatherCurveActivity.mHandler;
                break;
            case Parameter.FLUSH_CF:
                msgWhat = WeatherCurveActivity.MSG_CF_SUCCESS;
                handler = WeatherCurveActivity.mHandler;
                break;
            case Parameter.FLUSH_POSITION:
                //定位功能需要更新天气主页面的ui
                msgWhat = WeatherCurveActivity.MSG_SUCCESS;
                handler = WeatherCurveActivity.mHandler;
                if (handler != null) {
                    handler.obtainMessage(msgWhat, bundle).sendToTarget();
                }

                msgWhat = UpdateWeatherHandle.MSG_POSTITON_SUCCESS;
                handler = UpdateWeatherHandle.getHandle();
                break;
            case Parameter.FLUSH_ALARM:
                msgWhat = UpdateWeatherHandle.MSG_ALARM_SUCCESS;
                handler = UpdateWeatherHandle.getHandle();
                break;
            case Parameter.FLUSH_JUST:
                msgWhat = UpdateWeatherHandle.MSG_SUCCESS;
                handler = UpdateWeatherHandle.getHandle();
                break;
        }

        if (handler != null) {
            handler.obtainMessage(msgWhat, bundle).sendToTarget();
        }
    }

    public static void weatherDataFail(int what) {
        int msgWhat = UpdateWeatherHandle.MSG_FAILURE;
        Handler handler = UpdateWeatherHandle.getHandle();
        switch (what) {
            case Parameter.FLUSH_CITY:
                msgWhat = SearcherCityActivity.MSG_FAILURE;
                handler = SearcherCityActivity.mHandler;
                break;
            case Parameter.FLUSH_CURVE:
                msgWhat = WeatherCurveActivity.MSG_FAILURE;
                handler = WeatherCurveActivity.mHandler;
                break;
            case Parameter.FLUSH_CF:
                msgWhat = WeatherCurveActivity.MSG_FAILURE;
                handler = WeatherCurveActivity.mHandler;
                break;
            case Parameter.FLUSH_POSITION:
                //定位功能需要更新天气主页面的ui
                msgWhat = WeatherCurveActivity.MSG_FAILURE;
                handler = WeatherCurveActivity.mHandler;
                if (handler != null) {
                    handler.obtainMessage(msgWhat).sendToTarget();
                }

                msgWhat = UpdateWeatherHandle.MSG_POSTITON_FAILURE;
                handler = UpdateWeatherHandle.getHandle();
                break;
            case Parameter.FLUSH_ALARM:
                msgWhat = UpdateWeatherHandle.MSG_ALARM_FAILURE;
                handler = UpdateWeatherHandle.getHandle();
                break;
            case Parameter.FLUSH_JUST:
                msgWhat = UpdateWeatherHandle.MSG_FAILURE;
                handler = UpdateWeatherHandle.getHandle();
                break;
        }
        if (handler != null) {
            handler.obtainMessage(msgWhat).sendToTarget();
        }
    }

    private static Weather parseResponse(InputStream inputStream) {
        Weather result = new Weather();
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance()
                .newPullParser();
            parser.setInput(inputStream, "utf-8");
            String tagName = null;
            List<Weather> list = new ArrayList<Weather>();
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("yweather:forecast")) {
                        Weather weather = new Weather();
                        weather.setWeatherweek(parser.getAttributeValue(null,
                                                                        "day"));
                        weather.setWeatherdate(parser.getAttributeValue(null,
                                                                        "date"));
                        weather.setHightmp(parser.getAttributeValue(null,
                                                                    "high"));
                        weather.setLowtmp(parser.getAttributeValue(null, "low"));
                        weather.setWeathercode(parser.getAttributeValue(null,
                                                                        "code"));
                        weather.setWeathercondition(parser.getAttributeValue(
                            null, "text"));
                        list.add(weather);
                    } else if (tagName.equals("yweather:condition")) {
                        result.setWeathercode(parser.getAttributeValue(null,
                                                                       "code"));
                        result.setWeathercondition(parser.getAttributeValue(
                            null, "text"));
                        result.setCurrtmp(parser
                                              .getAttributeValue(null, "temp"));
                        result.setWeatherdate(parser.getAttributeValue(null,
                                                                       "date"));
                    } else if (tagName.equals("yweather:atmosphere")) {
                        result.setShidu(parser.getAttributeValue(null,
                                                                 "humidity"));
                    } else if (tagName.equals("yweather:location")) {
                        result.setWeathercity(parser.getAttributeValue(null,
                                                                       "city"));
                    }
                } else if (event == XmlPullParser.END_TAG) {
                } else if (event == XmlPullParser.TEXT) {
                }
                event = parser.next();
            }
            result.setList(list);
        } catch (XmlPullParserException t) {
            t.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String makeQueryCityURL(String cityName, String lang) {
        try {
            cityName = URLEncoder.encode(cityName, "utf-8").replaceAll("\\+", "%20");
            cityName = cityName.replaceAll("%3A", ":").replaceAll("%2F", "/");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (lang == null) {
            lang = Locale.getDefault().getLanguage();
        }

        return YAHOO_GEO_URL + "/places.q(" + cityName + "%2A);count=" + 10
               + "?appid=" + APPID + "&lang=" + lang;

    }

    private static String makeWeatherURL(String woeid, String unit) {
        String
            sql =
            "select * from weather.forecast where woeid=" + woeid + " and u=\"" + unit + "\"";
        //对路径进行编码 然后替换路径中所有空格
        try {
            sql = URLEncoder.encode(sql, "utf-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //编码之后的路径中的“/”也变成编码的东西了 所有还有将其替换回来
        sql = sql.replaceAll("%3A", ":").replaceAll("%2F", "/");
        return YAHOO_WEATHER_URL + sql;
//        return YAHOO_WEATHER_URL + "?w=" + woeid + "&u=" + unit;
    }
}
