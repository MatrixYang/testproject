package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

public class Parameter {
    public final static String currentCityName = "currentnumbercityname";
    public final static String currentunit = "currentnumbercityunit";
    public final static String currentCityId = "currentnumbercityid";
    public final static String currentCountry = "currentnumbercitycountry";

    public final static String SerializableWeather = "serializableweather";
    public final static String SerializableCityResult = "serializablecityresult";


    //FLUSH_CURVE FLUSH_JUST是否可以和FLUSH_ALARM合并？现在的curve和just不更新alarm
    //FLUSH_CF FLUSH_CITY 和 FLUSH_ALARM之间？现在Alarm不更新ui
    public static final int FLUSH_CITY = 1;
    public static final int FLUSH_CURVE = 2;
    public static final int FLUSH_CF = 3;
    public static final int FLUSH_POSITION = 4;
    public static final int FLUSH_ALARM = 5;
    public static final int FLUSH_JUST = 6;


    public static final String UNIT_F = "f";
    public static final String UNIT_C = "c";
    public static final String DEFAULT_UNIT = "c";
    public static final String SHOW_UNIT_F = "°F";
    public static final String SHOW_UNIT_C = "°C";
}
