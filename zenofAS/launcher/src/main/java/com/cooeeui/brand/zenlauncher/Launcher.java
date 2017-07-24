package com.cooeeui.brand.zenlauncher;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Advanceable;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.alarmUpdate.handle.UpdateHotWordsHandle;
import com.cooeeui.brand.zenlauncher.alarmUpdate.handle.UpdateMobvistaAdHandle;
import com.cooeeui.brand.zenlauncher.alarmUpdate.handle.UpdateWeatherHandle;
import com.cooeeui.brand.zenlauncher.alarmUpdate.service.DataService;
import com.cooeeui.brand.zenlauncher.appIntentUtils.AppIntentUtil;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ItemInfo;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.changeicon.ChangeAppIcon;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBEntity;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBHelp;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBSearcherApp;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBUtils;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.debug.Logger;
import com.cooeeui.brand.zenlauncher.favorite.FavoriteScene;
import com.cooeeui.brand.zenlauncher.favorite.FavoritesData;
import com.cooeeui.brand.zenlauncher.favorite.MonitorService;
import com.cooeeui.brand.zenlauncher.favorite.SpeedyContainer;
import com.cooeeui.brand.zenlauncher.favorite.SpeedySetting;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.brand.zenlauncher.scenes.AddBubble;
import com.cooeeui.brand.zenlauncher.scenes.Allapps;
import com.cooeeui.brand.zenlauncher.scenes.BubbleView;
import com.cooeeui.brand.zenlauncher.scenes.GuidePage;
import com.cooeeui.brand.zenlauncher.scenes.HomeScreen;
import com.cooeeui.brand.zenlauncher.scenes.MyDeviceManager;
import com.cooeeui.brand.zenlauncher.scenes.SpeedDial;
import com.cooeeui.brand.zenlauncher.scenes.SwitchPagedView;
import com.cooeeui.brand.zenlauncher.scenes.Workspace;
import com.cooeeui.brand.zenlauncher.scenes.ZenSetting;
import com.cooeeui.brand.zenlauncher.scenes.ZenSettingLanguage;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragLayer;
import com.cooeeui.brand.zenlauncher.searchbar.SearchBarGroup;
import com.cooeeui.brand.zenlauncher.settings.AboutActivity;
import com.cooeeui.brand.zenlauncher.settings.DefaultLauncherGuide;
import com.cooeeui.brand.zenlauncher.settings.RateDialog;
import com.cooeeui.brand.zenlauncher.settings.VersionUpdateDetector;
import com.cooeeui.brand.zenlauncher.tips.TextCircleViewInfo;
import com.cooeeui.brand.zenlauncher.tips.TipsPopup;
import com.cooeeui.brand.zenlauncher.tips.TipsSetting;
import com.cooeeui.brand.zenlauncher.tips.TipsSettingDataUtil;
import com.cooeeui.brand.zenlauncher.tools.ToolsAdpter;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.brand.zenlauncher.wallpaper.OnlineWallpaperActivity;
import com.cooeeui.brand.zenlauncher.wallpaper.WallpaperUtil;
import com.cooeeui.brand.zenlauncher.wallpaper.util.PreferencesUtils;
import com.cooeeui.brand.zenlauncher.welcome.WelcomePageHelper;
import com.cooeeui.brand.zenlauncher.widget.weatherclock.WeatherClockGroup;
import com.cooeeui.brand.zenlauncher.widgets.LauncherAppWidgetHost;
import com.cooeeui.brand.zenlauncher.widgets.LauncherAppWidgetInfo;
import com.cooeeui.brand.zenlauncher.widgets.NanoWidgetUtils;
import com.cooeeui.brand.zenlauncher.widgets.SelectWidget;
import com.cooeeui.brand.zenlauncher.widgets.WidgetAdapter;
import com.cooeeui.brand.zenlauncher.widgets.WidgetDrag;
import com.cooeeui.brand.zenlauncher.widgets.WidgetOption;
import com.cooeeui.brand.zenlauncher.widgets.WidgetsListView;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.smsandcall.SmsAndCalls;
import com.cooeeui.zenlauncher.common.ui.AlertDialogUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;
import com.cooeeui.zenlauncher.common.ui.popujar.PopuItem;
import com.cooeeui.zenlauncher.common.ui.popujar.PopuJar;
import com.cooeeui.zenlauncher.common.ui.popujar.PopuJar.OnDismissListener;
import com.cooeeui.zenlauncher.common.ui.popujar.PopuJar.OnPopuItemClickListener;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.umeng.analytics.MobclickAgent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class Launcher extends BaseActivity implements View.OnClickListener,
                                                      OnLongClickListener, LauncherModel.Callbacks {

    public static final String TAG = "Launcher";
    public static final int DEFAULT_APP_INSTALL_DAY_BEFORE = 5;
    public static final int THE_BASE_YEAR = 1900;
    private static Launcher sLauncher;

    private final Handler mHandler = new MyHandler(this);

    private LauncherModel mModel;
    private IconCache mIconCache;
    private DragController mDragController;
    private DragLayer mDragLayer;
    public Workspace workspace;
    public SwitchPagedView switchPage;
    private HomeScreen mHomeScreen;
    private LinearLayout mClockAndSpeedDial;
    private FrameLayout mWeatherArea;
    /**
     * when home page WeatherArea is hide, this view will receive long press
     */
    private View mWeatherBlank;
    /**
     * long press pop for home page WeatherArea(large time case)
     */
    private PopuJar mPopupArea = null;
    /**
     * long press pop for home page WeatherArea(hide case)
     */
    private PopuJar mPopupWeather = null;
    /**
     * long press pop for home page WeatherArea(widget time case)
     */
    private PopuJar mPopupWidget = null;
    private WeatherClockGroup mWeather;
    private SpeedDial mSpeedDial;
    private FrameLayout mSearchBottom = null;
    private SearchBarGroup mSearchBarGroup = null;
    private SpeedyContainer mSpeedyContainer;
    public Allapps Allapp;
    public boolean needHideAllapp;
    private ArrayList<AppInfo> mApps;
    private ArrayList<AppInfo> mCopyApps = new ArrayList<AppInfo>();

    private DefaultLauncherGuide mDefaultLauncherGuide;
    private DialogUtil mLoadingDialog;

    private RateDialog mRateDialog;

    public FavoriteScene favoriteScene;
    private FrameLayout mWidgets;
    private WidgetsListView mWidgetsView;
    private WidgetAdapter mWidgetAdapter;
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mAttached = false;
    private boolean mFisrtStart = true;
    public boolean mGuiding = false;
    public boolean mFinishBindApp = false;
    public boolean mFinishBindHome = false;
    public boolean mLauncherStart = false;

    private static final int REQUEST_CREATE_APPWIDGET = 1;
    private static final int REQUEST_BIND_APPWIDGET = 2;
    private static final int REQUEST_SELECT_WIDGET = 3;
    public static final int REQUEST_PICK_APPWIDGET = 4;
    public static final int REQUEST_START_LANGUAGE = 5;

    public static final int REQUEST_USAGE_SETTING_GUIDE_PAGE = 6;
    public static final int REQUEST_USAGE_SETTING_ALERT = 7;
    public static final int REQUEST_USAGE_SETTING_NANO_SETTING = 8;

    public static final int APPWIDGET_HOST_ID = 1024;
    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;
    private AppWidgetProviderInfo mPendingAddWidgetInfo;
    private static final int MSG_ADVANCE = 1;
    public static final int MSG_SMS_CHANGE = 2;
    public static final int MSG_CALL_CHANGE = 3;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceSentTime;
    private long mAutoAdvanceTimeLeft = -1;
    private boolean mAutoAdvanceRunning = false;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance =
        new HashMap<View, AppWidgetProviderInfo>();

    private boolean mOnResumeNeedsLoad;
    private boolean mPaused = true;
    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();

    public static final int STATE_WIDGET = 0;
    public static final int STATE_HOMESCREEN = 1;
    public static final int STATE_FAVORITE = 2;
    public static final int STATE_ALLAPP = 3;
    private static PopuJar mPopupFavorite = null;
    private static PopuJar mPopupRecently = null;
    // mPopupFavorite start
    public static final int POPUP_MOVE_OUT = 0;
    public static final int POPUP_ADD_SPEED = 1;
    public static final int POPUP_APP_INFO = 2;
    public static final int POPUP_UNINSTALL_APP = 3;
    public static final int POPUP_CHANGE_APP_ICON = 4;
    // mPopupFavorite end

    // mPopupRecently start
    public static final int POPUP_RECENT_ADD_SPEED = 1;
    public static final int POPUP_RECENT_APP_INFO = 2;
    public static final int POPUP_RECENT_UNINSTALL_APP = 3;

    public static final int POPUP_SET_HIDE = 0;
    public static final int POPUP_REPLACE_WIDGET = 1;
    public static final int POPUP_REPLACE_WEATHER = 2;

    public static final int POPUP_SET_VISIBLE = 0;

    private AppWidgetHostView mAreaWidgetView = null;
    private String mAreaWidgetName;

    // 在home主页添加widget的标志位
    private boolean mAddWidgetInArea = false;

    public static final int STATE_WEATHER_VISIBLE = 0;
    public static final int STATE_WEATHER_INVISIBLE = 1;
    public static final int STATE_WIDGET_VISIBLE = 2;
    public static final int STATE_WIDGET_INVISIBLE = 3;

    private int mAreaState = STATE_WEATHER_VISIBLE;

    public static boolean isLanguageChanged;

    public static String NOTICE_SERVICE_ACTION = "com.cooeeui.notificationservice.NoticeService";
    public static String NOTICE_PACKAGE_NAME = "com.cooeeui.notificationservice";
    public static String ICONUI_PACKAGE_NAME = "com.cooeeui.iconui";
    private SmsAndCalls mSmsAndCalls;
    private AddBubble mAddBubble = null;

    private int mDownY = -1;
    private int mDownX = -1;
    private boolean mMoved = false;
    private boolean mLongClick = false;
    private static float mDensity;

    private VersionUpdateDetector mVersionDetector;
    private Intent mOriIntent;

    public SmsAndCalls getSmsAndCalls() {
        return mSmsAndCalls;
    }

    private long mTimes;
    private long firstClickTime = 0;
    private long secondClickTime = 0;

    private ChangeAppIconDBSearcherApp appIconDBSearcherApp;
    private GridView recommendGridView;
    private ToolsAdpter toolsAdpter;

    private RequestQueue requestQueue;


    private WelcomePageHelper welcomePageHelper;

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public GridView getRecommendGridView() {
        return recommendGridView;
    }

    public PopuJar getmPopupRecently() {
        return mPopupRecently;
    }

    public PopuJar getmPopupFavorite() {
        return mPopupFavorite;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager wm = this.getWindowManager();

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        Log.d( "ymc" , "width "+width);
        Log.d( "ymc" , "height "+height);

        Toast.makeText(this, "width :" + width+"/height :"+height, Toast.LENGTH_LONG).show();

        sLauncher = this;

        mDensity = getResources().getDisplayMetrics().density;

        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        LauncherAppState.setAppIntentUtil(new AppIntentUtil(this));// 将appIntent的帮助类传递到AppState

        mModel = app.setLauncher(this);
        mIconCache = app.getIconCache();
        mDragController = new DragController(this);
        mPaused = false;

        UpdateWeatherHandle.getInstance(this);
        UpdateHotWordsHandle.getInstance(this);
        UpdateMobvistaAdHandle.getInstance(this);

        DataService.startDataService(this, DataService.WEATHER);
        DataService.startDataService(this, DataService.HOTWORDS);
        DataService.startDataService(this, DataService.MOBVISTA_APPWALL_PRELOAD);

        if (DeviceUtils.hasMeiZuSmartBar()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            DeviceUtils.hideNavigationBar(getWindow().getDecorView());
            if (Build.VERSION.SDK_INT >= 19) {
                if (Build.VERSION.SDK_INT >= 21) {
                    Window window = getWindow();
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                      | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.TRANSPARENT);
                    window.setNavigationBarColor(Color.TRANSPARENT);
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            if (Build.VERSION.SDK_INT >= 21) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                  | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }

        checkForLocaleChange();

        setContentView(R.layout.launcher);

        loadMobvista();

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mDragLayer.setup(this, mDragController);

        // workspace
        workspace = (Workspace) findViewById(R.id.workspace);
        workspace.setup(this);

        switchPage = (SwitchPagedView) findViewById(R.id.switch_page);
        switchPage.setup(this);

        mSpeedDial = (SpeedDial) mDragLayer.findViewById(R.id.speed_dial);
        mSpeedDial.setup(this, mDragController);
        mSpeedDial.setOnClickListener(this);

        mWidgetAdapter = new WidgetAdapter(this);
        mWidgets = (FrameLayout) mDragLayer.findViewById(R.id.widget);

        int statusbarHeight = 0;
        if (Build.VERSION.SDK_INT >= 19) {
            statusbarHeight = DeviceUtils.getStatusBarHeight(this);
            WidgetsListView list = (WidgetsListView) mWidgets.findViewById(R.id.widget_view);
            list.setPadding(list.getPaddingLeft(), statusbarHeight,
                            list.getPaddingRight(), list.getPaddingBottom());
        }

        mWidgetsView = (WidgetsListView) mDragLayer.findViewById(R.id.widget_view);
        mWidgetsView.setup(this);
        mWidgetsView.setAdapter(mWidgetAdapter);

        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetHost.startListening();

        mHomeScreen = (HomeScreen) findViewById(R.id.home_screen);
        mHomeScreen.setOnClickListener(this);
        mHomeScreen.setOnLongClickListener(this);

        int navigationHeight = 0;
        if (Build.VERSION.SDK_INT >= 19) {
            if (!DeviceUtils.isSpecialDevicesForNavigationbar()
                && LauncherAppState.HasNavigationBar(this)) {
                navigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
            }
            mHomeScreen.setPadding(mHomeScreen.getPaddingLeft(), statusbarHeight,
                                   mHomeScreen.getPaddingRight(), navigationHeight);
        }

        mClockAndSpeedDial = (LinearLayout) findViewById(R.id.clockGroup_SpeedDial);

        favoriteScene = (FavoriteScene) findViewById(R.id.rl_favorite_scene);
        favoriteScene.setOnLongClickListener(this);

        mWeatherArea = (FrameLayout) findViewById(R.id.weather_area);
        mWeatherBlank = findViewById(R.id.weather_blank);
        mWeatherBlank.setTag("WeatherBlank");
        mWeatherBlank.setOnLongClickListener(this);
        mAreaWidgetView = null;
        mAddWidgetInArea = false;

        mWeather = (WeatherClockGroup) findViewById(R.id.weatherclock);
        mWeather.setup(this);

        mAreaState = SettingPreference.getAreaState();
        if (mAreaState != STATE_WEATHER_VISIBLE) {
            mWeather.setVisibility(View.INVISIBLE);
        }

        mSearchBottom = (FrameLayout) findViewById(R.id.search_bottom);
        mSearchBarGroup = (SearchBarGroup) this.findViewById(R.id.search_bar);
        mSearchBarGroup.setup(this);

        mSpeedyContainer = (SpeedyContainer) findViewById(R.id.speedy_container);
        mSpeedyContainer.configureSpeedy(this);

        Allapp = (Allapps) mDragLayer.findViewById(R.id.all_app);
        Allapp.setup(this);

        mModel.startLoader(true);
        welcomePageHelper = new WelcomePageHelper(this);
        firstStartAction();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LauncherConstants.ACTION_FAVOTITE_UPDATE);
        registerReceiver(mFavoriteReceiver, filter);

        startService(new Intent(this, MonitorService.class));

        requestQueue = Volley.newRequestQueue(this);
//        AdServiceUtil.cancleAlarmManager(getInstance());
//        initAdGetJson();
//        AdServiceUtil.invokeTimerPOIService(getInstance());

        TextCircleViewInfo tipCircleInfo = TipsSettingDataUtil.geteTipCircleInfoByTime(this, 0);
        TipsPopup.unlockCount = tipCircleInfo.getUnlock_times();
        TipsPopup.userTime = (long) tipCircleInfo.getPhone_time();
        mSpeedyContainer.registerReceiver();

        //未读信息和未接电话
        initSmsAndCalls();

        int lan = SettingPreference.getZenLanguage();
        StringUtil.loadXml(this, lan);
        isLanguageChanged = true;

        mVersionDetector = new VersionUpdateDetector(this);
        mVersionDetector.setmAutoDetector(true);

        mDefaultLauncherGuide = new DefaultLauncherGuide(getApplicationContext());
    }


    private void loadMobvista() {
        //MobvistaSDK *begin*//
        initRecommendGridView();
        //MobvistaSDK *end*//
    }

    private void initRecommendGridView() {
        recommendGridView = (GridView) findViewById(R.id.gv_recommend);
        recommendGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        toolsAdpter = new ToolsAdpter(Launcher.this);
        recommendGridView.setAdapter(toolsAdpter);
        recommendGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                UIEffectTools.onClickEffect(view);
                if (position == 0) {
                    startZenSetting();
                    // 进入Nano桌面设置的总次数
                    MobclickAgent.onEvent(Launcher.this, "Clickintonanosetting");
                } else if (position == 1) {
                    startTipsSetting();
                } else if (position == 2) {
                    //

                } else if (position == 3) {
                    startWallpaper();
                    //nano tools 中点击设置壁纸次数
                    MobclickAgent.onEvent(Launcher.this, "Wallpaperinnanotools");
                }
            }
        });
    }

    public void updateString() {
        TextView textView = (TextView) favoriteScene.findViewById(R.id.text1);
        String text = StringUtil.getString(this, R.string.favorite);
        textView.setText(text);

        textView = (TextView) favoriteScene.findViewById(R.id.tv_recently);
        text = StringUtil.getString(this, R.string.recently_install_text);
        textView.setText(text);

        textView = (TextView) favoriteScene.findViewById(R.id.tv_recommend);
        text = StringUtil.getString(this, R.string.recommend_app_text);
        textView.setText(text);

        textView = (TextView) Allapp.findViewById(R.id.all_app_title);
        text = StringUtil.getString(this, R.string.all_apps);
        textView.setText(text);
        Allapp.initAllappsPop();

        mWidgetAdapter.updateString();
    }

    private void initRecentPopup() {
        PopuItem addSpeed = new PopuItem(POPUP_RECENT_ADD_SPEED,
                                         StringUtil.getString(this, R.string.pop_add_speeddial),
                                         getResources().getDrawable(R.drawable.add_homescreen));
        PopuItem appInfo = new PopuItem(POPUP_RECENT_APP_INFO,
                                        StringUtil.getString(this, R.string.pop_app_info),
                                        getResources().getDrawable(R.drawable.app_info));
        PopuItem unInstallApp = new PopuItem(POPUP_RECENT_UNINSTALL_APP,
                                             StringUtil.getString(this, R.string.unload),
                                             getResources().getDrawable(R.drawable.app_uninstall));
        PopuItem changeAppIcon = new PopuItem(POPUP_CHANGE_APP_ICON,
                                              StringUtil.getString(this,
                                                                   R.string.pop_change_app_icon),
                                              getResources().getDrawable(
                                                  R.drawable.change_app_icon_popupwindow));
        mPopupRecently = new PopuJar(this, PopuJar.VERTICAL);
        mPopupRecently.addPopuItem(addSpeed, PopuJar.APP_TYPE_NORMAL);
        mPopupRecently.addPopuItem(changeAppIcon, PopuJar.APP_TYPE_NORMAL);
        mPopupRecently.addPopuItem(appInfo, PopuJar.APP_TYPE_NORMAL);
        mPopupRecently.addPopuItem(unInstallApp, PopuJar.APP_TYPE_SYS);
        mPopupRecently.setAnimStyle(PopuJar.ANIM_AUTO);
        mPopupRecently.setOnPopuItemClickListener(new OnPopuItemClickListener() {

            @Override
            public void onItemClick(PopuJar source, int pos, int actionId) {

                AppInfo info = mPopupRecently.getSelected();
                switch (actionId) {
                    case POPUP_RECENT_ADD_SPEED:
                        if (!getSpeedDial().isFull()) {
                            getSpeedDial().addBubbleView(info.makeShortcut());
                            getSpeedDial().update();
                            AppInfo favorite =
                                FavoritesData.getAppInfo(info.componentName.getPackageName());
                            if (favorite != null) {
                                FavoritesData.remove(favorite);
                                updateFavorite();
                            }

                            Toast.makeText(Launcher.this,
                                           StringUtil.getString(
                                               Launcher.this, R.string.pop_add_success),
                                           Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Launcher.this,
                                           StringUtil.getString(
                                               Launcher.this, R.string.pop_add_fail),
                                           Toast.LENGTH_SHORT).show();
                        }
                        alphaAnimOnLongClick(false, mSelectedView, recentlyGrid);
                        break;
                    case POPUP_RECENT_APP_INFO:
                        Uri
                            packageURI =
                            Uri.parse("package:" + info.componentName.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                   packageURI);
                        startActivitySafely(intent);
                        alphaAnimOnLongClick(false, mSelectedView, recentlyGrid);
                        // MobclickAgent.onEvent(Launcher.this,
                        // "FavoriteClickAppInfo");
                        break;
                    case POPUP_RECENT_UNINSTALL_APP:
                        startApplicationUninstallActivity(info.componentName, info.flags);
                        alphaAnimOnLongClick(false, mSelectedView, recentlyGrid);
                        break;
                    case POPUP_CHANGE_APP_ICON:
                        Bundle bundle = new Bundle();
                        bundle.putInt(ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE,
                                      ChangeAppIconDBEntity.ICON_CHANGE_TYPE_LATEST_INSTALLED_PAGE);
                        Intent intent2 = new Intent(Launcher.this, ChangeAppIcon.class);
                        intent2.putExtras(bundle);
                        startActivity(intent2);
                        alphaAnimOnLongClick(false, mSelectedView, recentlyGrid);
                        break;
                    default:
                        alphaAnimOnLongClick(false, mSelectedView, recentlyGrid);
                        break;
                }

            }
        });
        mPopupRecently.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                alphaAnimOnLongClick(false, mSelectedView, recentlyGrid);
                mPopupRecently.dismiss();
            }
        });
    }

    public static Launcher getInstance() {
        return sLauncher;
    }

    private void firstStartAction() {
        if (LauncherPreference.getFirstStart()) {
            welcomePageHelper.welcomePageShow();
            setWallpaperFirstStart();
            // 暂时关闭首次启动弹出“设置默认桌面”弹出提示，后续集成到用户引导
//            if (!DeviceUtils.isSpecialDevicesForDefaultLauncherGuide()) {
//                defaultLauncherGuideFirstStart();
//            }

            LauncherPreference.setFirstStart(false);

            SettingPreference.setRateAutoRemindFirstTime(System.nanoTime());
        }

        favoriteScene.blurFavoriteScene();
    }

    private final BroadcastReceiver mFavoriteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FavoritesData.saveFavoritesToDatabase(Launcher.this);
            updateFavorite();
        }
    };

    public SearchBarGroup getSearchBarGroup() {
        return mSearchBarGroup;
    }

    /**
     * first start setting wallpaper
     */
    public void setWallpaperFirstStart() {
        WallpaperUtil.setDefaultWallpaper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWeather.register();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        WallpaperUtil.suggestWallpaperDimensions(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWeather.unRegister();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
        mDragController.cancelDrag();
        unregisterHomeKeyReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLanguageChanged) {
            updateString();
            initFavoritePopup();
            initRecentPopup();
            initWeatherPopup();
            isLanguageChanged = false;
        }
        if (mDragLayer.getVisibility() == View.INVISIBLE) {
            mDragLayer.setAlpha(0);
            mDragLayer.setVisibility(View.VISIBLE);
            startDragLayerAnim();
        }
        if (needHideAllapp && workspace.isAllappShowed()) {
            workspace.hideAllapp();
            needHideAllapp = false;
        }
        mPaused = false;
        if (mOnResumeNeedsLoad) {
            mModel.startLoader(true);
            mOnResumeNeedsLoad = false;
        }

        if (mBindOnResumeCallbacks.size() > 0) {
            for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
                mBindOnResumeCallbacks.get(i).run();
            }
            mBindOnResumeCallbacks.clear();
        }
        if (mWeather != null) {
            mWeather.changeTimeAndDate(true);
        }

        mTimes = SystemClock.uptimeMillis();
        registerHomeKeyReceiver();

        if (mFisrtStart) {
            // Launcher首次启动
            MobclickAgent.onEvent(this, "LauncherFisrtStart");
            mFisrtStart = false;
        }
        mSearchBarGroup.resetEngineDisplay(this);

        RateAutoRemindCheckSelf();

        versionUpdateCheck();

        if (mDefaultLauncherGuide.isDefaultLauncher()) {
            // 返回桌面且设置为默认桌面的次数
            MobclickAgent.onEvent(this, "Defaultlauncherback");
        }

        //hot app插件加载广告
        Intent intent = new Intent(NanoWidgetUtils.ACTION_WIDGET_LOAD_MOBVISTA_NATIVE_AD);
        Launcher.this.sendBroadcast(intent);

        if (toolsAdpter != null) {
            toolsAdpter.initString();
        }
    }

    private void versionUpdateCheck() {
        if (welcomePageHelper.isShowing || SettingPreference.getGuideHome() || SettingPreference
            .getGuideFavorite() || SettingPreference.getGuideAllApp()) {
            return;
        }

        Intent intent = getIntent();
        boolean justAlert = false;
        if (intent != null && intent.hasExtra("just_alert")) {
            justAlert = intent.getBooleanExtra("just_alert", false);
            setIntent(mOriIntent);
        }
        mVersionDetector.checkUpdate(false, justAlert);
    }


    private void RateAutoRemindCheckSelf() {
        if (SettingPreference.getRateAutoRemindCount() <= RateDialog.AUTO_REMIND_COUNT_MAX) {

            if (mRateDialog == null) {
                mRateDialog = new RateDialog(Launcher.this);
            }

            int launcheCount = 30; // 从其他页面返回桌面的次数
            long useTime = 7200000000000l; // 2小时， nanosceond, 再次回到桌面的时间间隔

            if (FlavorController.testVersion) {
                launcheCount = 3; // 从其他页面返回桌面的次数
                useTime = 60000000000l; // 1分钟， nanosecond, 再次回到桌面的时间间隔
            }

            long time = System.nanoTime()
                        - SettingPreference.getRateAutoRemindFirstTime();

            if (mRateDialog.isThreeDaysAgo() && mRateDialog.rateCount++ >= launcheCount
                && time > useTime && !LauncherAppState.getInstance().tipsPopup.isShowen) {
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mRateDialog.showAlertDialog();
                        // 五星好评自动弹出
                    }
                }, 500);
            }
        }
    }

    /**
     * 将mDragLayer有
     */
    private void startDragLayerAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1f);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                mDragLayer.setAlpha(alpha);
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mModel != null) {
            mModel.unbindItemInfosAndClearQueuedBindRunnables();
        }

        try {
            mAppWidgetHost.stopListening();
        } catch (NullPointerException e) {
            //
        }
        mAppWidgetHost = null;
        mWidgetsToAdvance.clear();
        mSpeedyContainer.unRegisterReceiver();
        unregisterReceiver(mFavoriteReceiver);
        // unbindNoticeService();
        requestQueue.stop();

        mHandler.removeCallbacksAndMessages(null);
    }

    private void startWallpaper() {
        Intent wallpaperIntent = new Intent(sLauncher, OnlineWallpaperActivity.class);
        wallpaperIntent.putExtra("wallpaper_string", StringUtil.mStringMap);
        startActivity(wallpaperIntent);
//        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
//        // pickWallpaper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//        // | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        startActivitySafely(pickWallpaper);
    }

    private void startSetting() {
        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                          | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivitySafely(settings);
    }

    public void startApplicationDetailsActivity(ComponentName componentName) {
        String packageName = componentName.getPackageName();
        Intent intent = new Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
            "package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivitySafely(intent);
    }

    public boolean startApplicationUninstallActivity(
        ComponentName componentName, int flags) {
        if ((flags & AppInfo.DOWNLOADED_FLAG) == 0) {
            // System applications cannot be uninstall.
            return false;
        } else {
            String packageName = componentName.getPackageName();
            String className = componentName.getClassName();
            Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts(
                "package", packageName, className));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            return true;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mGuiding) {
            return true;
        }

        if (mSpeedDial.isDragState()) {
            mSpeedDial.stopDrag();
            return true;
        }

        if (mLongClick) {
            return true;
        }
        mLongClick = true;

        Object tag = v.getTag();

        if (tag instanceof String) {
            longClickWeatherArea();
        } else if (v instanceof BubbleView) {
            BubbleView view = (BubbleView) v;
            mSpeedDial.startDrag(view);
            UIEffectTools.onLongClickEffect(view);
        } else {
            showMenu();
            //menu进入次数
            MobclickAgent.onEvent(this, "IntoMenu");
        }

        return true;
    }

    public boolean startActivitySafely(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, StringUtil.getString(this, R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (mGuiding) {
            return;
        }

        Object tag = v.getTag();

        if (tag instanceof Integer) {
            UIEffectTools.onClickEffect(v);
            Integer num = (Integer) v.getTag();
            switch (num.intValue()) {
                case SpeedDial.EDIT_VIEW_ICON:
                    Bundle bundle = new Bundle();
                    bundle.putInt(ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE,
                                  ChangeAppIconDBEntity.ICON_CHANGE_TYPE_FRIST_PAGE);
                    Intent intent2 = new Intent(Launcher.this, ChangeAppIcon.class);
                    intent2.putExtras(bundle);
                    startActivity(intent2);
                    // getDragLayer().clearAnimation();
                    break;

                case SpeedDial.EDIT_VIEW_DELETE:
                    mSpeedDial.deleteBubbleView();
                    break;
            }
            return;
        }

        if (mSpeedDial.isDragState()) {
            mSpeedDial.stopDrag();
            return;
        }

        if (tag instanceof ShortcutInfo) {
            UIEffectTools.onClickEffect(v);
            final ShortcutInfo shortcut = (ShortcutInfo) tag;
            BubbleView bubbleView = (BubbleView) v;
            if (bubbleView.getShowNotice()) {
                showAlterAndSaveAction();
                bubbleView.setShowNotice(false);
                bubbleView.invalidate();
                return;
            }
             final Intent intent = shortcut.intent;
            if (intent != null) {
                startActivitySafely(intent);
                return;
            } else if ("*BROWSER*".equals(shortcut.title)) {
                LauncherAppState.getAppIntentUtil().startBrowserIntent();
                return;
            }
        }

        if (SettingPreference.getAdvanced()) {
            secondClickTime = System.currentTimeMillis();
            if (secondClickTime - firstClickTime < 500) {
                DevicePolicyManager mDPM =
                    (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName mDeviceAdminSample = new ComponentName(this, MyDeviceManager.class);
                if (mDPM.isAdminActive(mDeviceAdminSample)) {
                    mDPM.lockNow();
                } else {
                    SettingPreference.setAdvanced(false);
                }
            }
            firstClickTime = System.currentTimeMillis();
        }
    }

    /**
     * 此函数是判断手机上是否有指定的APP
     *
     * @param context     上下文对象
     * @param packageName app的包名
     * @return 如果手机上已经存在指定的app则返回true 否则返回false
     */
    private boolean isAPKInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName,
                                                       PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlterAndSaveAction() {
        AlertDialogUtil alertDialogUtil = new AlertDialogUtil(this);
        alertDialogUtil
            .showAlertDialog(true, false, AlertDialogUtil.AlertDialogType.TYPE_NOTIFICATION,
                             R.layout.alter_dialog);
        SharedPreferences preferences = this.getSharedPreferences(
            LauncherConstants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(SpeedDial.FRIST_SHOW_NOTICE, false);
        editor.commit();
    }

    public static boolean isNoticeApp(String name) {
        if (Launcher.NOTICE_PACKAGE_NAME.equals(name)) {
            return true;
        }
        return false;
    }

    public static boolean isIconUIApp(String name) {
        if (Launcher.ICONUI_PACKAGE_NAME.equals(name)) {
            return true;
        }
        return false;
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public SpeedDial getSpeedDial() {
        return mSpeedDial;
    }

    public SpeedyContainer getSpeedyContainer() {
        return mSpeedyContainer;
    }

    public LinearLayout getClockAndSpeedDial() {
        return mClockAndSpeedDial;
    }

    public ArrayList<AppInfo> getApps() {
        return mApps;
    }

    public DefaultLauncherGuide getDefaultLauncherGuide() {
        return mDefaultLauncherGuide;
    }

    public DialogUtil getLoadingDialog() {
        return mLoadingDialog;
    }

    public void showLoadingDialog() {
        if (mApps == null && mLoadingDialog == null) {
            mLoadingDialog = new DialogUtil(this);
            mLoadingDialog.showLoadingDialog(false);
        }
    }

    public void cancelLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.cancelLoadingDialogNoAnimation();
            mLoadingDialog = null;

            if (SettingPreference.getGuideFavorite()) {
                favoriteScene.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showGuidePage(STATE_FAVORITE);
                    }
                }, 500);
            }
        }
    }

    public void clearDefaultHome() {
        new DefaultLauncherGuide(getApplicationContext()).clearPreDefaultSetting();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer,
                     String[] args) {
        super.dump(prefix, fd, writer, args);
        Logger.dump(writer);
    }

    private boolean waitUntilResume(Runnable run,
                                    boolean deletePreviousRunnables) {
        if (mPaused) {
            Log.i(TAG, "Deferring update until onResume");
            if (deletePreviousRunnables) {
                while (mBindOnResumeCallbacks.remove(run)) {
                }
            }
            mBindOnResumeCallbacks.add(run);
            return true;
        } else {
            return false;
        }
    }

    private boolean waitUntilResume(Runnable run) {
        return waitUntilResume(run, false);
    }

    /**
     * If the activity is currently paused, signal that we need to re-run the loader in onResume.
     * This needs to be called from incoming places where resources might have been loaded while we
     * are paused. That is becaues the Configuration might be wrong when we're not running, and if
     * it comes back to what it was when we were paused, we are not restarted. Implementation of the
     * method from LauncherModel.Callbacks.
     *
     * @return true if we are currently paused. The caller might be able to skip some work in that
     * case since we will come back again.
     */
    @Override
    public boolean setLoadOnResume() {
        if (mPaused) {
            mOnResumeNeedsLoad = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void startBinding() {
        if (mAreaWidgetView != null) {
            mWeatherArea.removeView(mAreaWidgetView);
            mAreaWidgetView = null;
        }

        mApps = null;
        mBindOnResumeCallbacks.clear();
        mSpeedDial.startBind();
        mWidgetAdapter.startBinding();
        mFinishBindApp = false;
        mFinishBindHome = false;
    }

    @Override
    public void bindItems(final ArrayList<ShortcutInfo> shortcuts, final int start,
                          final int end, final boolean forceAnimateIcons) {
        Runnable r = new Runnable() {
            public void run() {
                bindItems(shortcuts, start, end, forceAnimateIcons);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        for (int i = start; i < end; i++) {
            ShortcutInfo info = shortcuts.get(i);
            mSpeedDial.addBubbleViewFromBind(info);
        }
        mSpeedDial.update();

    }

    @Override
    public void finishBindingItems() {
        Runnable r = new Runnable() {
            public void run() {
                finishBindingItems();
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        int appWidgetId = SettingPreference.getWidgetId();
        if (appWidgetId >= 0) {
            mAddWidgetInArea = true;
            completeAddAppWidget(appWidgetId, null, null);
            mWeatherArea.invalidate();
        }

        mSpeedDial.finishBind();

        mFinishBindHome = true;
        if (mLauncherStart) {
            showGuideHome();
        }
    }

    private void showGuideHome() {
        if (SettingPreference.getGuideHome()) {
            SettingPreference.setGuideHome(false);
            mSpeedDial.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showGuidePage(STATE_HOMESCREEN);
                }
            }, 500);
        }
    }

    @Override
    public void bindAllApplications(ArrayList<AppInfo> apps) {

        loadAllAppFilterNotice(apps);
        mApps = apps;
        bindFavorite();
        if (Allapp != null && apps != null) {
            Allapp.hideApp(apps);
            Allapp.bindAllApplications(apps);
        }

        cancelLoadingDialog();
        mFinishBindApp = true;
    }

    public void startLauncher() {
        clearDefaultHome();
        mGuiding = true;
        String lan = Locale.getDefault().getLanguage();
        if (lan.equals("en")) {
            mLauncherStart = true;
            if (mFinishBindHome) {
                showGuideHome();
            }
        } else {
            Intent intent = new Intent(this, ZenSettingLanguage.class);
            intent.putExtra("pop", true);
            startActivityForResult(intent, REQUEST_START_LANGUAGE);
        }
    }

    @Override
    public void bindAppsAdded(final ArrayList<AppInfo> addedApps) {
        //AdGetJson.obtainAdGetJson().getJSONVolley();

        mSpeedDial.postDelayed(new Runnable() {
            public void run() {
                mSpeedDial.updateFromBind();
            }
        }, 1500);

        if (mApps == null) {
            return;
        }
        Runnable r = new Runnable() {
            public void run() {
                bindAppsAdded(addedApps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        // 过滤屏蔽应用
        filterOurApp(addedApps);

        if (Allapp != null && addedApps != null) {
            Allapp.hideApp(addedApps);
            Allapp.notifyDataSetChanged(addedApps);
        }

        if (addedApps.size() <= 0) {
            return;
        }

        mApps.addAll(addedApps);
        FavoritesData.mApps.addAll(addedApps);
        FavoritesData.filterApps(this);
        bindFavorite();
        mSearchBarGroup.checkQRCodeInstallStatus();
    }

    @Override
    public void bindAppsUpdated(final ArrayList<AppInfo> apps) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppsUpdated(apps);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        filterOurApp(apps);
        mSpeedDial.updateFromBind();
    }

    @Override
    public void bindComponentsRemoved(final ArrayList<String> packageNames,
                                      final ArrayList<AppInfo> appInfos,
                                      final boolean packageRemoved) {

        if (mApps == null) {
            return;
        }
        Runnable r = new Runnable() {
            public void run() {
                bindComponentsRemoved(packageNames, appInfos, packageRemoved);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }
        for (AppInfo appInfo : appInfos) {
            ChangeAppIconDBEntity appIconDBEntity = new ChangeAppIconDBEntity();
            appIconDBEntity.setIconPackage(appInfo.componentName.getPackageName() + "/"
                                           + appInfo.componentName.getClassName());
            appIconDBSearcherApp.appIconDBUtils.deleteAppIcon(
                appIconDBEntity,
                ChangeAppIconDBUtils.DELETE_TYPE_ALL);
        }
        Allapp.refreshAllApp();
        removeNoticeApp(appInfos);
        mSpeedDial.removeBubbleViewFromBind(appInfos);
        mApps.removeAll(appInfos);
        bindAreaRemoved(packageNames, appInfos);

        FavoritesData.mApps.removeAll(appInfos);
        FavoritesData.datas.removeAll(appInfos);

        // 删除数据库记录
        for (AppInfo appInfo : appInfos) {
            LauncherModel.deleteItemFromDatabase(sLauncher, appInfo);
        }

        bindFavorite();

        if (Allapp != null && appInfos != null) {
            Allapp.notifyDataSetChanged(appInfos);
        }

        mWidgetAdapter.bindRemoved(packageNames, appInfos);
        mSearchBarGroup.checkQRCodeInstallStatus();

    }

    public void onHideAppChanged(final ArrayList<AppInfo> appInfos, final boolean isRemove) {
        if (mApps == null) {
            return;
        }
        if (isRemove) {
            mApps.removeAll(appInfos);
            FavoritesData.mApps.removeAll(appInfos);
            FavoritesData.datas.removeAll(appInfos);
            mSpeedDial.removeBubbleViewFromBind(appInfos);
            // Allapps内存中删除隐藏部分
            if (Allapp != null && appInfos != null) {
                Allapp.notifyDataSetChanged(appInfos);
            }
        } else {
            FavoritesData.mApps.addAll(appInfos);
            mApps.addAll(appInfos);
        }

        bindFavorite();
    }

    @Override
    public void bindSearchablesChanged() {

    }

    @Override
    public void bindAppWidget(final LauncherAppWidgetInfo info) {
        Runnable r = new Runnable() {
            public void run() {
                bindAppWidget(info);
            }
        };
        if (waitUntilResume(r)) {
            return;
        }

        int appWidgetId = info.appWidgetId;
        String widgetType = info.type;
        if ("nano".equals(widgetType)) {
            AppWidgetProviderInfo appWidgetInfo = NanoWidgetUtils.getNanoWidgetProviderInfo(this,
                                                                                            info.providerName);
            info.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            View
                widgetView =
                NanoWidgetUtils.getNanoWidgetView(this, appWidgetId, appWidgetInfo.configure
                    .getPackageName(), appWidgetInfo.configure.getClassName());
            if (widgetView != null) {
                info.hostView.addView(widgetView);
            }
//            info.hostView.addView(NanoWidgetUtils.getNanoWidgetView(this, appWidgetInfo.configure
//                .getPackageName(), appWidgetInfo.configure.getClassName()));
            setupAppWidgetNano(info);
            mWidgetAdapter.bindAppWidget(info);
        } else {
            AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (appWidgetInfo != null) {
                info.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
                setupAppWidget(info);
                mWidgetAdapter.bindAppWidget(info);
                addWidgetToAutoAdvanceIfNeeded(info.hostView, appWidgetInfo);
            }
        }
    }

    /**
     * 当移除notice app时做回到初始化阶段及不限miss number和设置界面里的消息提醒置为false
     */
    private void removeNoticeApp(final ArrayList<AppInfo> appInfos) {
        for (AppInfo appInfo : appInfos) {
            if (appInfo.componentName.getPackageName() != null
                && isNoticeApp(appInfo.componentName.getPackageName())) {
                appInfos.remove(appInfo);
                break;
            }
        }
        for (int j = appInfos.size() - 1; j >= 0; j--) {
            if (appInfos.get(j).componentName.getPackageName() != null
                && Launcher.isIconUIApp(appInfos.get(j).componentName.getPackageName())) {
                appInfos.remove(j);
                break;
            }
        }
    }

    /**
     * 当加载all app时剔除notice app
     */
    private void loadAllAppFilterNotice(final ArrayList<AppInfo> apps) {
        for (AppInfo appInfo : apps) {
            if (appInfo.componentName.getPackageName() != null
                && isNoticeApp(appInfo.componentName.getPackageName())) {
                apps.remove(appInfo);
                break;
            }
        }
        for (int j = apps.size() - 1; j >= 0; j--) {
            if (apps.get(j).componentName.getPackageName() != null
                && Launcher.isIconUIApp(apps.get(j).componentName.getPackageName())) {
                apps.remove(j);
                break;
            }
        }
    }

    /**
     * 当更新或者安装notice app/Zen UI 时剔除notice app/Zen UI 并且显示错过的数目
     */
    private void filterOurApp(final ArrayList<AppInfo> apps) {
        for (AppInfo appInfo : apps) {
            if (appInfo.componentName.getPackageName() != null
                && isNoticeApp(appInfo.componentName.getPackageName())) {
                apps.remove(appInfo);
                break;
            }
        }
        for (int j = apps.size() - 1; j >= 0; j--) {
            if (apps.get(j).componentName.getPackageName() != null
                && isIconUIApp(apps.get(j).componentName.getPackageName())) {
                apps.remove(apps.get(j));
                break;
            }
        }
    }

    @Override
    public void finishBindWidget() {
        mWidgetAdapter.finishBindWidget();
    }

    void startTipsSetting() {
        Intent intent = new Intent(this, TipsSetting.class);
        startActivity(intent);
    }

    void startZenSetting() {
        Intent intent = new Intent(this, ZenSetting.class);
        startActivity(intent);
    }


    public boolean isNotScroll() {
        if (mSpeedDial.isDragState() || (Allapp != null && Allapp.isPopup())
            || mPopupWeather.isShow() || mPopupWidget.isShow() || mPopupArea.isShow()
            || mPopupFavorite.isShow() || mPopupRecently.isShow() || mGuiding
            || (mPopupWindow != null && mPopupWindow.isShowing())
            || isWidgetOptionShow() || (mDragText != null && mDragText.isShowing())) {
            return true;
        }
        return false;
    }

    private void pressBackKey() {
        int state = switchPage.getCurrentPage();
        if (workspace.isAllappShowed()) {
            if (!workspace.isEditAllApp()) {
                workspace.hideAllappWithAnim();
            } else {
                workspace.postHideApp();
            }
        } else if (state == STATE_FAVORITE) {
            switchPage.snapToPage(STATE_HOMESCREEN);
        } else if (state == STATE_WIDGET) {
            if (hideWidgetOption()) {
                mWidgetsView.resetSelect();
            } else {
                switchPage.snapToPage(STATE_HOMESCREEN);
            }
        }
    }

    private void pressHomeKey() {
        cancelLoadingDialog();
        if (!isHomeScreen()) {
            if (workspace.isAllappShowed()) {
                workspace.hideAllappWithAnim();
            }
            switchPage.snapToPage(STATE_HOMESCREEN);
        }
        if (workspace.isEditAllApp()) {
            workspace.postHideApp();
        }
    }

    public void showAddWidget(boolean isHomeWidget) {
        mAddWidgetInArea = false;

        if (DeviceUtils.SDK_INT >= 16) {
            mDragLayer.setAlpha(0.5f);
            Intent intent = new Intent(this, SelectWidget.class);
            intent.putExtra("home_widget", isHomeWidget);
            startActivityForResult(intent, REQUEST_SELECT_WIDGET);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            mDragLayer.setVisibility(View.INVISIBLE);
        } else {
            Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
            int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            try {
                startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, StringUtil.getString(this, R.string.activity_not_found),
                               Toast.LENGTH_SHORT).show();
            }
        }
        //插件页添加按钮点击次数
        MobclickAgent.onEvent(this, "WidgetClickAddButton");
    }

    private WidgetOption mWidgetOption = null;

    public boolean isWidgetOptionShow() {
        return mWidgetOption != null && mWidgetOption.isShowing();
    }

    public void showWidgetOption() {
        if (mWidgetOption == null) {
            mWidgetOption = new WidgetOption(this);
        }
        mWidgetOption.updateString();
        mWidgetOption.show();
    }

    public boolean hideWidgetOption() {
        boolean result = false;

        if (mWidgetOption != null) {
            result = mWidgetOption.hide();
        }

        return result;
    }

    private View mBottomView = null;

    public void showBottomView() {
        if (mBottomView == null) {
            mBottomView = LayoutInflater.from(this).inflate(R.layout.widget_view_end, null);
            mBottomView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddWidget(false);
                }
            });

            if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
                && LauncherAppState.HasNavigationBar(this)) {
                View v = mBottomView.findViewById(R.id.end_view);
                v.setVisibility(View.VISIBLE);
            }

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
            mBottomView.setLayoutParams(params);
            mWidgets.addView(mBottomView);
        }
        TextView textView = (TextView) mBottomView.findViewById(R.id.widget_end_text);
        String textCurrent = textView.getText().toString();
        String textNew = StringUtil.getString(this, R.string.widget_add);
        if (!textCurrent.equals(textNew)) {
            textView.setText(textNew);//与OnGlobalLayoutListener会出现循环调用,频繁刷新
        }
        ImageView imageViewTip = (ImageView) mBottomView.findViewById(R.id.widget_new_tip);
        if (!PreferencesUtils.getBoolean(this, "weather_widget", false)) {
            imageViewTip.setVisibility(View.VISIBLE);
        } else {
            imageViewTip.setVisibility(View.GONE);
        }
        mBottomView.setVisibility(View.VISIBLE);
    }

    public void hideBottomView() {
        if (mBottomView != null) {
            mBottomView.setVisibility(View.INVISIBLE);
        }
    }

    private WidgetDrag mDragText = null;

    public void showDragText() {
        if (mDragText == null) {
            mDragText = new WidgetDrag(this);
        }
        mDragText.updateString();
        mDragText.show();
    }

    public void hideDragText() {
        if (mDragText != null) {
            mDragText.hide();
        }
    }

    public void hideAreaPop() {
        if (mPopupWeather != null && mPopupWeather.isShow()) {
            mPopupWeather.dismiss();
        }
        if (mPopupWidget != null && mPopupWidget.isShow()) {
            mPopupWidget.dismiss();
        }
        if (mPopupArea != null && mPopupArea.isShow()) {
            mPopupArea.dismiss();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mGuiding) {
            return true;
        }
        if (welcomePageHelper.isShowing) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                welcomePageHelper.RemoveViewShowGuide();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPaused || SystemClock.uptimeMillis() - mTimes < 500) {
                return true;
            }
            // 判断是否有菜单需要取消
            if (!dismissMenu()) {
                if (isHomeScreen()) {
                    mSpeedDial.stopDrag();
                }
                pressBackKey();
            }

            if (SpeedySetting.isBrightControlShow) {
                SpeedySetting.onPostBrightness();
                SpeedySetting.isBrightControlShow = false;
            }

        } else if (keyCode == KeyEvent.KEYCODE_MENU
                   && event.getAction() == KeyEvent.ACTION_UP) {
            if (!dismissMenu() && !mSpeedDial.isDragState() && !Allapp.isShowedAll()
                && !SpeedySetting.isBrightControlShow) {
                showMenu();
                //menu进入次数
                MobclickAgent.onEvent(this, "IntoMenu");
            }
        }

        return true;
    }

    private void dismissBrowserChooseDialog() {
        if (LauncherAppState.getAppIntentUtil() != null
            && LauncherAppState.getAppIntentUtil().browserUtil != null
            && LauncherAppState.getAppIntentUtil().browserUtil.chooseDialog != null) {
            LauncherAppState.getAppIntentUtil().browserUtil.chooseDialog.dismiss();
        }
    }

    /**
     * 按home键返回launcher首页
     *
     * @author leexingwang
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mOriIntent = getIntent();

        setIntent(intent);

        if (Intent.ACTION_MAIN.equals(intent.getAction())) {

            switchPage.removeGuide();
            workspace.removeGuide();

            if (welcomePageHelper.isShowing) {
                welcomePageHelper.RemoveViewShowGuide();
            }

            if (mRateDialog != null && mRateDialog.isShowing()) {
                mRateDialog.cancel();
            }

            dismissMenu();

            dismissBrowserChooseDialog();

            if (mGuidePage != null) {
                mGuidePage.dismissGuide();
                mGuidePage = null;
            }

            if (mAddBubble != null && mAddBubble.isShowing()) {
                mAddBubble.dismiss();
                mAddBubble = null;
            }

            hideAreaPop();

            if (Allapp.popujarTemp != null && Allapp.popujarTemp.isShow()) {
                Allapp.popujarTemp.dismiss();
                Allapp.popujarTemp = null;
            }

            if (SpeedySetting.isBrightControlShow) {
                SpeedySetting.onPostBrightness();
            }

            if (isHomeScreen()) {
                mSpeedDial.stopDrag();
            }

            if (hideWidgetOption()) {
                mWidgetsView.resetSelect();
            }

            long time = 10;
            if (mPopupFavorite != null && mPopupFavorite.isShow()) {
                mPopupFavorite.dismiss();
                time = 500;
            }

            if (mPopupRecently != null && mPopupRecently.isShow()) {
                mPopupRecently.dismiss();
                time = 500;
            }

            if (Allapp != null && Allapp.dismissPopup()) {
                time = 500;
            }
            if (isHomeKey) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        pressHomeKey();
                    }
                }, time);
            }
        }
    }

    public class HomeListenerReceiver extends BroadcastReceiver {

        private static final String SYSTEM_REASON = "reason";
        private static final String SYSTEM_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (SYSTEM_HOME_KEY.equals(reason)) {
                    isHomeKey = true;
                    if (mDefaultLauncherGuide != null) {
                        if (mDefaultLauncherGuide.getCurDefaultLauncher() != null
                            && !mDefaultLauncherGuide.isDefaultLauncher()) {
                            mDefaultLauncherGuide.showGuide();
                        }
                    } else {
                        mDefaultLauncherGuide = new DefaultLauncherGuide(getApplicationContext());
                        if (mDefaultLauncherGuide.getCurDefaultLauncher() != null
                            && !mDefaultLauncherGuide.isDefaultLauncher()) {
                            mDefaultLauncherGuide.showGuide();
                        }
                    }
                }
            }
        }

    }

    private HomeListenerReceiver mHomeKeyReceiver = null;
    private boolean isHomeKey;

    private void registerHomeKeyReceiver() {
        mHomeKeyReceiver = new HomeListenerReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        isHomeKey = false;
        registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private void unregisterHomeKeyReceiver() {
        if (mHomeKeyReceiver != null) {
            unregisterReceiver(mHomeKeyReceiver);
            mHomeKeyReceiver = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                mMoved = false;
                mLongClick = false;
                if (dismissMenu()) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int deltaX = Math.abs((int) ev.getX() - mDownX);
                int deltaY = Math.abs((int) ev.getY() - mDownY);
                if (deltaX > 15 || deltaY > 15) {
                    mMoved = true;
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    private PopupWindow mPopupWindow = null;

    private OnTouchListener mTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(getResources().getColor(R.color.menu_item_select_color));
            } else if (action == MotionEvent.ACTION_UP) {
                v.setBackgroundColor(Color.TRANSPARENT);
                switch (v.getId()) {
                    case R.id.menu_add:
                        if (!mSpeedDial.isFull() && mApps != null) {
                            mAddBubble = new AddBubble(Launcher.this);
                            mAddBubble.show();
                            // menu中点击添加app次数
                            MobclickAgent.onEvent(Launcher.this, "InMenuClickAddApp");
                        }
                        break;

                    case R.id.menu_wallpaper:
                        startWallpaper();
                        LauncherPreference.setMenuWallpaperAlert(false);
                        // menu中点击设置壁纸次数
                        MobclickAgent.onEvent(Launcher.this, "InMenuClickSetWallpaper");
                        break;

                    case R.id.menu_system:
                        startSetting();
                        // menu中点击进入系统设置次数
                        MobclickAgent.onEvent(Launcher.this, "InMenuClickSystemSettings");
                        break;

                    case R.id.menu_tips:
                        startTipsSetting();
                        LauncherPreference.setMenuTipsAlert(false);
                        // menu中点击进入智能提醒次数
                        MobclickAgent.onEvent(Launcher.this, "InMenuClickSmartReminder");
                        break;

                    case R.id.menu_zen:
                        LauncherPreference.setMenuZenSettingAlert(false);
                        startZenSetting();
                        // menu中点击进入zen设置次数
                        MobclickAgent.onEvent(Launcher.this, "InMenuClickZenSettings");

                        // 进入Nano桌面设置的总次数
                        MobclickAgent.onEvent(Launcher.this, "Clickintonanosetting");
                        break;

                    case R.id.menu_facebook:
                        //点击进入zen桌面facebook主页
                        LauncherPreference.setMenuFacebookAlert(false);
                        startActivitySafely(
                            AboutActivity.getOpenFacebookIntent(getApplicationContext()));
                        break;
                }
                mPopupWindow.dismiss();
                mPopupWindow = null;
            }

            return true;
        }
    };

    private static final float MENU_RATIO = 0.65f;

    private void showMenu() {
        if (mPopupWindow != null) {
            return;
        }

        // 进入菜单次数

        int width = DeviceUtils.getScreenPixelsWidth(this);
        width = (int) (width * MENU_RATIO);

        LayoutInflater mInflater = LayoutInflater.from(this);
        View view = mInflater.inflate(R.layout.menu, null);
        ImageView alertView;

        mPopupWindow = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);

        RelativeLayout v = (RelativeLayout) view.findViewById(R.id.menu_wallpaper);
        v.setOnTouchListener(mTouchListener);
        alertView = (ImageView) v.findViewById(R.id.img_wallpaper_alert);
        if (LauncherPreference.getMenuWallpaperAlert()) {
            alertView.setVisibility(View.VISIBLE);
        } else {
            alertView.setVisibility(View.GONE);
        }
        TextView textView = (TextView) v.findViewById(R.id.menu_wallpaper_text);
        String text = StringUtil.getString(this, R.string.wallpaper);
        textView.setText(text);

        v = (RelativeLayout) view.findViewById(R.id.menu_system);
        v.setOnTouchListener(mTouchListener);
        textView = (TextView) v.findViewById(R.id.menu_system_text);
        text = StringUtil.getString(this, R.string.settings);
        textView.setText(text);

        v = (RelativeLayout) view.findViewById(R.id.menu_tips);
        v.setOnTouchListener(mTouchListener);
        alertView = (ImageView) v.findViewById(R.id.img_tips_alert);
        if (LauncherPreference.getMenuTipsAlert()) {
            alertView.setVisibility(View.VISIBLE);
        } else {
            alertView.setVisibility(View.GONE);
        }
        textView = (TextView) v.findViewById(R.id.menu_tips_text);
        text = StringUtil.getString(this, R.string.tips_setting);
        textView.setText(text);

        v = (RelativeLayout) view.findViewById(R.id.menu_zen);
        v.setOnTouchListener(mTouchListener);
        alertView = (ImageView) v.findViewById(R.id.img_zen_setting_alert);
        if (LauncherPreference.getMenuZenSettingAlert()) {
            alertView.setVisibility(View.VISIBLE);
        } else {
            alertView.setVisibility(View.GONE);
        }
        textView = (TextView) v.findViewById(R.id.menu_zen_text);
        text = StringUtil.getString(this, R.string.zen_settings);
        textView.setText(text);

        v = (RelativeLayout) view.findViewById(R.id.menu_add);
        if (!mSpeedDial.isFull() && mApps != null && isHomeScreen()) {
            v.setOnTouchListener(mTouchListener);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
        textView = (TextView) v.findViewById(R.id.menu_add_text);
        text = StringUtil.getString(this, R.string.add);
        textView.setText(text);

        v = (RelativeLayout) view.findViewById(R.id.menu_facebook);
        v.setOnTouchListener(mTouchListener);
        alertView = (ImageView) v.findViewById(R.id.img_facebook_alert);
        if (LauncherPreference.getMenuFacebookAlert()) {
            alertView.setVisibility(View.VISIBLE);
        } else {
            alertView.setVisibility(View.GONE);
        }

        mPopupWindow.setAnimationStyle(R.style.menu_anim_style);

        int navigationHeight = 0;
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(this)) {
            navigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
        }

        mPopupWindow.showAtLocation(mDragLayer, Gravity.BOTTOM
                                                | Gravity.CENTER_HORIZONTAL, 0, navigationHeight);
    }

    private boolean dismissMenu() {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
            mPopupWindow = null;
            return true;
        }

        return false;
    }

    private GuidePage mGuidePage = null;

    public void showGuidePage(int state) {
        if (mGuidePage == null) {
            mGuidePage = new GuidePage(this);
        }

        switch (state) {
            case STATE_WIDGET:
                //do nothing
                break;
            case STATE_HOMESCREEN:
                mGuidePage.showGuide(state);
                break;

            case STATE_FAVORITE:
                if (SettingPreference.getGuideFavorite()) {
                    SettingPreference.setGuideFavorite(false);
                    mGuidePage.showGuide(state);
                }
                break;

            case STATE_ALLAPP:
                if (SettingPreference.getGuideAllApp()) {
                    SettingPreference.setGuideAllApp(false);
                    mGuidePage.showGuide(state);
                }
                break;
        }
    }

    public boolean isHomeScreen() {
        return switchPage.getCurrentPage() == STATE_HOMESCREEN;
    }

    private FavoriteAdapter mFavoriteAdapter = null;
    private GridView mFavoriteContainer;
    private View mSelectedView;

    public GridView getFavoriteContainer() {
        return mFavoriteContainer;
    }

    public void updateFavorite() {
        FavoritesData.removeAll(mSpeedDial.getPackageNames());
        FavoritesData.sort();
        if (mFavoriteAdapter == null) {
            mFavoriteContainer = (GridView) mDragLayer.findViewById(R.id.favorite_gridview);
            mFavoriteAdapter = new FavoriteAdapter();
            mFavoriteContainer.setAdapter(mFavoriteAdapter);
        } else {
            mFavoriteAdapter.notifyDataSetChanged();
        }
        updateRecentApp();
    }

    private GridView recentlyGrid = null;

    public void updateRecentApp() {
        if (mApps == null) {
            return;
        }

        mCopyApps.clear();
        for (int i = 0; i < mApps.size(); i++) {
            mCopyApps.add(mApps.get(i));
        }

        int appShowNumber = FavoritesData.datas.size() > 16 ? 16 : FavoritesData.datas.size();
        for (int i = 0; i < appShowNumber; i++) {
            for (int j = mCopyApps.size() - 1; j >= 0; j--) {
                if (FavoritesData.datas.get(i).title.equals(mCopyApps.get(j).title)) {
                    mCopyApps.remove(j);
                    break;
                }
            }
        }

        for (String name : getSpeedDial().getPackageNames()) {
            for (int i = 0; i < mApps.size(); i++) {
                if (name.equals(mApps.get(i).componentName.getPackageName())) {
                    mCopyApps.remove(mApps.get(i));
                    break;
                }
            }

        }
        Collections.sort(mCopyApps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (lhs.firstInstallTime > rhs.firstInstallTime) {
                    return -1;
                } else if (lhs.firstInstallTime < rhs.firstInstallTime) {
                    return 1;
                }
                return 0;
            }
        });

        recentlyGrid = (GridView) mDragLayer.findViewById(R.id.gv_recently);
        recentlyGrid.setAdapter(new RecentlyAdapter(this, mCopyApps));
        recentlyGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
        recentlyGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIEffectTools.onClickEffect(view);
                startActivitySafely(mCopyApps.get(position).intent);
                // 最新安装点击次数
                MobclickAgent.onEvent(Launcher.this, "ClickLatestInstall",
                                      mCopyApps.get(position).componentName.getPackageName());
            }
        });
        recentlyGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                           long id) {

                mSelectedView = view;
                alphaAnimOnLongClick(true, mSelectedView, recentlyGrid);
                // 请注意在遇到mPopupFavorite弹窗和UIEffectTools点击效果同时出现时请注意先后顺序，弹窗在前，点击效果在后！
                final AppInfo info = mCopyApps.get(position);
                mPopupRecently.setSelected(info);
                if (info.flags == 0) {
                    mPopupRecently.hideUninstallView();
                }
                mPopupRecently.show(view);
                UIEffectTools.onLongClickEffect(view);
                return true;
            }
        });
        recentlyGrid.invalidate();
    }

    private void areaAnimOnLongClick(boolean alphaOut) {
        PropertyValuesHolder alphaValuesHolder;
        if (alphaOut) {
            alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", 1f,
                                                             0.2f);
        } else {
            alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", 0.2f,
                                                             1f);
        }
        ObjectAnimator.ofPropertyValuesHolder(mSpeedDial, alphaValuesHolder).setDuration(100)
            .start();
        ObjectAnimator.ofPropertyValuesHolder(mSearchBottom, alphaValuesHolder).setDuration(100)
            .start();
    }

    private void longClickWeatherArea() {
        if (mMoved || mWeather == null || !isHomeScreen()) {
            return;
        }
        if (mWeather.getVisibility() == View.VISIBLE) {
            if (!mPopupWeather.isShow()) {
                mPopupWeather.show(mWeather);
            }
        } else if (mAreaWidgetView != null && mAreaWidgetView.getParent() != null) {
            if (!mPopupWidget.isShow()) {
                mPopupWidget.show(mAreaWidgetView);
            }
        } else {
            if (!mPopupArea.isShow()) {
                mPopupArea.show(mWeather);
            }
        }

        areaAnimOnLongClick(true);
    }

    public void bindAreaRemoved(ArrayList<String> packageNames, ArrayList<AppInfo> appInfos) {
        if (mAreaWidgetView == null) {
            return;
        }

        HashSet<String> cns = new HashSet<String>();
        for (AppInfo info : appInfos) {
            cns.add(info.componentName.getPackageName());
        }
        for (String name : packageNames) {
            cns.add(name);
        }

        if (cns.contains(mAreaWidgetName)) {
            removeAreaWidget();
            mWeather.show();
            mAreaState = STATE_WEATHER_VISIBLE;
            SettingPreference.setAreaState(mAreaState);
        }
    }

    public void refreshFavoriteAndRecent() {
        updateFavorite();
    }

    public void removeAreaWidget() {
        if (mAreaWidgetView != null) {
            mWeatherArea.removeView(mAreaWidgetView);

            final int appId = SettingPreference.getWidgetId();
            if (appId >= 0) {
                ThreadUtil.execute(new Runnable() {

                    @Override
                    public void run() {
                        mAppWidgetHost.deleteAppWidgetId(appId);
                    }
                });
            }

            removeWidgetToAutoAdvance(mAreaWidgetView);
            SettingPreference.setWidgetId(-1);

            mAreaWidgetView = null;
        }
    }

    private void initWeatherPopup() {
        PopuItem hide = new PopuItem(POPUP_SET_HIDE,
                                     StringUtil.getString(this, R.string.pop_set_hide),
                                     getResources().getDrawable(R.drawable.pop_invisible));
        PopuItem widget = new PopuItem(POPUP_REPLACE_WIDGET,
                                       StringUtil.getString(this, R.string.pop_replace_widget),
                                       getResources().getDrawable(R.drawable.pop_widget));
        mPopupWeather = new PopuJar(this, PopuJar.VERTICAL);
        mPopupWeather.addPopuItem(hide, PopuJar.APP_TYPE_NORMAL);
        mPopupWeather.addPopuItem(widget, PopuJar.APP_TYPE_END);
        mPopupWeather.setOnPopuItemClickListener(new PopuJar.OnPopuItemClickListener() {
            @Override
            public void onItemClick(PopuJar source, int pos, int actionId) {
                switch (actionId) {
                    case POPUP_SET_HIDE:
                        mWeather.hide();
                        mAreaState = STATE_WEATHER_INVISIBLE;
                        break;

                    case POPUP_REPLACE_WIDGET:
                        showAddWidget(true);
                        mAddWidgetInArea = true;
                        mAreaState = STATE_WIDGET_VISIBLE;
                        break;
                }
                areaAnimOnLongClick(false);
                SettingPreference.setAreaState(mAreaState);
            }
        });
        mPopupWeather.setShowSite(PopuJar.POP_ON_BOTTOM);

        PopuItem weather = new PopuItem(POPUP_REPLACE_WEATHER,
                                        StringUtil.getString(this, R.string.pop_replace_weather),
                                        getResources().getDrawable(R.drawable.pop_clock));
        mPopupWidget = new PopuJar(this, PopuJar.VERTICAL);
        mPopupWidget.addPopuItem(hide, PopuJar.APP_TYPE_NORMAL);
        mPopupWidget.addPopuItem(widget, PopuJar.APP_TYPE_NORMAL);
        mPopupWidget.addPopuItem(weather, PopuJar.APP_TYPE_END);
        mPopupWidget.setOnPopuItemClickListener(new PopuJar.OnPopuItemClickListener() {
            @Override
            public void onItemClick(PopuJar source, int pos, int actionId) {
                switch (actionId) {
                    case POPUP_SET_HIDE:
                        if (mAreaWidgetView != null) {
                            mWeatherArea.removeView(mAreaWidgetView);
                        }
                        mAreaState = STATE_WIDGET_INVISIBLE;
                        break;

                    case POPUP_REPLACE_WEATHER:
                        removeAreaWidget();
                        mWeather.show();
                        mAreaState = STATE_WEATHER_VISIBLE;
                        break;

                    case POPUP_REPLACE_WIDGET:
                        showAddWidget(true);
                        mAddWidgetInArea = true;
                        mAreaState = STATE_WIDGET_VISIBLE;
                        break;
                }
                areaAnimOnLongClick(false);
                SettingPreference.setAreaState(mAreaState);
            }
        });
        mPopupWidget.setShowSite(PopuJar.POP_ON_BOTTOM);

        PopuItem visible = new PopuItem(POPUP_SET_VISIBLE,
                                        StringUtil.getString(this, R.string.pop_set_visible),
                                        getResources().getDrawable(R.drawable.pop_visible));
        mPopupArea = new PopuJar(this, PopuJar.VERTICAL);
        mPopupArea.addPopuItem(visible, PopuJar.APP_TYPE_END);
        mPopupArea.setOnPopuItemClickListener(new PopuJar.OnPopuItemClickListener() {
            @Override
            public void onItemClick(PopuJar source, int pos, int actionId) {
                switch (actionId) {
                    case POPUP_SET_VISIBLE:
                        if (mAreaWidgetView != null) {
                            mWeatherArea.addView(mAreaWidgetView);
                            mAreaState = STATE_WIDGET_VISIBLE;
                        } else {
                            mWeather.show();
                            mAreaState = STATE_WEATHER_VISIBLE;
                        }
                        break;
                }
                areaAnimOnLongClick(false);
                SettingPreference.setAreaState(mAreaState);
            }
        });
        mPopupArea.setShowSite(PopuJar.POP_ON_BOTTOM);

        OnDismissListener dismiss = new OnDismissListener() {

            @Override
            public void onDismiss() {
                areaAnimOnLongClick(false);
            }
        };

        mPopupWeather.setOnDismissListener(dismiss);
        mPopupWidget.setOnDismissListener(dismiss);
        mPopupArea.setOnDismissListener(dismiss);
    }

    private void initFavoritePopup() {
        PopuItem moveOut = new PopuItem(POPUP_MOVE_OUT,
                                        StringUtil.getString(this, R.string.pop_move_out),
                                        getResources().getDrawable(R.drawable.move_out));
        PopuItem addSpeed = new PopuItem(POPUP_ADD_SPEED,
                                         StringUtil.getString(this, R.string.pop_add_speeddial),
                                         getResources().getDrawable(R.drawable.add_homescreen));
        PopuItem appInfo = new PopuItem(POPUP_APP_INFO,
                                        StringUtil.getString(this, R.string.pop_app_info),
                                        getResources().getDrawable(R.drawable.app_info));
        PopuItem unInstallApp = new PopuItem(POPUP_UNINSTALL_APP,
                                             StringUtil.getString(this, R.string.unload),
                                             getResources().getDrawable(R.drawable.app_uninstall));
        PopuItem changeAppIcon = new PopuItem(POPUP_CHANGE_APP_ICON,
                                              StringUtil.getString(this,
                                                                   R.string.pop_change_app_icon),
                                              getResources().getDrawable(
                                                  R.drawable.change_app_icon_popupwindow));
        mPopupFavorite = new PopuJar(this, PopuJar.VERTICAL);
        mPopupFavorite.addPopuItem(moveOut, PopuJar.APP_TYPE_NORMAL);
        mPopupFavorite.addPopuItem(addSpeed, PopuJar.APP_TYPE_NORMAL);
        mPopupFavorite.addPopuItem(changeAppIcon, PopuJar.APP_TYPE_NORMAL);
        mPopupFavorite.addPopuItem(appInfo, PopuJar.APP_TYPE_NORMAL);
        mPopupFavorite.addPopuItem(unInstallApp, PopuJar.APP_TYPE_SYS);
        mPopupFavorite.setAnimStyle(PopuJar.ANIM_AUTO);
        mPopupFavorite.setOnPopuItemClickListener(new PopuJar.OnPopuItemClickListener() {
            @Override
            public void onItemClick(PopuJar source, int pos, int actionId) {
                AppInfo info = mPopupFavorite.getSelected();
                switch (actionId) {
                    case POPUP_MOVE_OUT:

                        // 常用区域app move out点击次数
                        info.launchTimes = 0;
                        mCopyApps.add(info);
                        LauncherModel.deleteItemFromDatabase(Launcher.this, info);
                        info.id = ItemInfo.NO_ID;
                        AppInfo favorite =
                            FavoritesData.getAppInfo(info.componentName.getPackageName());
                        if (favorite != null) {
                            FavoritesData.remove(favorite);
                            updateFavorite();
                        }
                        alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
                        break;
                    case POPUP_ADD_SPEED:
                        if (!getSpeedDial().isFull()) {

                            // 常用区域app home screen点击次数
                            getSpeedDial().addBubbleView(info.makeShortcut());
                            getSpeedDial().update();
                            AppInfo favorite2 =
                                FavoritesData.getAppInfo(info.componentName.getPackageName());
                            if (favorite2 != null) {
                                FavoritesData.remove(favorite2);
                                updateFavorite();
                            }
                            Toast.makeText(Launcher.this,
                                           StringUtil.getString(Launcher.this,
                                                                R.string.pop_add_success),
                                           Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Launcher.this,
                                           StringUtil.getString(Launcher.this,
                                                                R.string.pop_add_fail),
                                           Toast.LENGTH_SHORT).show();
                        }
                        alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
                        break;
                    case POPUP_APP_INFO:
                        Uri
                            packageURI =
                            Uri.parse("package:" + info.componentName.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                   packageURI);
                        startActivitySafely(intent);
                        alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
                        // 常用区域app 的appinfo点击次数
                        break;
                    case POPUP_UNINSTALL_APP:
                        startApplicationUninstallActivity(info.componentName, info.flags);
                        alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
                        break;
                    case POPUP_CHANGE_APP_ICON:
                        Bundle bundle = new Bundle();
                        bundle.putInt(ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE,
                                      ChangeAppIconDBEntity.ICON_CHANGE_TYPE_MOST_USED_PAGE);
                        Intent intent2 = new Intent(Launcher.this, ChangeAppIcon.class);
                        intent2.putExtras(bundle);
                        startActivity(intent2);
                        alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
                        break;
                    default:
                        alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
                        break;
                }
            }
        });

        mPopupFavorite.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                alphaAnimOnLongClick(false, mSelectedView, mFavoriteContainer);
            }
        });
    }

    private void bindFavorite() {
        Collections.sort(mApps, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                if (lhs.firstInstallTime > rhs.firstInstallTime) {
                    return -1;
                } else if (lhs.firstInstallTime < rhs.firstInstallTime) {
                    return 1;
                }
                return 0;
            }
        });
        GridView recentlyGrid = (GridView) mDragLayer.findViewById(R.id.gv_recently);
        recentlyGrid.setAdapter(new RecentlyAdapter(this, mApps));
        recentlyGrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivitySafely(mApps.get(position).intent);
            }
        });
        updateFavorite();
    }

    /**
     * 判断最近安装列表显示的首个应用的安装时间是否在五天以内
     *
     * @return 在五天以内返回true，否则返回false；
     */
    @SuppressWarnings("deprecation")
    public boolean isDefaultDaysAgo() {
        if (mCopyApps != null && mCopyApps.size() != 0 && mCopyApps.get(0) != null) {
            Date date = new Date(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -DEFAULT_APP_INSTALL_DAY_BEFORE);
            return new Date(mCopyApps.get(0).firstInstallTime).before(new Date(
                (calendar.get(Calendar.YEAR) - THE_BASE_YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                date.getHours(), date.getMinutes(),
                date.getSeconds()));
        } else {
            return false;
        }
    }

    private class FavoriteAdapter extends BaseAdapter {

        ArrayList<AppInfo> appInfos;
        int size;

        public FavoriteAdapter() {
            appIconDBSearcherApp = new ChangeAppIconDBSearcherApp(getInstance());
            appInfos = FavoritesData.datas;
            size = getResources().getDimensionPixelSize(R.dimen.image_size);
        }

        @Override
        public int getCount() {
            int c = appInfos.size();
            if (c > LauncherAppState.DEFAULT_FAVORITE_NUM) {
                c = LauncherAppState.DEFAULT_FAVORITE_NUM;
            }
            return c;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(Launcher.this);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                imageView = (ImageView) convertView;
            }

            final AppInfo info = appInfos.get(position);
            Bitmap bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                info.componentName.getPackageName() + "/" + info.componentName.getClassName(),
                0, 0);
            if (bitmap != null) {
                info.iconBitmap = bitmap;
            }
            imageView.setImageBitmap(info.iconBitmap);
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    UIEffectTools.onClickEffect(v);
                    startActivitySafely(info.intent);

                    // 常用区域点app击次数
                    MobclickAgent.onEvent(Launcher.this, "FavoriteClickApp",
                                          info.componentName.getPackageName());
                }
            });
            imageView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    mPopupFavorite.setSelected(info);
                    mSelectedView = v;
                    alphaAnimOnLongClick(true, mSelectedView, mFavoriteContainer);
                    // 请注意在遇到mPopupFavorite弹窗和UIEffectTools点击效果同时出现时请注意先后顺序，弹窗在前，点击效果在后！
                    if (info.flags == 0) {
                        mPopupFavorite.hideUninstallView();
                    }
                    mPopupFavorite.show(v);
                    UIEffectTools.onLongClickEffect(v);
                    return true;
                }
            });

            return imageView;
        }
    }

    /**
     * 长按常用的图标，弹出pop框的同时，其他未被选中的图标变暗，pop框退出后，恢复状态
     *
     * @param alphaOut 是显示还是透明
     * @param view     禁止透明对象
     * @param parent   透明对象容器
     */
    private void alphaAnimOnLongClick(boolean alphaOut, View view, ViewGroup parent) {
        if (view == null) {
            return;
        }
        PropertyValuesHolder alphaValuesHolder;
        if (alphaOut) {
            alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", 1f,
                                                             0.2f);
        } else {
            alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", 0.2f,
                                                             1f);
        }

        View child = null;
        for (int i = 0; i < parent.getChildCount(); i++) {
            child = parent.getChildAt(i);
            if (!view.equals(child)) {
                ObjectAnimator.ofPropertyValuesHolder(child, alphaValuesHolder).setDuration(100)
                    .start();
            }
        }
        mPopupRecently.recoveryUninstallView();
        mPopupFavorite.recoveryUninstallView();
    }

    class RecentlyAdapter extends BaseAdapter {

        Context context;
        ArrayList<AppInfo> appInfos;

        public RecentlyAdapter(Context context, ArrayList<AppInfo> appInfos) {
            this.context = context;
            this.appInfos = appInfos;
        }

        @Override
        public int getCount() {
            return appInfos.size() > 4 ? 4 : appInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.temp_icon, null);
                holder.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.title = (TextView) convertView.findViewById(R.id.tv_title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            AppInfo info = appInfos.get(position);
            Bitmap bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                info.componentName.getPackageName() + "/" + info.componentName.getClassName(),
                0, 0);
            if (bitmap != null) {
                info.iconBitmap = bitmap;
            }
            holder.icon.setImageBitmap(info.iconBitmap);
            holder.title.setText(info.title);
            return convertView;

        }

        class ViewHolder {

            ImageView icon;
            TextView title;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mReceiver, filter);
        mAttached = true;
        mVisible = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        if (mAttached) {
            unregisterReceiver(mReceiver);
            mAttached = false;
        }
        updateRunning();
    }

    public void onWindowVisibilityChanged(int visibility) {
        mVisible = visibility == View.VISIBLE;
        updateRunning();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                updateRunning();
                screenOffDisnissPopup();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BIND_APPWIDGET) {
            int appWidgetId = data != null ?
                              data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, null, mPendingAddWidgetInfo);
            } else {
                if (mAppWidgetHost != null) {
                    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
            return;
        }

        if (requestCode == REQUEST_PICK_APPWIDGET) {
            int appWidgetId = data != null ?
                              data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (resultCode == RESULT_OK) {
                addAppWidgetImpl(appWidgetId, null, null);
            } else {
                if (mAppWidgetHost != null) {
                    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            }
            return;
        }

        if (requestCode == REQUEST_CREATE_APPWIDGET) {
            int appWidgetId = data != null ?
                              data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
            if (appWidgetId >= 0) {
                completeTwoStageWidgetDrop(resultCode, appWidgetId);
            }
            return;
        }

        if (requestCode == REQUEST_SELECT_WIDGET) {
            if (data != null) {
                if (data.getBooleanExtra("nano_widget", false)) {
                    AppWidgetProviderInfo info = data
                        .getParcelableExtra(SelectWidget.EXTRA_APPWIDGET_INFO);
                    addAppWidgetNano(info);
                } else {
                    AppWidgetProviderInfo info = data
                        .getParcelableExtra(SelectWidget.EXTRA_APPWIDGET_INFO);
                    addAppWidget(info);
                }
            }
            return;
        }

        if (requestCode == REQUEST_START_LANGUAGE) {
            mLauncherStart = true;
            if (mFinishBindHome) {
                showGuideHome();
            }
            return;
        }

        if (requestCode == REQUEST_USAGE_SETTING_GUIDE_PAGE) {
            Intent intent = new Intent(LauncherConstants.ACTION_USAGE_SETTING_TIP_REMOVE);
            this.sendBroadcast(intent);
            if (mGuidePage != null) {
                mGuidePage.checkUsageStatus(this);
            }
            return;
        }

        if (requestCode == REQUEST_USAGE_SETTING_ALERT) {
            Intent intent = new Intent(LauncherConstants.ACTION_USAGE_SETTING_TIP_REMOVE);
            this.sendBroadcast(intent);
            return;
        }
    }

    public void sendMissedMessage(int what, int arg) {
        mHandler.removeMessages(what);
        Message msg = mHandler.obtainMessage(what);
        msg.arg1 = arg;
        mHandler.sendMessage(msg);
    }

    private void sendAdvanceMessage(long delay) {
        mHandler.removeMessages(MSG_ADVANCE);
        Message msg = mHandler.obtainMessage(MSG_ADVANCE);
        mHandler.sendMessageDelayed(msg, delay);
        mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void updateRunning() {
        boolean autoAdvanceRunning = mVisible && mUserPresent && !mWidgetsToAdvance.isEmpty();
        if (autoAdvanceRunning != mAutoAdvanceRunning) {
            mAutoAdvanceRunning = autoAdvanceRunning;
            if (autoAdvanceRunning) {
                long delay = mAutoAdvanceTimeLeft == -1 ? mAdvanceInterval : mAutoAdvanceTimeLeft;
                sendAdvanceMessage(delay);
            } else {
                if (!mWidgetsToAdvance.isEmpty()) {
                    mAutoAdvanceTimeLeft = Math.max(0, mAdvanceInterval -
                                                       (System.currentTimeMillis()
                                                        - mAutoAdvanceSentTime));
                }
                mHandler.removeMessages(MSG_ADVANCE);
                mHandler.removeMessages(0);
            }
        }
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

        private final WeakReference<Launcher> mOuter;

        public MyHandler(Launcher outer) {
            mOuter = new WeakReference<Launcher>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            Launcher outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case MSG_ADVANCE:
                        int i = 0;
                        for (View key : mWidgetsToAdvance.keySet()) {
                            final View v = key.findViewById(
                                mWidgetsToAdvance.get(key).autoAdvanceViewId);
                            final int delay = mAdvanceStagger * i;
                            if (v instanceof Advanceable) {
                                postDelayed(new Runnable() {
                                    public void run() {
                                        if (Build.VERSION.SDK_INT
                                            >= Build.VERSION_CODES.JELLY_BEAN) {
                                            ((Advanceable) v).advance();
                                        }
                                    }
                                }, delay);
                            }
                            i++;
                        }
                        sendAdvanceMessage(mAdvanceInterval);
                        break;
                    case MSG_SMS_CHANGE:
                        if (mSpeedDial != null) {
                            mSpeedDial.refreshNotSMS(msg.arg1);
                        }

                        break;
                    case MSG_CALL_CHANGE:
                        if (mSpeedDial != null) {
                            mSpeedDial.refreshNotCall(msg.arg1);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void addWidgetToAutoAdvanceIfNeeded(View hostView,
                                                AppWidgetProviderInfo appWidgetInfo) {
        if (DeviceUtils.SDK_INT < 16 || appWidgetInfo == null
            || appWidgetInfo.autoAdvanceViewId == -1) {
            return;
        }
        View v = hostView.findViewById(appWidgetInfo.autoAdvanceViewId);
        if (v instanceof Advanceable) {
            mWidgetsToAdvance.put(hostView, appWidgetInfo);
            ((Advanceable) v).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    private void removeWidgetToAutoAdvance(View hostView) {
        if (DeviceUtils.SDK_INT < 16) {
            return;
        }
        if (mWidgetsToAdvance.containsKey(hostView)) {
            mWidgetsToAdvance.remove(hostView);
            updateRunning();
        }
    }

    public void removeAppWidget() {
        LauncherAppWidgetInfo info = mWidgetsView.getSelectInfo();

        mWidgetsView.resetSelect();
        hideWidgetOption();
        if (info != null) {
            removeWidgetToAutoAdvance(info.hostView);
            mWidgetAdapter.removeWidget(info);

            final int appId = info.appWidgetId;
            ThreadUtil.execute(new Runnable() {

                @Override
                public void run() {
                    mAppWidgetHost.deleteAppWidgetId(appId);
                }
            });

        }
    }

    private void completeTwoStageWidgetDrop(final int resultCode, final int appWidgetId) {
        if (resultCode == RESULT_OK) {
            AppWidgetHostView layout = mAppWidgetHost.createView(this, appWidgetId,
                                                                 mPendingAddWidgetInfo);
            completeAddAppWidget(appWidgetId, layout, null);
        }
    }

    public WidgetsListView getWidgetsView() {
        return mWidgetsView;
    }

    public static int[] getSpanForWidget(Context context, AppWidgetProviderInfo info) {
        int span = (int) (mDensity * 74);
        int spanX = (int) Math.ceil(info.minWidth / (double) span);
        int spanY = (int) Math.ceil(info.minHeight / (double) span);

        return new int[]{
            spanX, spanY, span
        };
    }

    @SuppressLint("NewApi")
    public static int[] getSizeForWidget(Context context, AppWidgetProviderInfo info) {
        Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, info.provider, null);
        int requiredWidth = info.minWidth + padding.left + padding.right;
        int requiredHeight = info.minHeight + padding.top + padding.bottom;
        int previewHeight = 0;

        int[] spanXY = getSpanForWidget(context, info);
        int spanX = spanXY[0];
        int spanY = spanXY[1];
        int span = spanXY[2];

        float ratio = info.minHeight / (float) info.minWidth;

        if (info.previewImage != 0) {
            Drawable drawable = context.getPackageManager().getDrawable(
                info.provider.getPackageName(), info.previewImage, null);

            previewHeight = drawable.getIntrinsicHeight();
        }

        int height = spanY * span;
        int s = span / 4;

        if (previewHeight > info.minHeight && ratio > 0.15) {
            if (previewHeight < span) {
                height = (int) (height * previewHeight / (float) info.minHeight);
            } else {
                height = previewHeight;
            }
            if (ratio != 1f) {
                height = (height + s - 1) / s * s;
            }
        }

        requiredHeight = (requiredHeight + s - 1) / s * s;
        if (requiredHeight < height) {
            requiredHeight = height;
        }

        requiredWidth = DeviceUtils.getScreenPixelsWidth(context) - (int) (mDensity * 10);

        if (info.minWidth > requiredWidth / 2) {
            requiredHeight += s * 2;
        } else if (requiredHeight < span * 2 && ratio > 0.15) {
            requiredHeight += s;
        }
        if (requiredHeight > span * 6) {
            requiredHeight = span * 6;
        }

        return new int[]{
            requiredWidth, requiredHeight, spanX, spanY
        };
    }

    private void setupAppWidget(LauncherAppWidgetInfo info) {
        info.hostView.setOnLongClickListener(mWidgetsView);
        info.notifyWidgetSizeChanged(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            info.width, info.height);
        info.hostView.setLayoutParams(lp);

        if (info.spanX == 1 && info.spanX == info.spanY) {
            info.hostView.setBackgroundResource(R.drawable.widget_item);
        }
    }

    @SuppressLint("NewApi")
    private void completeAddAppWidget(final int appWidgetId,
                                      AppWidgetHostView hostView,
                                      AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        if (appWidgetInfo == null) {
            ThreadUtil.execute(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            });
            return;
        }

        LauncherAppWidgetInfo info = new LauncherAppWidgetInfo(appWidgetId,
                                                               appWidgetInfo.provider);

        int[] size = getSizeForWidget(this, appWidgetInfo);

        info.width = size[0];
        info.height = size[1];
        info.spanX = size[2];
        info.spanY = size[3];

        if (hostView == null) {
            info.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            info.hostView.setAppWidget(appWidgetId, appWidgetInfo);
        } else {
            info.hostView = hostView;
        }

        if (mAddWidgetInArea) {
            mAddWidgetInArea = false;
            removeAreaWidget();
            mWeather.hide();

            info.width = DeviceUtils.getScreenPixelsWidth(this) - (int) (mDensity * 40);
            if (info.height > mWeatherArea.getHeight()) {
                info.height = mWeatherArea.getHeight();
            }
            info.notifyWidgetSizeChanged(this);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                info.width, info.height);
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            info.hostView.setLayoutParams(lp);

            if (mAreaState != STATE_WIDGET_INVISIBLE) {
                mWeatherArea.addView(info.hostView);
            }

            mAreaWidgetView = info.hostView;
            mAreaWidgetView.setTag("widget");
            mAreaWidgetView.setOnLongClickListener(this);
            mAreaWidgetName = appWidgetInfo.provider.getPackageName();
            SettingPreference.setWidgetId(appWidgetId);
        } else {
            setupAppWidget(info);
            mWidgetAdapter.addWidget(info);
            mWidgetsView.setSelection(mWidgetAdapter.getCount() - 1);

            // 插件页添加插件成功次数
            MobclickAgent.onEvent(Launcher.this, "Addwidgetsuccess");
        }

        addWidgetToAutoAdvanceIfNeeded(info.hostView, appWidgetInfo);
    }

    private void addAppWidgetImpl(final int appWidgetId, AppWidgetHostView boundWidget,
                                  AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        if (appWidgetInfo != null && appWidgetInfo.configure != null) {
            mPendingAddWidgetInfo = appWidgetInfo;

            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            try {
                startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this,
                               StringUtil.getString(this, R.string.activity_not_found),
                               Toast.LENGTH_SHORT).show();
            }

        } else {
            completeAddAppWidget(appWidgetId, boundWidget,
                                 appWidgetInfo);
        }
    }

    @SuppressLint("NewApi")
    public void addAppWidget(AppWidgetProviderInfo info) {
        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        boolean success = false;

        if (DeviceUtils.SDK_INT >= 16) {
            success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,
                                                                 info.provider);
        }
        if (success) {
            addAppWidgetImpl(appWidgetId, null, info);
        } else {
            mPendingAddWidgetInfo = info;
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider);
            try {
                startActivityForResult(intent, Launcher.REQUEST_BIND_APPWIDGET);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this,
                               StringUtil.getString(this, R.string.activity_not_found),
                               Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupAppWidgetNano(LauncherAppWidgetInfo info) {
        info.hostView.setOnLongClickListener(mWidgetsView);
//        info.notifyWidgetSizeChanged(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            info.width, info.height);
        info.hostView.setLayoutParams(lp);

//        if (info.spanX == 1 && info.spanX == info.spanY) {
//            info.hostView.setBackgroundResource(R.drawable.widget_item);
//        }
    }

    @SuppressLint("NewApi")
    private void completeAddAppWidgetNano(final int appWidgetId,
                                          AppWidgetHostView hostView,
                                          AppWidgetProviderInfo appWidgetInfo) {
        if (appWidgetInfo == null) {
            appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        }

        if (appWidgetInfo == null) {
            ThreadUtil.execute(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                }
            });
            return;
        }

        LauncherAppWidgetInfo info = new LauncherAppWidgetInfo(appWidgetId,
                                                               appWidgetInfo.provider);

        info.type = "nano";

        int[] size = getSizeForWidget(this, appWidgetInfo);

        info.width = size[0];
        info.height = size[1];
        info.spanX = size[2];
        info.spanY = size[3];
        if (hostView == null) {
            info.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
            info.hostView.setAppWidget(appWidgetId, appWidgetInfo);
            View
                widgetView =
                NanoWidgetUtils.getNanoWidgetView(this, appWidgetId, appWidgetInfo.configure
                    .getPackageName(), appWidgetInfo.configure.getClassName());
            if (widgetView != null) {
                info.hostView.addView(widgetView);
            }
//            info.hostView.addView(NanoWidgetUtils.getNanoWidgetView(this, appWidgetInfo.configure
//                .getPackageName(), appWidgetInfo.configure.getClassName()));
        } else {
            info.hostView = hostView;
            info.hostView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (mAddWidgetInArea) {
            mAddWidgetInArea = false;
            removeAreaWidget();
            mWeather.hide();

            info.width = DeviceUtils.getScreenPixelsWidth(this) - (int) (mDensity * 40);
            if (info.height > mWeatherArea.getHeight()) {
                info.height = mWeatherArea.getHeight();
            }
            info.notifyWidgetSizeChanged(this);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                info.width, info.height);
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            info.hostView.setLayoutParams(lp);

            if (mAreaState != STATE_WIDGET_INVISIBLE) {
                mWeatherArea.addView(info.hostView);
            }

            mAreaWidgetView = info.hostView;
            mAreaWidgetView.setTag("widget");
            mAreaWidgetView.setOnLongClickListener(this);
            mAreaWidgetName = appWidgetInfo.provider.getPackageName();
            SettingPreference.setWidgetId(appWidgetId);
        } else {
            setupAppWidgetNano(info);
            mWidgetAdapter.addWidget(info);
            mWidgetsView.setSelection(mWidgetAdapter.getCount() - 1);

            // 插件页添加插件成功次数
            MobclickAgent.onEvent(Launcher.this, "Addwidgetsuccess");
        }

        addWidgetToAutoAdvanceIfNeeded(info.hostView, appWidgetInfo);
    }

    private void addAppWidgetNanoImpl(final int appWidgetId, AppWidgetHostView boundWidget,
                                      AppWidgetProviderInfo appWidgetInfo) {
        completeAddAppWidgetNano(appWidgetId, boundWidget,
                                 appWidgetInfo);
    }

    public void addAppWidgetNano(AppWidgetProviderInfo info) {
        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        addAppWidgetNanoImpl(appWidgetId, null, info);
    }

    private static final String PREFERENCES = "launcher.preferences";

    private static LocaleConfiguration sLocaleConfiguration = null;

    private void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() {
                @Override
                protected LocaleConfiguration doInBackground(Void... unused) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                @Override
                protected void onPostExecute(LocaleConfiguration result) {
                    sLocaleConfiguration = result;
                    checkForLocaleChange(); // recursive, but now with a locale
                    // configuration
                }
            }.execute();
            return;
        }

        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = sLocaleConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = sLocaleConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = sLocaleConfiguration.mnc;
        final int mnc = configuration.mnc;

        boolean localeChanged = !locale.equals(previousLocale) || mcc != previousMcc
                                || mnc != previousMnc;

        if (localeChanged) {
            sLocaleConfiguration.locale = locale;
            sLocaleConfiguration.mcc = mcc;
            sLocaleConfiguration.mnc = mnc;

            mIconCache.flush();

            final LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            ThreadUtil.execute(new Runnable() {

                @Override
                public void run() {
                    if (localeConfiguration != null) {
                        writeConfiguration(Launcher.this, localeConfiguration);
                    }
                }
            });

        }
    }

    private static class LocaleConfiguration {

        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }

    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } catch (NullPointerException e) {
            // Ignore
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void bindHidesApp(ArrayList<AppInfo> hidens) {
        Allapp.bindHideApps(hidens);
    }

    @Override
    public void onRemoveApp(ArrayList<AppInfo> removes) {
        Allapp.removeAppInfo(removes);
    }

    /**
     * 灭屏后做相应的菜单收回操作！ 注意需要与onnewintent()方法中的操作同步！
     */
    private void screenOffDisnissPopup() {
        if (mRateDialog != null && mRateDialog.isShowing()) {
            mRateDialog.cancel();
        }

        dismissMenu();

        dismissBrowserChooseDialog();

        if (mAddBubble != null && mAddBubble.isShowing()) {
            mAddBubble.dismiss();
            mAddBubble = null;
        }

        hideAreaPop();

        if (Allapp.popujarTemp != null && Allapp.popujarTemp.isShow()) {
            Allapp.popujarTemp.dismiss();
            Allapp.popujarTemp = null;
        }

        if (SpeedySetting.isBrightControlShow) {
            SpeedySetting.onPostBrightness();
        }

        if (isHomeScreen()) {
            mSpeedDial.stopDrag();
        }

        if (hideWidgetOption()) {
            mWidgetsView.resetSelect();
        }

        if (mPopupFavorite != null && mPopupFavorite.isShow()) {
            mPopupFavorite.dismiss();
        }

        if (mPopupRecently != null && mPopupRecently.isShow()) {
            mPopupRecently.dismiss();
        }
        if (Allapp != null) {
            Allapp.dismissPopup();
        }

    }

    public AppWidgetHost getAppWidgetHost() {
        return mAppWidgetHost;
    }

    public void initSmsAndCalls() {
        mSmsAndCalls = new SmsAndCalls(this, new MyHandler(this));
    }


}
