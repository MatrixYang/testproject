package com.cooeeui.brand.zenlauncher.changeicon;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.cooeeui.basecore.customview.HeaderGridView;
import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBEntity;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBHelp;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBSearcherApp;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBUtils;
import com.cooeeui.brand.zenlauncher.preferences.Preferences;
import com.cooeeui.brand.zenlauncher.preferences.SharedPreferencesUtil;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.umeng.analytics.MobclickAgent;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChangeAppIcon extends BaseActivity implements OnClickListener {

    private int mNumcolumns;// 每一行几个图标
    private GridView mGridView_suggestion;
    private HeaderGridView mGridView_all;
    private ImageView mImageView_oldIcon;
    private ImageView mImageView_newIcon;
    private RelativeLayout mRelativeLayout_moreIcon;
    private LinearLayout mLinearLayout_OK;
    private ImageView mImageView_by_Album;
    private ImageView mImageView_triangle;
    private TextView mTextView_Dialog_All_Yes;
    private TextView mTextView_Dialog_All_No;
    private CheckBox mCheckBox_Dialog;
    private Spinner mChoosePackageNameSpinner;
    private View mHeaderView;
    private TextView mTextView_Not_Support;
    private Context slaveContext = null;
    // 当前更换图标的app info
    private ShortcutInfo info;
    private Bitmap firstIcon;
    private Bitmap mTargetBitmap;
    private ArrayList<IconBase> iconBases = new ArrayList<IconBase>();
    private ArrayList<IconBase> allIconBases = new ArrayList<IconBase>();
    private ArrayList<IconBase> suggestionIconBases = new ArrayList<IconBase>();
    private PackageManager mPackageManager;
    private SharedPreferences preferences;
    // our icon package name
    private final static String ICON_PKG_NAME = "com.cooeeui.iconui";
    public final static String ICON_APP_NAME = "NanoUI";
    private int[] mDefualtAppIcons = {
        R.raw.camera, R.raw.contacts,
        R.raw.setting, R.raw.dial, R.raw.sms, R.raw.browser,
    };
    private ChangeAppIconDBUtils changeAppIconDBUtils;
    private ChangeAppIconDBEntity appIconDBEntity;
    private int change_app_icon_delete_type = 0;
    private ChangeAppIconDBSearcherApp appIconDBSearcherApp;
    // 存储当前icon package 的name
    public static String iconPackageName = "com.cooeeui.iconui";
    private String suggestionDrawableName = null;
    public static final String BROWSER_NAME = "*BROWSER*";
    private DialogUtil dialogUtil;
    private final static int LOAD_OK = 100;
    private final static int SHOW_LOADING = 101;
    List<AppItemInfo> itemInfos = new ArrayList<AppItemInfo>();
    private final Handler mHandler = new MyHandler(this);
    private Thread thread;
    private boolean clickNanoUi = false;
    private List<String> otherPackageNames = new ArrayList<String>();
    private AppIconSpinnerAdapter adapter;

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

        private final WeakReference<ChangeAppIcon> mOuter;

        public MyHandler(ChangeAppIcon outer) {
            mOuter = new WeakReference<ChangeAppIcon>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            ChangeAppIcon outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case LOAD_OK:
                        loadingOkshowView();
                        break;
                    case SHOW_LOADING:
                        dialogUtil = new DialogUtil(ChangeAppIcon.this);
                        dialogUtil.showLoadingDialog(false);
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_app_icon);
        preferences = this.getSharedPreferences("change_app_icon_show_dialog",
                                                Context.MODE_PRIVATE);
        try {
            getDefaultIcon();
            initView();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        dialogUtil = new DialogUtil(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 1.给两个gridview添加点击事件 2.给相应的view添加事件监听
     */

    @SuppressLint("NewApi")
    private void setAllClickLinsener() {
        mGridView_all.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                UIEffectTools.onClickEffect(view);
                switch (allIconBases.get(position).getTypeIcon()) {
                    case IconBase.ICON_TYPE_ZEN_SIX_ICON:
                        // 对新的api接口进行处理
                        if (Build.VERSION.SDK_INT > 20) {
                            mImageView_newIcon.setImageDrawable(ChangeAppIcon.this
                                                                    .getResources().getDrawable(
                                    allIconBases.get(position).getImageId(), null));
                        } else {
                            mImageView_newIcon.setImageDrawable(ChangeAppIcon.this
                                                                    .getResources().getDrawable(
                                    allIconBases.get(position).getImageId()));
                        }
                        mTargetBitmap = BitmapFactory.decodeResource(
                            ChangeAppIcon.this.getResources(), allIconBases
                                .get(position).getImageId());
                        break;
                    case IconBase.ICON_TYPE_OUR_APPLICATION:
                        if (Build.VERSION.SDK_INT > 20) {
                            mImageView_newIcon.setImageDrawable(slaveContext
                                                                    .getResources().getDrawable(
                                    allIconBases.get(position).getImageId(), null));
                        } else {
                            mImageView_newIcon.setImageDrawable(slaveContext
                                                                    .getResources().getDrawable(
                                    allIconBases.get(position).getImageId()));
                        }
                        mTargetBitmap = BitmapFactory.decodeResource(
                            slaveContext.getResources(),
                            allIconBases.get(position).getImageId());
                        break;
                    case IconBase.ICON_TYPE_THIRD_PARTY_APPLICATION:

                        break;

                    default:
                        break;
                }
            }
        });
        mGridView_suggestion.setOnItemClickListener(new OnItemClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                UIEffectTools.onClickEffect(view);
                switch (suggestionIconBases.get(position).getTypeIcon()) {
                    case IconBase.ICON_TYPE_APP_DEFALUT:
                        mImageView_newIcon.setImageBitmap(suggestionIconBases.get(
                            position).getIconBitmap());
                        mTargetBitmap = suggestionIconBases.get(position)
                            .getIconBitmap();
                        break;
                    case IconBase.ICON_TYPE_ZEN_SIX_ICON:
                        if (Build.VERSION.SDK_INT > 20) {
                            mImageView_newIcon.setImageDrawable(ChangeAppIcon.this
                                                                    .getResources().getDrawable(
                                    suggestionIconBases.get(position)
                                        .getImageId(), null));
                        } else {
                            mImageView_newIcon.setImageDrawable(ChangeAppIcon.this
                                                                    .getResources().getDrawable(
                                    suggestionIconBases.get(position)
                                        .getImageId()));
                        }
                        mTargetBitmap = BitmapFactory.decodeResource(
                            ChangeAppIcon.this.getResources(),
                            suggestionIconBases.get(position).getImageId());
                        break;
                    case IconBase.ICON_TYPE_OUR_APPLICATION:
                        if (Build.VERSION.SDK_INT > 20) {
                            mImageView_newIcon.setImageDrawable(slaveContext
                                                                    .getResources().getDrawable(
                                    suggestionIconBases.get(position)
                                        .getImageId(), null));
                        } else {
                            mImageView_newIcon.setImageDrawable(slaveContext
                                                                    .getResources().getDrawable(
                                    suggestionIconBases.get(position)
                                        .getImageId()));
                        }
                        mTargetBitmap = BitmapFactory.decodeResource(slaveContext
                                                                         .getResources(),
                                                                     suggestionIconBases
                                                                         .get(position)
                                                                         .getImageId());
                        break;
                    case IconBase.ICON_TYPE_THIRD_PARTY_APPLICATION:
                        if (Build.VERSION.SDK_INT > 20) {
                            mImageView_newIcon.setImageDrawable(suggestionIconBases
                                                                    .get(position)
                                                                    .getContext()
                                                                    .getResources()
                                                                    .getDrawable(
                                                                        suggestionIconBases
                                                                            .get(position)
                                                                            .getImageId(), null));
                        } else {
                            mImageView_newIcon.setImageDrawable(suggestionIconBases
                                                                    .get(position)
                                                                    .getContext()
                                                                    .getResources()
                                                                    .getDrawable(
                                                                        suggestionIconBases
                                                                            .get(position)
                                                                            .getImageId()));
                        }
                        mTargetBitmap = BitmapFactory.decodeResource(
                            suggestionIconBases.get(position).getContext()
                                .getResources(),
                            suggestionIconBases.get(position).getImageId());
                        break;
                }
            }
        });
        mLinearLayout_OK.setOnClickListener(this);
        mRelativeLayout_moreIcon.setOnClickListener(this);
        mImageView_by_Album.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_change_app_icon_moreicon:
                ChangeAppIconHelp.gotoDownloadAPK(this,
                                                  ChangeAppIconHelp.CHANGE_APP_PKG_MORE_URL);

                // 点击更多图标包次数
                MobclickAgent.onEvent(ChangeAppIcon.this, "Clickmoreicon");
                break;
            case R.id.ll_change_app_icon_ok:
                if (!preferences.getBoolean("change_app_icon_show_dialog_agin",
                                            true)) {
                    showAlertDialog();
                } else {
                    if (mTargetBitmap != null) {
                        // Launcher.getInstance().getSpeedDial().changeIcon(mTargetBitmap);
                        appIconDBEntity.setIcon(mTargetBitmap);
                        changeAppIconDBUtils.deleteAppIcon(appIconDBEntity,
                                                           change_app_icon_delete_type);
                        changeAppIconDBUtils.insertAppIcon(appIconDBEntity);
                        refreshAll();
                    }
                    finishAll();
                }
                break;
            case R.id.iv_change_app_by_album:

                break;
            default:
                break;
        }
    }

    /**
     * 1.获取bundle传过来的数据； 2.读取app的原来的图片；
     */
    public void getDefaultIcon() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        switch (bundle.getInt(ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE)) {
            case ChangeAppIconDBEntity.ICON_CHANGE_TYPE_FRIST_PAGE:
                if (Launcher.getInstance().getSpeedDial().getmSelect() == null) {
                    return;
                }
                info = (ShortcutInfo) Launcher.getInstance().getSpeedDial()
                    .getmSelect().getTag();
                change_app_icon_delete_type =
                    ChangeAppIconDBUtils.DELETE_TYPE_BY_CHANGE_TYPE_FRIST_PAGE;
                break;
            case ChangeAppIconDBEntity.ICON_CHANGE_TYPE_MOST_USED_PAGE:
                info = Launcher.getInstance().getmPopupFavorite().getSelected()
                    .makeShortcut();
                change_app_icon_delete_type = ChangeAppIconDBUtils.DELETE_TYPE_BY_CHANGE_TYPE;
                break;
            case ChangeAppIconDBEntity.ICON_CHANGE_TYPE_LATEST_INSTALLED_PAGE:
                info = Launcher.getInstance().getmPopupRecently().getSelected()
                    .makeShortcut();
                change_app_icon_delete_type = ChangeAppIconDBUtils.DELETE_TYPE_BY_CHANGE_TYPE;
                break;
            case ChangeAppIconDBEntity.ICON_CHANGE_TYPE_ALL_APPS_PAGE:
                info = Launcher.getInstance().Allapp.getmSelectedInfo()
                    .makeShortcut();
                change_app_icon_delete_type = ChangeAppIconDBUtils.DELETE_TYPE_BY_CHANGE_TYPE;
                break;
        }
        changeAppIconDBUtils = new ChangeAppIconDBUtils(this);
        appIconDBEntity = new ChangeAppIconDBEntity();
        appIconDBEntity.setIconTitle((String) info.title);
        appIconDBEntity.setIconChangeType(bundle
                                              .getInt(
                                                  ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE));
        if (info.position != 0) {
            appIconDBEntity.setIconPosition(info.position);
        } else {
            appIconDBEntity
                .setIconPosition(ChangeAppIconDBEntity.DELAULT_ICON_POSITION);
        }
        appIconDBEntity
            .setIconType(ChangeAppIconDBEntity.ICON_TYPE_OUR_APPLICATION);

        // 对浏览器进行特殊处理
        if (BROWSER_NAME.equals(info.title)) {
            appIconDBEntity.setIconPackage((String) info.title);
        } else {
            Log.i("XML", "AAAAAAA"
                         + info.getIntent().getComponent().getPackageName() + "/"
                         + info.getIntent().getComponent().getClassName());
            // 非浏览器
            appIconDBEntity.setIconPackage(info.getIntent().getComponent()
                                               .getPackageName() == null ? "hahao"
                                                                         : info.getIntent()
                                                                               .getComponent()
                                                                               .getPackageName()
                                                                           + "/" + info
                                                                               .getIntent()
                                                                               .getComponent()
                                                                               .getClassName());
        }
        firstIcon = null;
        LauncherAppState app = LauncherAppState.getInstance();
        if (info.intent != null) {
            firstIcon = app.getIconCache().getIcon(info.intent);
            // if (app.getIconCache().isDefaultIcon(firstIcon)) {
            // firstIcon = null;
            // }
        }
        if (firstIcon != null) {
            IconBase base = new IconBase();
            base.setIconBitmap(firstIcon);
            base.setTypeIcon(IconBase.ICON_TYPE_APP_DEFALUT);
            suggestionIconBases.add(base);
        }
        if (info.mIconId != 0xffffffff) {
            IconBase base = new IconBase();
            base.setImageId(info.mIconId);
            base.setName((String) info.title);
            base.setTypeIcon(IconBase.ICON_TYPE_ZEN_SIX_ICON);
            // base.setContext(this);
            suggestionIconBases.add(base);
        }
    }

    /**
     * 初始化界面包括读取app现在的图片
     */
    @SuppressLint("InflateParams")
    private void initView() {
        mNumcolumns = getResources().getInteger(R.dimen.change_app_icon_numcolumns);
        mHeaderView = LayoutInflater.from(this)
            .inflate(R.layout.chang_app_icon_header_layout, null);
        mGridView_suggestion = (GridView) mHeaderView
            .findViewById(R.id.gv_change_app_icon_suggestion);
        mGridView_all = (HeaderGridView) findViewById(R.id.gv_change_app_icon_all);
        mImageView_oldIcon = (ImageView) findViewById(R.id.iv_change_app_icon_old_icon);
        mImageView_newIcon = (ImageView) findViewById(R.id.iv_change_app_icon_new_icon);
        mRelativeLayout_moreIcon = (RelativeLayout) findViewById(R.id.rl_change_app_icon_moreicon);
        mLinearLayout_OK = (LinearLayout) findViewById(R.id.ll_change_app_icon_ok);
        mImageView_by_Album = (ImageView) findViewById(R.id.iv_change_app_by_album);
        mImageView_triangle = (ImageView) findViewById(R.id.iv_change_app_icon_triangle);
        mChoosePackageNameSpinner = (Spinner) findViewById(R.id.sp_changeicon_package);
        mTextView_Not_Support = (TextView) findViewById(R.id.tv_change_app_icon_not_support_tips);
        mTextView_Not_Support.setText(StringUtil.getString(this,
                                                           R.string.change_app_icon_not_support_icon_tips));
        TextView textView = (TextView) mHeaderView.findViewById(R.id.change_suggestion_text);
        textView.setText(StringUtil.getString(this, R.string.change_app_icon_suggestion));
        textView = (TextView) findViewById(R.id.change_app_icon_title_text);
        textView.setText(StringUtil.getString(this, R.string.change_app_icon_title));
        textView = (TextView) findViewById(R.id.change_app_icon_more_text);
        textView.setText(StringUtil.getString(this, R.string.change_app_icon_more_icons));
        textView = (TextView) findViewById(R.id.ok_text);
        textView.setText(StringUtil.getString(this, R.string.change_app_icon_ok));

        initSpinner();
        // 去掉gridview自带的点击效果
        mGridView_all.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView_suggestion.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mImageView_oldIcon.setImageBitmap(firstIcon);
        appIconDBSearcherApp = new ChangeAppIconDBSearcherApp(this);
        Bitmap bitmap;
        // 对浏览器做特殊处理
        if (BROWSER_NAME.equals(info.title)) {
            mImageView_oldIcon.setImageResource(info.mIconId);
            bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                (String) info.title, 0, 0);
            // 浏览器更换过后的图片如果数据库中存有
            if (bitmap != null) {
                mImageView_newIcon.setImageBitmap(bitmap);
                return;
            }
        } else {
            bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(info
                                                                          .getIntent()
                                                                          .getComponent()
                                                                          .getPackageName()
                                                                      + "/" + info.getIntent()
                                                                          .getComponent()
                                                                          .getClassName(),
                                                                      0,
                                                                      0);
            if (bitmap != null) {
                // 非浏览器更换过后的图片如果数据库中存有
                mImageView_newIcon.setImageBitmap(bitmap);
                return;
            } else if (info.mIconId != -1) {
                // 若是默认配置的六个图标
                mImageView_newIcon.setImageResource(info.mIconId);
                return;
            } else {
                // 以上图片都不是时显示为空
                mImageView_newIcon.setImageDrawable(null);
                return;
            }
        }

    }

    /**
     * 初始化Spinner
     */
    private void initSpinner() {
        adapter = new AppIconSpinnerAdapter(this, otherPackageNames);
        mChoosePackageNameSpinner.setAdapter(adapter);
        mChoosePackageNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                int index = parent.getSelectedItemPosition();
                iconPackageName = itemInfos.get(index).getPackageName();
                if (ICON_PKG_NAME.equals(iconPackageName) && !CommonUtil
                    .isAppInstalled(ChangeAppIcon.this, ICON_PKG_NAME) && ICON_APP_NAME
                        .equals(itemInfos.get(index).getAppName())) {
                    ChangeAppIconHelp
                        .gotoDownloadAPK(ChangeAppIcon.this, ChangeAppIconHelp.CHANGE_APP_PKG_URL);
                    mChoosePackageNameSpinner.setSelection(0);
                    clickNanoUi = true;
                } else {
                    thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            Message message = mHandler.obtainMessage();
                            message.what = SHOW_LOADING;
                            // 发送message显示loading界面
                            mHandler.sendMessage(message);
                            initIconPackageIconsDate();
                            message = mHandler.obtainMessage();
                            message.what = LOAD_OK;
                            // 发送message取消loading界面并告知loading ok
                            mHandler.sendMessage(message);
                        }
                    });
                    thread.start();
                    String itemName = mChoosePackageNameSpinner.getSelectedItem().toString();
                    Preferences sharedPerf = SharedPreferencesUtil.get();
                    if (sharedPerf == null) {
                        SharedPreferencesUtil.init(ChangeAppIcon.this);
                    }
                    sharedPerf.putString("change_spinner", itemName);
                    sharedPerf.flush();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 如果没有安装我们的icon package显示默认的六个icon
     */
    private void addDefaultSixIcon() {
        boolean isHaveOurIconPKG = false;
        for (int j = 0; j < itemInfos.size(); j++) {
            if (itemInfos.get(j).getPackageName().equals(ICON_PKG_NAME) && CommonUtil
                .isAppInstalled(this, ICON_PKG_NAME)) {
                isHaveOurIconPKG = true;
                break;
            }
        }
        if (!isHaveOurIconPKG) {
            String defaultStr = StringUtil.getString(this, R.string.edit_icon_default);
            itemInfos.add(0, new AppItemInfo(ICON_PKG_NAME, defaultStr));
            otherPackageNames.add(0, defaultStr);
        }
    }

    // 解析icon package中的icon信息
    private void initIconPackageIconsDate() {
        try {
            iconBases.clear();
            allIconBases.clear();
            if (!iconPackageName.equals(ICON_PKG_NAME) && suggestionIconBases.size() > 1) {
                for (int j = suggestionIconBases.size() - 1; j >= 1; j--) {
                    suggestionIconBases.remove(j);
                }
            }
            if (iconPackageName.equals(ICON_PKG_NAME)) {
                for (int j = suggestionIconBases.size() - 1; j >= 1; j--) {
                    suggestionIconBases.remove(j);
                }
                if (info.mIconId != 0xffffffff) {
                    IconBase base = new IconBase();
                    base.setImageId(info.mIconId);
                    base.setName((String) info.title);
                    base.setTypeIcon(IconBase.ICON_TYPE_ZEN_SIX_ICON);
                    // base.setContext(this);
                    suggestionIconBases.add(base);
                }
                initDefaultIconsDate();
            }
            mPackageManager = this.getPackageManager();
            slaveContext = this.createPackageContext(iconPackageName,
                                                     Context.CONTEXT_IGNORE_SECURITY);
            InputStream inputStream = null;
            HashMap<String, String> hashMap = null;
            try {
                inputStream = slaveContext.getAssets().open("appfilter.xml");
                hashMap = ParserAppfilterXML(inputStream);
                if (hashMap.containsKey(info.intent.getComponent()
                                            .getPackageName()
                                        + "/"
                                        + info.intent.getComponent().getClassName())) {
                    suggestionDrawableName = hashMap.get(info.intent
                                                             .getComponent().getPackageName()
                                                         + "/"
                                                         + info.intent.getComponent()
                                                             .getClassName());
                    Log.i("XML", "drawableName" + suggestionDrawableName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                inputStream = slaveContext.getAssets().open("drawable.xml");
                List<String> drawableNames = ParseDrawableXML(inputStream);
                Resources res = mPackageManager
                    .getResourcesForApplication(iconPackageName);
                for (String name : drawableNames) {
                    IconBase base = new IconBase();
                    int imageid = res.getIdentifier(name, "drawable",
                                                    iconPackageName);
                    if (imageid == 0X00) {
                        continue;
                    }
                    base.setImageId(imageid);
                    base.setName(name);
                    // base.setContext(slaveContext);
                    base.setTypeIcon(IconBase.ICON_TYPE_OUR_APPLICATION);
                    if (suggestionDrawableName != null
                        && suggestionDrawableName.length() != 0
                        && name.startsWith(suggestionDrawableName)) {
                        suggestionIconBases.add(base);
                    } else {
                        iconBases.add(base);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取默认六个icon
     */
    private void initDefaultIconsDate() {
        String[] defaultAppIcon = getResources().getStringArray(
            R.array.default_app_icon);
        for (int i = 0; i < defaultAppIcon.length; i++) {
            if (mDefualtAppIcons[i] != info.mIconId) {
                IconBase iconBase = new IconBase();
                iconBase.setName(defaultAppIcon[i]);
                iconBase.setImageId(mDefualtAppIcons[i]);
                iconBase.setTypeIcon(IconBase.ICON_TYPE_ZEN_SIX_ICON);
                // iconBase.setContext(this);
                allIconBases.add(iconBase);
            }
        }
    }

    /**
     * loading ok后显示界面
     */
    @SuppressLint("InflateParams")
    private void loadingOkshowView() {
        boolean isRemove = mGridView_all.removeHeaderView(mHeaderView);
        if (isRemove) {
            mHeaderView = LayoutInflater.from(this)
                .inflate(R.layout.chang_app_icon_header_layout, null);
            mGridView_suggestion = (GridView) mHeaderView
                .findViewById(R.id.gv_change_app_icon_suggestion);
            mGridView_suggestion.setSelector(new ColorDrawable(Color.TRANSPARENT));
            TextView textView = (TextView) mHeaderView.findViewById(R.id.change_suggestion_text);
            textView.setText(StringUtil.getString(this, R.string.change_app_icon_suggestion));
        }
        mGridView_suggestion.setAdapter(new AppIconAdapter(this, suggestionIconBases));
        allIconBases.addAll(iconBases);
        AppIconAdapter appIconAdapter = new AppIconAdapter(this, allIconBases);
        setSuggestionmGridviewHight(suggestionIconBases);
        mGridView_all.addHeaderView(mHeaderView);
        mGridView_all.setAdapter(appIconAdapter);
        appIconAdapter.notifyDataSetChanged();
        setAllClickLinsener();
        dialogUtil.cancelLoadingDialog();
        mTextView_Not_Support.setVisibility(View.GONE);
        if (allIconBases.size() == 0) {
            mTextView_Not_Support.setVisibility(View.VISIBLE);
        }
    }

    private void showAlertDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.change_appicon_dialog);
        window.setGravity(Gravity.CENTER_HORIZONTAL);
        mTextView_Dialog_All_Yes = (TextView) window
            .findViewById(R.id.tv_change_app_icon_all_yes);
        mTextView_Dialog_All_No = (TextView) window
            .findViewById(R.id.tv_change_app_icon_all_no);
        mCheckBox_Dialog = (CheckBox) window
            .findViewById(R.id.cb_change_app_icon_checkBox);
        mTextView_Dialog_All_Yes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTargetBitmap != null) {
                    // Launcher.getInstance().getSpeedDial().changeIcon(mTargetBitmap);
                    appIconDBEntity.setIcon(mTargetBitmap);
                    changeAppIconDBUtils.deleteAppIcon(appIconDBEntity,
                                                       change_app_icon_delete_type);
                    changeAppIconDBUtils.insertAppIcon(appIconDBEntity);
                    refreshAll();
                }
                alertDialog.cancel();
                finishAll();
            }
        });
        mTextView_Dialog_All_No.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTargetBitmap != null) {
                    // Launcher.getInstance().getSpeedDial().changeIcon(mTargetBitmap);
                    appIconDBEntity.setIcon(mTargetBitmap);
                    changeAppIconDBUtils.deleteAppIcon(appIconDBEntity,
                                                       change_app_icon_delete_type);
                    changeAppIconDBUtils.insertAppIcon(appIconDBEntity);
                    refreshAll();
                }
                alertDialog.cancel();
                finishAll();
            }
        });
        mCheckBox_Dialog
            .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    Editor editor = preferences.edit();
                    editor.putBoolean("change_app_icon_show_dialog_agin",
                                      isChecked);
                    editor.commit();
                }
            });

    }

    /**
     * 建议gridview设置数据
     */
    private void setSuggestionmGridviewHight(ArrayList<IconBase> bases) {
        int m = bases.size() / mNumcolumns
                + (bases.size() % mNumcolumns == 0 ? 0 : 1);
        ViewGroup.LayoutParams layoutParams = mGridView_suggestion
            .getLayoutParams();
        layoutParams.height = (int) (m * (getResources().getDimension(
            R.dimen.change_app_icon_icon_size) + getResources()
                                              .getDimension(
                                                  R.dimen.change_app_icon_gradview_verticalSpacing)));
        mGridView_suggestion.setLayoutParams(layoutParams);
    }

    /**
     * All Gridview 设置数据 后面使用headerGridview后不再调用此函数
     */
    @SuppressWarnings("unused")
    private void setAllmGridviewHight(ArrayList<IconBase> bases) {
        int m = 24 / mNumcolumns
                + (24 % mNumcolumns == 0 ? 0 : 1);
        ViewGroup.LayoutParams layoutParams = mGridView_all.getLayoutParams();
        layoutParams.height = (int) (m * (getResources().getDimension(
            R.dimen.change_app_icon_icon_size) + getResources()
                                              .getDimension(
                                                  R.dimen.change_app_icon_gradview_verticalSpacing)));
        mGridView_all.setLayoutParams(layoutParams);
    }

    /**
     * 解析icon package中的drawable.xml文件
     *
     * @return List<String> icon的文件名称集合
     * @throws Exception 抛出解析异常
     */
    private List<String> ParseDrawableXML(InputStream inputStream)
        throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "utf-8");
        List<String> drawableNames = new ArrayList<String>();
        String drawableName = null;
        int eventType = parser.getEventType();// 得到第一个事件类型
        while (eventType != XmlPullParser.END_DOCUMENT) {// 如果事件类型不是文档结束的话则不断处理事件
            switch (eventType) {
                case (XmlPullParser.START_DOCUMENT):// 如果是文档开始事件
                    break;
                case (XmlPullParser.START_TAG):// 如果遇到标签开始
                    String tagName = parser.getName();// 获得解析器当前元素的名称
                    if ("item".equals(tagName)) {
                        drawableName = parser.getAttributeValue(0);
                    }
                    break;
                case (XmlPullParser.END_TAG):// 如果遇到标签结束
                    if (drawableName != null && drawableName.length() != 0) {
                        drawableNames.add(drawableName);
                    }
                    drawableName = null;
                    break;
            }
            eventType = parser.next();// 进入下一个事件处理
        }
        return drawableNames;

    }

    /**
     * 解析icon package中的Appfilter.xml文件
     *
     * @return HashMap<String, String> icon的文件名与之对应的package name和文件名的键值对集合
     * @throws Exception 抛出解析异常
     */
    private HashMap<String, String> ParserAppfilterXML(InputStream inputStream)
        throws Exception {
        XmlPullParser parser = Xml.newPullParser();// 得到Pull解析器
        parser.setInput(inputStream, "utf-8");
        HashMap<String, String> hashMap = new HashMap<String, String>();
        String packageName = null;
        String drawableName = null;
        int eventType = parser.getEventType();// 得到第一个事件类型
        while (eventType != XmlPullParser.END_DOCUMENT) {// 如果事件类型不是文档结束的话则不断处理事件
            switch (eventType) {
                case (XmlPullParser.START_DOCUMENT):// 如果是文档开始事件
                    break;
                case (XmlPullParser.START_TAG):// 如果遇到标签开始
                    String tagName = parser.getName();// 获得解析器当前元素的名称
                    if ("item".equals(tagName)) {
                        try {
                            if ((parser.getAttributeValue(0).indexOf("{") + 1) <= parser
                                .getAttributeValue(0).indexOf("}")) {
                                packageName = parser.getAttributeValue(0).substring(
                                    parser.getAttributeValue(0).indexOf("{") + 1,
                                    parser.getAttributeValue(0).indexOf("}"));
                                drawableName = parser.getAttributeValue(1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            drawableName = null;
                        }
                    }
                    break;
                case (XmlPullParser.END_TAG):// 如果遇到标签结束
                    if (packageName != null && packageName.length() != 0
                        && drawableName != null && drawableName.length() != 0) {
                        hashMap.put(packageName, drawableName);
                    }
                    packageName = null;
                    drawableName = null;
                    break;
            }
            eventType = parser.next();// 进入下一个事件处理
        }
        return hashMap;
    }

    /**
     * 刷新所有界面的icon
     */
    private void refreshAll() {
        if (Launcher.getInstance() != null) {
            Launcher.getInstance().getSpeedDial().refresh();
            Launcher.getInstance().refreshFavoriteAndRecent();
            Launcher.getInstance().Allapp.refreshAllApp();
        }
    }

    /**
     * 退出时清空处理
     */
    private void finishAll() {
        if (iconBases != null) {
            iconBases.clear();
            iconBases = null;
        }
        if (allIconBases != null) {
            allIconBases.clear();
            allIconBases = null;
        }
        if (suggestionIconBases != null) {
            suggestionIconBases.clear();
            suggestionIconBases = null;
        }
        mGridView_suggestion = null;
        mGridView_all = null;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSpinner();
    }

    private void updateSpinner() {
        otherPackageNames.clear();
        itemInfos.clear();
        String[] spinnerNames = getResources().getStringArray(R.array.other_package_icon_name);
        for (int i = 0; i < spinnerNames.length; i++) {
            if (ICON_PKG_NAME.equals(spinnerNames[i])) {
                if (CommonUtil.isAppInstalled(this, spinnerNames[i])) {// 若安装了相对应的icon
                    try {
                        ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(
                            spinnerNames[i], 0);
                        // 获取icon Application 的名字
                        String applicationName =
                            (String) this.getPackageManager().getApplicationLabel(appInfo);
                        itemInfos.add(new AppItemInfo(spinnerNames[i], applicationName));
                        otherPackageNames.add(applicationName);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    itemInfos.add(new AppItemInfo(spinnerNames[i], ICON_APP_NAME));
                    otherPackageNames.add(ICON_APP_NAME);
                }
            } else {
                if (CommonUtil.isAppInstalled(this, spinnerNames[i])) {// 若安装了相对应的icon
                    // app
                    try {
                        ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(
                            spinnerNames[i], 0);
                        // 获取icon Application 的名字
                        String applicationName =
                            (String) this.getPackageManager().getApplicationLabel(appInfo);
                        itemInfos.add(new AppItemInfo(spinnerNames[i], applicationName));
                        otherPackageNames.add(applicationName);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        addDefaultSixIcon();
        adapter.notifyDataSetChanged();
        Preferences sharedPerf = SharedPreferencesUtil.get();
        if (sharedPerf == null) {
            SharedPreferencesUtil.init(ChangeAppIcon.this);
        }
        String itemName = sharedPerf.getString("change_spinner", null);
        if (itemName != null) {
            boolean uninstalled = true;
            for (int i = 0; i < otherPackageNames.size(); i++) {
                if (itemName.equals(otherPackageNames.get(i))) {
                    mChoosePackageNameSpinner.setSelection(i);
                    uninstalled = false;
                    break;
                }
            }
            if (uninstalled) {
                mChoosePackageNameSpinner.setSelection(0);
            }
        } else {
            mChoosePackageNameSpinner.setSelection(0);
        }
        if (clickNanoUi && CommonUtil.isAppInstalled(this, ICON_PKG_NAME)) {
            clickNanoUi = false;
            thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Message message = mHandler.obtainMessage();
                    message.what = SHOW_LOADING;
                    // 发送message显示loading界面
                    mHandler.sendMessage(message);
                    initIconPackageIconsDate();
                    message = mHandler.obtainMessage();
                    message.what = LOAD_OK;
                    // 发送message取消loading界面并告知loading ok
                    mHandler.sendMessage(message);
                }
            });
            thread.start();
        }
    }

}
