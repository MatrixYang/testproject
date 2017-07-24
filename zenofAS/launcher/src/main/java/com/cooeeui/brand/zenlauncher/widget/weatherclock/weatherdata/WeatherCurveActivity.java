package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;

import java.lang.ref.WeakReference;

public class WeatherCurveActivity extends BaseActivity {

    private ImageView iv_curve;
    private ImageView firstIcon;
    private ImageView secondIcon;
    private ImageView thirdIcon;
    private ImageView forthIcon;
    private ImageView fiveIcon;

    private ImageView ivweatherconditionname;

    private ImageView ivweekname;
    private ImageView iv_cityrefresh;
    private TextView tv_cuttentcityname;
    private TextView tv_cuttentcitytem;
    private TextView content_weatherTitle;
    private ImageView content_weatherIamge;
    private FrameLayout layout_location;
    private RelativeLayout rl_tempertureunit;
    private LinearLayout ll_curve = null;
    private SharedPreferences sharepreference = null;
    private DialogUtil progressDialog = null;

    public static MyHandler mHandler = null;
    public static final int MSG_SUCCESS = 1;
    public static final int MSG_FAILURE = 2;
    public static final int MSG_NETWORK_FAILURE = 3;
    public static final int MSG_CF_SUCCESS = 4;

    private Bundle mBundle;
    private Weather mWeather;

    private Intent cityIntent;
    private LinearLayout ll_currentcity;
    private RelativeLayout rl_curveshow;
    private String change_Unit;

    private Drawable drawCurrentC;
    private Drawable drawCurrentF;
    private ImageView iv_corf;

    private Toast toast;

    @SuppressWarnings("deprecation")
    @SuppressLint({
        "NewApi", "HandlerLeak"
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

        setContentView(R.layout.weathercurvelayout);
        sharepreference = PreferenceManager
            .getDefaultSharedPreferences(this);
        drawCurrentC = this.getResources().getDrawable(R.drawable.current_c);
        drawCurrentF = this.getResources().getDrawable(R.drawable.current_f);

        TextView textView = (TextView) findViewById(R.id.rl_flush_text);
        String text = StringUtil.getString(this, R.string.weathercurve_title);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.rl_tempertureunit_text);
        text = StringUtil.getString(this, R.string.default_foreigncorf);
        textView.setText(text);

        ll_curve = (LinearLayout) findViewById(R.id.ll_curve);
        ll_curve.getBackground().setAlpha(80);// 0~255透明度值 ，0为完全透明，255为不透明
        tv_cuttentcityname = (TextView) findViewById(R.id.tv_cuttentcityname);

        text = StringUtil.getString(this, R.string.default_cityname);
        tv_cuttentcityname.setText(text);
        tv_cuttentcitytem = (TextView) findViewById(R.id.tv_cuttentcitytem);
        tv_cuttentcitytem.setText(text);
        content_weatherIamge = (ImageView) findViewById(R.id.content_weatherIamge);
        content_weatherTitle = (TextView) findViewById(R.id.content_weatherTitle);
        cityIntent = new Intent(this, SearcherCityActivity.class);
        layout_location = (FrameLayout) findViewById(R.id.onclick_location);
        layout_location.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                WeatherCurveActivity.this.startActivity(cityIntent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        setInnerWeatherGrroup(this);
        mHandler = new MyHandler(WeatherCurveActivity.this);

        iv_cityrefresh = (ImageView) findViewById(R.id.iv_cityrefresh);

        cancleCityRefreshAnim();
        if (NumberClockHelper.isHaveInternet(this)) {
            NumberClockHelper.updateWeatherThread(WeatherCurveActivity.this,Parameter.FLUSH_CURVE);
            if (sharepreference.getString(Parameter.currentCityName, null) == null) {
                startCityRefreshAnim();
            }
        }

        iv_curve = (ImageView) findViewById(R.id.iv_curve);
        firstIcon = (ImageView) findViewById(R.id.iv_firsticon);
        secondIcon = (ImageView) findViewById(R.id.iv_secondicon);
        thirdIcon = (ImageView) findViewById(R.id.iv_thirdicon);
        forthIcon = (ImageView) findViewById(R.id.iv_forthicon);
        fiveIcon = (ImageView) findViewById(R.id.iv_fiveicon);
        rl_tempertureunit = (RelativeLayout) findViewById(R.id.rl_tempertureunit);
        iv_corf = (ImageView) findViewById(R.id.iv_corf);
        if (Parameter.UNIT_F.equals(sharepreference
                                        .getString(Parameter.currentunit,
                                                   Parameter.DEFAULT_UNIT))) {
            iv_corf.setImageDrawable(drawCurrentF);
        } else {
            iv_corf.setImageDrawable(drawCurrentC);
        }

        rl_tempertureunit.setVisibility(View.VISIBLE);
        iv_corf.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NumberClockHelper.isHaveInternet(WeatherCurveActivity.this)) {
                    String str2 = StringUtil.getString(WeatherCurveActivity.this,
                                                       R.string.networkerror);
                    if (toast != null) {
                        toast.setText(str2);
                    } else {
                        toast = Toast.makeText(WeatherCurveActivity.this, str2,
                                               Toast.LENGTH_SHORT);
                    }
                    toast.show();
                    return;
                }
                final CityResult cityResult = NumberClockHelper.getCityResult(sharepreference);
                if (cityResult.getCityName() != null && cityResult.getWoeid() != null
                    && cityResult.getCountry() != null) {
                    if (Parameter.UNIT_F.equals(sharepreference.getString(
                        Parameter.currentunit, Parameter.DEFAULT_UNIT))) {
                        change_Unit = Parameter.UNIT_C;
                    } else {
                        change_Unit = Parameter.UNIT_F;
                    }

                    progressDialog = new DialogUtil(WeatherCurveActivity.this);
                    progressDialog.showLoadingDialog(true);
                    ThreadUtil.execute(new Runnable() {

                        @Override
                        public void run() {
                            YahooClient.getWeatherInfo(
                                cityResult,
                                change_Unit,
                                WeatherCurveActivity.this, Parameter.FLUSH_CF);
                        }
                    });

                } else {
                    String str4 = StringUtil.getString(WeatherCurveActivity.this,
                                                       R.string.notsetcity_foreign);
                    if (toast != null) {
                        toast.setText(str4);
                    } else {
                        toast = Toast.makeText(WeatherCurveActivity.this, str4,
                                               Toast.LENGTH_SHORT);
                    }
                    toast.show();
                }

            }
        });

        ivweatherconditionname = (ImageView) findViewById(R.id.iv_weatherconditionname);
        ivweekname = (ImageView) findViewById(R.id.iv_weekname);

        iv_curve.setImageBitmap(NumberClockHelper.drawCurve(WeatherCurveActivity.this,
                                                            26, 28, 32, 28, 30, 22, 16, 18, 16,
                                                            28));
        iv_curve.setAlpha(0f);
        AlphaAnimToShow();

        String defaultForeign = StringUtil.getString(WeatherCurveActivity.this,
                                                     R.string.defaultWeatherconditionName_foreign);
        ivweatherconditionname
            .setImageBitmap(
                NumberClockHelper.drawString(
                    WeatherCurveActivity.this,
                    defaultForeign,
                    defaultForeign,
                    defaultForeign,
                    defaultForeign,
                    defaultForeign));
        ivweekname.setImageBitmap(NumberClockHelper.drawString(
            WeatherCurveActivity.this,
            StringUtil.getString(WeatherCurveActivity.this, R.string.firstweek_foreign),
            StringUtil.getString(WeatherCurveActivity.this, R.string.secondweek_foreign),
            StringUtil.getString(WeatherCurveActivity.this, R.string.thirdweek_foreign),
            StringUtil.getString(WeatherCurveActivity.this, R.string.forthweek_foreign),
            StringUtil.getString(WeatherCurveActivity.this, R.string.fiveweek_foreign)));
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

        private final WeakReference<WeatherCurveActivity> mOuter;

        public MyHandler(WeatherCurveActivity outer) {
            mOuter = new WeakReference<WeatherCurveActivity>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            WeatherCurveActivity outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case MSG_SUCCESS:
                        mBundle = (Bundle) msg.obj;
                        mWeather = (Weather) mBundle.getSerializable(Parameter.SerializableWeather);
                        NumberClockHelper.saveWeather(WeatherCurveActivity.this,
                                                      sharepreference,
                                                      mWeather);
                        changeWeather((Bundle) msg.obj);
                        cancleCityRefreshAnim();
//                        AlphaAnimSetTo();
                        break;
                    case MSG_CF_SUCCESS:
                        changeCF();

                        mBundle = (Bundle) msg.obj;
                        mWeather = (Weather) mBundle.getSerializable(Parameter.SerializableWeather);
                        NumberClockHelper.saveWeather(WeatherCurveActivity.this,
                                                      sharepreference,
                                                      mWeather);
                        changeWeather((Bundle) msg.obj);
                        cancleCityRefreshAnim();
                        cancelLoadingDialog();
                        AlphaAnimSetTo();
                        break;
                    case MSG_FAILURE:
                        cancleCityRefreshAnim();
                        cancelLoadingDialog();
                        break;
                    case MSG_NETWORK_FAILURE:
                        cancleCityRefreshAnim();
                        cancelLoadingDialog();
                        break;
                }
            }
        }
    }

    private void changeWeather(Bundle bundle) {
        Weather weather = (Weather) bundle
            .getSerializable(Parameter.SerializableWeather);
        if (weather != null && weather.getList() != null
            && weather.getList().size() >= YahooClient.FORECAST_DAY_MIN) {
            flushAllViewsForeign(WeatherCurveActivity.this, weather);
        }
    }

    private void cancelLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancelLoadingDialog();
        }
    }

    private void changeCF() {
        if (Parameter.UNIT_F.equals(sharepreference.getString(
            Parameter.currentunit, Parameter.DEFAULT_UNIT))) {
            iv_corf.setImageDrawable(drawCurrentC);
            sharepreference.edit()
                .putString(Parameter.currentunit, Parameter.UNIT_C)
                .commit();
        } else {
            iv_corf.setImageDrawable(drawCurrentF);
            sharepreference.edit()
                .putString(Parameter.currentunit, Parameter.UNIT_F)
                .commit();
        }
    }

    @Override
    protected void onStart() {
        if (Launcher.getInstance() != null && Launcher.getInstance().getDragLayer() != null) {
            Launcher.getInstance().getDragLayer().setVisibility(View.INVISIBLE);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(mNetworkReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancelLoadingDialogNoAnimation();
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Weather weather = NumberClockHelper.getWeatherForeign(sharepreference);
        flushAllViewsForeign(this, weather);

    }

    private void startCityRefreshAnim() {
        iv_cityrefresh.setVisibility(View.VISIBLE);
        ObjectAnimator
            animator =
            ObjectAnimator
                .ofFloat(iv_cityrefresh, "rotation", 0.0f, 360.0f);
        animator.setDuration(1000);
        animator.setRepeatCount(Integer.MAX_VALUE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        if (tv_cuttentcityname != null) {
            tv_cuttentcityname.setVisibility(View.INVISIBLE);
        }
        animator.start();

        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancleCityRefreshAnim();
                }
            }, 1000 * 10);
        }
    }

    private void cancleCityRefreshAnim() {
        if (iv_cityrefresh != null) {
            iv_cityrefresh.clearAnimation();
            iv_cityrefresh.setVisibility(View.INVISIBLE);
            tv_cuttentcityname.setVisibility(View.VISIBLE);
        }
    }

    protected void onDestroy() {
        if (mNetworkReceiver != null) {
            this.unregisterReceiver(mNetworkReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent2 = new Intent(WeatherCurveActivity.this,
                                        Launcher.class);
            WeatherCurveActivity.this.startActivity(intent2);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 点击天气详情空白页返回桌面
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            ll_currentcity = (LinearLayout) findViewById(R.id.ll_currentcity);
            rl_curveshow = (RelativeLayout) findViewById(R.id.rl_curveshow);
            rl_tempertureunit = (RelativeLayout) findViewById(R.id.rl_tempertureunit);
            int touchX = (int) event.getX();
            int touchY = (int) event.getY();
            // 判断触发点是否在可点击区域内
            if (isInZone(ll_currentcity, touchX, touchY) || isInZone(rl_curveshow, touchX, touchY)
                || isInZone(rl_tempertureunit, touchX, touchY)) {
                return false;
            } else {
                // 若不在可点击区域内结束详细页
//                finish();
                // 退出动画
//                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * 判断点击点是否在某个view区域中
     *
     * @param view 待确定的目标view
     * @param x    点击点的x坐标
     * @param y    点击点的y坐标
     * @return 如果在这个view区域内返回 true;如果不在这个view区域内返回false
     */
    private boolean isInZone(View view, int x, int y) {
        Rect mRect = new Rect();
        if (view != null) {
            // 得到view的可见边框矩形
            view.getDrawingRect(mRect);
            int[] location = new int[2];
            // 获取view的绝对坐标
            view.getLocationOnScreen(location);
            // 设置矩形的左上角和右下角两个点的绝对坐标
            mRect.left = location[0];
            mRect.top = location[1];
            mRect.right = mRect.right + location[0];
            mRect.bottom = mRect.bottom + location[1];
            // 判断触发点的x,y坐标是否在本矩形范围内
            return mRect.contains(x, y);
        } else {
            return false;
        }
    }

    public void setInnerWeatherGrroup(Context context) {
        if (sharepreference.getBoolean("numberweatherstate", false)) {
            Weather weather = NumberClockHelper
                .getWeatherForeign(sharepreference);
            if (weather != null && weather.getList() != null
                && weather.getList().size() >= YahooClient.FORECAST_DAY_MIN) {
                updateWeatherShow(weather.getResultCity(),
                                  weather.getWeathercode(), weather.getList().get(0)
                                      .getHightmp(), weather.getList().get(0)
                                      .getLowtmp());
            }
        } else {
            content_weatherIamge.setBackgroundResource(NumberClockHelper.codeForPathBig("31"));
            content_weatherTitle.setText(
                StringUtil.getString(context, R.string.default_weather_content_title));
            tv_cuttentcityname.setText(
                StringUtil.getString(context, R.string.default_cityname));
            String temp = 22 + "~" + 26 + Parameter.SHOW_UNIT_C;
            tv_cuttentcitytem.setText(temp);
        }

    }

    private void updateWeatherShow(String cityName, String weathercondition,
                                   String HighTmp, String lowTmp) {
        content_weatherIamge.setBackgroundResource(NumberClockHelper
                                                       .codeForPathBig(weathercondition));
        content_weatherTitle.setText(NumberClockHelper
                                         .getCurrentWeatherTitle(this, weathercondition));
        this.tv_cuttentcityname.setText(cityName);
        String unit = null;
        unit = sharepreference.getString(Parameter.currentunit, Parameter.DEFAULT_UNIT);
        if (Parameter.UNIT_F.equals(unit)) {
            unit = Parameter.SHOW_UNIT_F;
        } else if (Parameter.UNIT_C.equals(unit)) {
            unit = Parameter.SHOW_UNIT_C;
        }
        String temp = lowTmp + "~" + HighTmp + unit;
        tv_cuttentcitytem.setText(temp);
    }

    public void flushAllViewsForeign(Context context, Weather weather) {
        if (weather != null && weather.getList() != null
            && weather.getList().size() >= YahooClient.FORECAST_DAY_MIN) {
            firstIcon
                .setImageResource(NumberClockHelper
                                      .codeForSmallPath(weather.getList().get(0)
                                                            .getWeathercode()));
            secondIcon
                .setImageResource(NumberClockHelper
                                      .codeForSmallPath(weather.getList().get(1)
                                                            .getWeathercode()));
            thirdIcon
                .setImageResource(NumberClockHelper
                                      .codeForSmallPath(weather.getList().get(2)
                                                            .getWeathercode()));
            forthIcon
                .setImageResource(NumberClockHelper
                                      .codeForSmallPath(weather.getList().get(3)
                                                            .getWeathercode()));
            fiveIcon.setImageResource(NumberClockHelper
                                          .codeForSmallPath(
                                              weather.getList().get(4).getWeathercode()));
            setInnerWeatherGrroup(context);
            ivweatherconditionname.setImageBitmap(NumberClockHelper.drawString(
                context,
                NumberClockHelper.StringChange(weather.getList().get(0)
                                                   .getWeathercondition()),
                NumberClockHelper.StringChange(weather.getList().get(1)
                                                   .getWeathercondition()),
                NumberClockHelper.StringChange(weather.getList().get(2)
                                                   .getWeathercondition()),
                NumberClockHelper.StringChange(weather.getList().get(3)
                                                   .getWeathercondition()),
                NumberClockHelper.StringChange(weather.getList().get(4)
                                                   .getWeathercondition())));
            ivweekname.setImageBitmap(NumberClockHelper.drawString(context,
                                                                   NumberClockHelper.LanWeek(
                                                                       WeatherCurveActivity.this,
                                                                       weather.getList().get(0)
                                                                           .getWeatherweek()),
                                                                   NumberClockHelper.LanWeek(
                                                                       WeatherCurveActivity.this,
                                                                       weather.getList().get(1)
                                                                           .getWeatherweek()),
                                                                   NumberClockHelper.LanWeek(
                                                                       WeatherCurveActivity.this,
                                                                       weather.getList().get(2)
                                                                           .getWeatherweek()),
                                                                   NumberClockHelper.LanWeek(
                                                                       WeatherCurveActivity.this,
                                                                       weather.getList().get(3)
                                                                           .getWeatherweek()),
                                                                   NumberClockHelper.LanWeek(
                                                                       WeatherCurveActivity.this,
                                                                       weather.getList().get(4)
                                                                           .getWeatherweek())));

            iv_curve.setImageBitmap(NumberClockHelper.drawCurve(context,
                                                                Integer.parseInt(
                                                                    weather.getList().get(0)
                                                                        .getHightmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(1)
                                                                        .getHightmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(2)
                                                                        .getHightmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(3)
                                                                        .getHightmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(4)
                                                                        .getHightmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(0)
                                                                        .getLowtmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(1)
                                                                        .getLowtmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(2)
                                                                        .getLowtmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(3)
                                                                        .getLowtmp()),
                                                                Integer.parseInt(
                                                                    weather.getList().get(4)
                                                                        .getLowtmp())));
        }
    }

    public void AlphaAnimSetTo() {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(iv_curve, "alpha", 0f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(iv_curve, "alpha", 1f);
        AnimatorSet animset = new AnimatorSet();
        animset.play(anim1).before(anim2);
        animset.setDuration(10);
        animset.start();
    }

    public void AlphaAnimToShow() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(iv_curve, "alpha", 1f);
        anim.setDuration(10);
        anim.start();
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager
                    mConnectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    if (sharepreference != null
                        && sharepreference.getString(Parameter.currentCityName, null) == null) {
                        startCityRefreshAnim();
                    }
                }
            }
        }
    };
}
