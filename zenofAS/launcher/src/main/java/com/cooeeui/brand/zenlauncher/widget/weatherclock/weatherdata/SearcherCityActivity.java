package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@TargetApi(19)
public class SearcherCityActivity extends BaseActivity {

    private EditText editcitysearch;
    private ImageView ivsearchcity;
    private ListView lvcityshow;

    private CityAdapter cityadapter;
    private String PATH = null;
    private SharedPreferences sharepreference = null;
    public static MyHandler mHandler = null;
    private DialogUtil progressDialog = null;

    public static final int MSG_SUCCESS = 1;
    public static final int MSG_FAILURE = 2;
    public static final int MSG_NETWORK_FAILURE = 3;
    private static final int MSG_CITY_RESULT = 4;

    private Toast toast;

    @SuppressLint({
        "HandlerLeak", "InlinedApi", "ShowToast"
    })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DeviceUtils.hasMeiZuSmartBar()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            DeviceUtils.hideNavigationBar(getWindow().getDecorView());
            if (Build.VERSION.SDK_INT >= 19) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.cityfinderlayout);
        PATH = SearcherCityActivity.this.getFilesDir() + File.separator + "numberclock"
               + File.separator + "list.dat";
        sharepreference = PreferenceManager
            .getDefaultSharedPreferences(SearcherCityActivity.this);
        if (sharepreference.getBoolean("isOrNotFirstWrite1", true)) {
            NumberClockHelper.saveDataForeign(finddefaultOverseasCity(), PATH);
            Editor editor = sharepreference.edit();
            editor.putBoolean("isOrNotFirstWrite1", false);
            editor.commit();
        }

        mHandler = new MyHandler(SearcherCityActivity.this);
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        editcitysearch = (EditText)

            findViewById(R.id.et_searchcity);

        editcitysearch.setHintTextColor(Color.WHITE);
        String searchCityText = StringUtil.getString(SearcherCityActivity.this,
                                                     R.string.default_prompt);
        editcitysearch.setHint(searchCityText);
        ivsearchcity = (ImageView)

            findViewById(R.id.iv_search);

        lvcityshow = (ListView) findViewById(R.id.lv_cityshow);
        ArrayList<CityResult> cityArrayList = NumberClockHelper
            .GetDataForeign(PATH);
        cityadapter = new CityAdapter(SearcherCityActivity.this, cityArrayList);

        lvcityshow.setAdapter(cityadapter);
        lvcityshow.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (NumberClockHelper.isHaveInternet(SearcherCityActivity.this)) {
                    final CityResult result = (CityResult) parent.getItemAtPosition(position);
                    //not found this city
                    if (result.getWoeid() == null) {
                        return;
                    }

                    final String
                        unit =
                        sharepreference.getString(Parameter.currentunit, Parameter.DEFAULT_UNIT);
                    progressDialog = new DialogUtil(SearcherCityActivity.this);
                    progressDialog.showLoadingDialog(true);
                    ThreadUtil.execute(new Runnable() {
                        @Override
                        public void run() {
                            YahooClient.getWeatherInfo(result, unit, SearcherCityActivity.this,
                                                       Parameter.FLUSH_CITY);
                        }
                    });
                } else {
                    String str1 = StringUtil.getString(
                        SearcherCityActivity.this,
                        R.string.networkerror_foreign);
                    if (toast != null) {
                        toast.setText(str1);
                    } else {
                        toast =
                            Toast.makeText(SearcherCityActivity.this, str1, Toast.LENGTH_SHORT);
                    }
                    toast.show();
                }
            }
        });
        ivsearchcity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(editcitysearch.getText().toString().trim())) {
                    String
                        str1 =
                        StringUtil.getString(SearcherCityActivity.this, R.string.input_isempty);
                    if (toast != null) {
                        toast.setText(str1);
                    } else {
                        toast = Toast.makeText(SearcherCityActivity.this, str1, Toast.LENGTH_SHORT);
                    }
                    toast.show();
                    return;
                }
                if (NumberClockHelper.isHaveInternet(SearcherCityActivity.this)) {
                    progressDialog = new DialogUtil(SearcherCityActivity.this);
                    progressDialog.showLoadingDialog(true);
                    ThreadUtil.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<CityResult> cityResultList = YahooClient.getCityList(
                                editcitysearch.getText().toString(), SearcherCityActivity.this);
                            cityadapter.refreshCityList(cityResultList);
                            mHandler.removeMessages(MSG_CITY_RESULT);
                            Message msg = mHandler.obtainMessage(MSG_CITY_RESULT);
                            mHandler.sendMessage(msg);
                        }
                    });
                } else {
                    String
                        str1 =
                        StringUtil
                            .getString(SearcherCityActivity.this, R.string.networkerror_foreign);
                    if (toast != null) {
                        toast.setText(str1);
                    } else {
                        toast = Toast.makeText(SearcherCityActivity.this, str1, Toast.LENGTH_SHORT);
                    }
                    toast.show();
                }
            }
        });
    }

    /**
     * 采用内部Handler类来更新UI，避免内存泄露
     *
     * Handler mHandler = new Handler() { public void handleMessage(Message msg) {
     * mImageView.setImageBitmap(mBitmap); } }
     *
     * 上面是一段简单的Handler的使用。当使用内部类（包括匿名类）来创建Handler的时候，Handler对象会隐式地持有一个外部类对象（
     * 通常是一个Activity）的引用（不然你怎么可能通过Handler来操作Activity中的View？）。 而Handler通常会伴随着一个耗时的后台线程（例如从网络拉取图片）一起出现，
     * 这个后台线程在任务执行完毕（例如图片下载完毕）之后，通过消息机制通知Handler，然后Handler把图片更新到界面。 然而，如果用户在网络请求过程中关闭了Activity，正常情况下，Activity不再被使用，它就有可能在GC检查时被回收掉，
     * 但由于这时线程尚未执行完，而该线程持有Handler的引用（不然它怎么发消息给Handler？）， 这个Handler又持有Activity的引用，
     * 就导致该Activity无法被回收（即内存泄露），直到网络请求结束（例如图片下载完毕）。 另外，如果你执行了Handler的postDelayed()方法，该方法会将你的Handler装入一个Message，并把这条Message推到MessageQueue中，
     * 那么在你设定的delay到达之前，会有一条MessageQueue -> Message -> Handler -> Activity的链，导致你的Activity被持有引用而无法被回收。
     */
    private class MyHandler extends Handler {

        private final WeakReference<SearcherCityActivity> mOuter;

        public MyHandler(SearcherCityActivity outer) {
            mOuter = new WeakReference<SearcherCityActivity>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            SearcherCityActivity outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case MSG_SUCCESS:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.cancelLoadingDialogNoAnimation();
                        }
                        Bundle bundle = (Bundle) msg.obj;
                        Weather
                            weather =
                            (Weather) bundle.getSerializable(Parameter.SerializableWeather);
                        CityResult
                            cityResult =
                            (CityResult) bundle.getSerializable(Parameter.SerializableCityResult);
                        //保存result和weather
                        NumberClockHelper.setCityResult(sharepreference, cityResult);
                        NumberClockHelper.saveWeather(SearcherCityActivity.this,
                                                      sharepreference,
                                                      weather);
                        //更新城市列表
                        flushSearchList(cityResult);

                        editcitysearch.setText(null);

                        Intent intentCurve = new Intent(SearcherCityActivity.this,
                                                        WeatherCurveActivity.class);
                        SearcherCityActivity.this.startActivity(intentCurve);

                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;

                    case MSG_FAILURE:
                        break;
                    case MSG_NETWORK_FAILURE:
                        break;

                    case MSG_CITY_RESULT:
                        if (cityadapter.getCount() == 0) {
                            List<CityResult> crlist = new ArrayList<CityResult>();
                            CityResult cr = new CityResult();
                            cr.setCityName(StringUtil.getString(SearcherCityActivity.this,
                                                                R.string.citynotfound_foreign));
                            crlist.add(cr);
                            cityadapter.refreshCityList(crlist);

                            String str1 = StringUtil.getString(SearcherCityActivity.this,
                                                               R.string.citynotfound_foreign);
                            if (toast != null) {
                                toast.setText(str1);
                            } else {
                                toast = Toast.makeText(SearcherCityActivity.this, str1,
                                                       Toast.LENGTH_SHORT);
                            }
                            toast.show();
                        }

                        cityadapter.notifyDataSetChanged();

                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.cancelLoadingDialogNoAnimation();
                        }
                        break;
                }
            }
        }
    }

    private void flushSearchList(CityResult result) {
        List<CityResult> listforeign = new ArrayList<CityResult>();
        listforeign.add(result);
        List<CityResult> listforeigndate = NumberClockHelper
            .GetDataForeign(PATH);
        if (listforeigndate != null && listforeigndate.size() != 0) {
            for (int i = 0; i < listforeigndate.size(); i++) {
                if (listforeign.size() < 10) {
                    boolean ifExist = false;
                    for (int j = 0; j < listforeign.size(); j++) {
                        if (listforeign
                            .get(j)
                            .getWoeid()
                            .equals(listforeigndate.get(i)
                                        .getWoeid())) {
                            ifExist = true;
                            break;
                        }
                    }
                    if (!ifExist) {
                        listforeign.add(listforeigndate.get(i));
                    }
                } else {
                    break;
                }
            }
        }
        NumberClockHelper.saveDataForeign(listforeign, PATH);

        cityadapter.refreshCityList(listforeign);
        cityadapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancelLoadingDialogNoAnimation();
        }
    }

    private ArrayList<CityResult> finddefaultOverseasCity() {
        ArrayList<CityResult> defaultCityResults = new ArrayList<CityResult>();
        String[] default_city_id = getResources().getStringArray(R.array.default_city_id);
        String[] default_city_name = getResources().getStringArray(R.array.default_city_name);
        String[] default_country_name = getResources().getStringArray(
            R.array.default_country_name);
        for (int i = 0; i < default_country_name.length; i++) {
            CityResult cityResult = new CityResult();
            cityResult.setCityName(default_city_name[i]);
            cityResult.setCountry(default_country_name[i]);
            cityResult.setWoeid(default_city_id[i]);
            defaultCityResults.add(cityResult);
        }
        return defaultCityResults;
    }

    private ArrayList<CityResult> findDefaultInternalCity() {
        ArrayList<CityResult> defaultCityResults = new ArrayList<CityResult>();
        String[] default_Internal_city_name = getResources().getStringArray(
            R.array.default_internal_city);
        for (int i = 0; i < default_Internal_city_name.length; i++) {
            CityResult cityResult = new CityResult();
            cityResult.setCityName(default_Internal_city_name[i]);
            defaultCityResults.add(cityResult);
        }
        return defaultCityResults;
    }

    private class CityAdapter extends ArrayAdapter<CityResult> implements
                                                               Filterable {

        private Context ctx;
        private List<CityResult> cityList = new ArrayList<CityResult>();

        public CityAdapter(Context ctx, List<CityResult> cityList) {
            super(ctx, R.layout.cityresult_layout, cityList);
            this.cityList = cityList;
            this.ctx = ctx;
        }

        @Override
        public CityResult getItem(int position) {
            if (cityList != null) {
                return cityList.get(position);
            }
            return null;
        }

        @Override
        public int getCount() {
            if (cityList != null) {
                return cityList.size();
            }
            return 0;
        }

        @SuppressLint("CutPasteId")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = convertView;
            if (result == null) {
                LayoutInflater inf = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                result = inf.inflate(R.layout.cityresult_layout, parent, false);
            }
            TextView tv = (TextView) result.findViewById(R.id.txtCityName);
            ImageView imageView = (ImageView) result
                .findViewById(R.id.iv_locationCity);
            imageView.setImageResource(R.drawable.city);
            imageView.setVisibility(View.GONE);
            if (cityList.size() > 0) {
                if (cityList.get(position).getCountry() != null) {
                    // if (position == 0
                    // &&
                    // (cityList.get(position).getWoeid().equals(sharepreference
                    // .getString(
                    // CityFinder.LOCATIONCITYID, "00000")))) {
                    // ImageView imageView1 = (ImageView) result
                    // .findViewById(R.id.iv_locationCity);
                    // imageView1.setImageResource(R.drawable.city);
                    // imageView1.setVisibility(View.VISIBLE);
                    // String citynamei;
                    // Log.i("cityname", "getWoeid:   " +
                    // cityList.get(position).getWoeid());
                    // citynamei =
                    // sharepreference.getString(CityFinder.LOCATIONCITYID,
                    // "");
                    // Log.i("cityname", "LOCATIONCITYID:   " + citynamei);
                    // tv.setText(cityList.get(position).getCityName() + ","
                    // + cityList.get(position).getCountry());
                    // } else {
                    // tv.setText(cityList.get(position).getCityName() + ","
                    // + cityList.get(position).getCountry());
                    // }
                    tv.setText(cityList.get(position).getCityName() + ","
                               + cityList.get(position).getCountry());
                } else {
                    tv.setText(cityList.get(position).getCityName());
                }
            }
            return result;
        }

        @Override
        public long getItemId(int position) {
            if (cityList != null) {
                return cityList.get(position).hashCode();
            }
            return 0;
        }

        public void refreshCityList(List<CityResult> cityResultList) {
            cityList = cityResultList;
        }

        @Override
        public Filter getFilter() {
            Filter cityFilter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint == null || constraint.length() < 1) {
                        return results;
                    }
                    if (NumberClockHelper.isHaveInternet(ctx)) {
                        List<CityResult> cityResultList = YahooClient
                            .getCityList(constraint.toString(), ctx);
                        if (cityResultList.size() != 0) {
                            results.values = cityResultList;
                            results.count = cityResultList.size();
                        }
                        return results;
                    } else {
                        String str1 = StringUtil.getString(ctx, R.string.networkerror);
                        if (toast != null) {
                            toast.setText(str1);
                        } else {
                            toast = Toast.makeText(ctx, str1,
                                                   Toast.LENGTH_SHORT);
                        }
                        toast.show();
                        return results;
                    }
                }

                @SuppressWarnings({
                    "unchecked", "rawtypes"
                })
                @Override
                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {
                    cityList = (List) results.values;
                    notifyDataSetChanged();
                }
            };
            return cityFilter;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            Intent intent2 = new Intent(SearcherCityActivity.this,
                                        WeatherCurveActivity.class);
            SearcherCityActivity.this.startActivity(intent2);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
        return true;
    }

}
