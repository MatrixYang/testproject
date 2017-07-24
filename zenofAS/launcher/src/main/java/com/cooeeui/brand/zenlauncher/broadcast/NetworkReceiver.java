package com.cooeeui.brand.zenlauncher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.cooeeui.brand.zenlauncher.alarmUpdate.service.DataService;
import com.cooeeui.brand.zenlauncher.searchbar.SearchHotWords;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata.Parameter;

/**
 * Created by cuiqian on 2016/3/31.
 */
public class NetworkReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager
            mConnectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (netInfo != null && netInfo.isAvailable()) {
            if (sharedPreferences != null
                && sharedPreferences.getString(Parameter.currentCityName, null) == null) {
                DataService.startDataService(context, DataService.WEATHER);
            }

            if (SearchHotWords.getHotWordsSize() == 0) {
                DataService.startDataService(context, DataService.HOTWORDS);
            }
        }
    }
}
