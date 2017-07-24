package com.cooeeui.brand.zenlauncher.scenes;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.android.view.QsListViewLetters;
import com.cooeeui.brand.zenlauncher.android.view.QsListViewLetters.OnTouchingLetterChangedListener;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.changeicon.ChangeAppIcon;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBEntity;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBHelp;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBSearcherApp;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.brand.zenlauncher.tips.TipsSetting;
import com.cooeeui.brand.zenlauncher.utils.FirstLetterMapper;
import com.cooeeui.brand.zenlauncher.wallpaper.OnlineWallpaperActivity;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.popujar.PopuItem;
import com.cooeeui.zenlauncher.common.ui.popujar.PopuJar;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.out.MvWallHandler;
import com.umeng.analytics.MobclickAgent;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Allapps extends ViewGroup implements View.OnClickListener {

    private static final int APP_DETAILS = 1;
    private static final int APP_ADD_WORKSPACE = 2;
    private static final int APP_UNINSTALL = 3;
    private static final int APP_CHANGE_APP_ICON = 4;
    public PopuJar mPopu = null;
    public boolean isEdit = false;
    public PopuJar popujarTemp;
    ChangeAppIconDBSearcherApp appIconDBSearcherApp;
    private ArrayList<AppInfo> mApps = null;
    private ArrayList<AppInfo> hidens = new ArrayList<AppInfo>();
    private Launcher mLauncher;
    private QsListViewAdapter mListViewAdapter = null;
    private AllappsListview mListview = null;
    private QsListViewLetters mlistviewLetters = null;
    private ArrayList<ArrayList<Object>> mListDatas = new ArrayList<ArrayList<Object>>();
    private ArrayList<String> mOrderedSection = new ArrayList<String>();// 作为mListDatas的一维索引
    private AppInfo mSelectedInfo;
    private View mSelectedView;
    private LinearLayout mLinearLayoutHide;
    private ViewGroup relaytiveLayoutHotApp;
    private final Handler mHandler = new Handler();
    private MvWallHandler mvHandler;

    private ImageView mImageViewGameCenter;

    public Allapps(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(Launcher launcher) {
        this.mLauncher = launcher;
        initAllappsPop();
        appIconDBSearcherApp = new ChangeAppIconDBSearcherApp(mLauncher);
    }

    public void initAllappsPop() {
        PopuItem homeItem = new PopuItem(APP_ADD_WORKSPACE,
                                         StringUtil.getString(mLauncher,
                                                              R.string.pop_add_speeddial),
                                         getResources().getDrawable(R.drawable.add_homescreen));
        PopuItem infoItem = new PopuItem(APP_DETAILS,
                                         StringUtil.getString(mLauncher, R.string.pop_app_info),
                                         getResources().getDrawable(R.drawable.app_info));
        PopuItem unistallAppItem = new PopuItem(APP_UNINSTALL,
                                                StringUtil.getString(mLauncher, R.string.unload),
                                                getResources()
                                                    .getDrawable(R.drawable.app_uninstall));
        PopuItem changeAppIcon = new PopuItem(APP_CHANGE_APP_ICON,
                                              StringUtil.getString(mLauncher,
                                                                   R.string.pop_change_app_icon),
                                              getResources().getDrawable(
                                                  R.drawable.change_app_icon_popupwindow));
        mPopu = new PopuJar(mLauncher, PopuJar.VERTICAL);
        mPopu.addPopuItem(homeItem, PopuJar.APP_TYPE_NORMAL);
        mPopu.addPopuItem(changeAppIcon, PopuJar.APP_TYPE_NORMAL);
        mPopu.addPopuItem(infoItem, PopuJar.APP_TYPE_NORMAL);
        mPopu.addPopuItem(unistallAppItem, PopuJar.APP_TYPE_SYS);
        mPopu.setAnimStyle(PopuJar.ANIM_AUTO);
        mPopu.setOnPopuItemClickListener(new PopuJar.OnPopuItemClickListener() {
            @Override
            public void onItemClick(PopuJar source, int pos, int actionId) {
                PopuItem PopuItem = mPopu.getPopuItem(pos);
                if (actionId == APP_DETAILS) {
                    exitOnLongClickChangeAlpha();
                    mLauncher.startApplicationDetailsActivity(mSelectedInfo.componentName);
                    // all app中app info点击次数
                    MobclickAgent.onEvent(mLauncher, "AllAppClcikAppInfo");
                } else if (actionId == APP_ADD_WORKSPACE) {

                    // all app中home screen点击次数

                    if (!mLauncher.getSpeedDial().isFull()) {
                        mLauncher.getSpeedDial().addBubbleView(mSelectedInfo.makeShortcut());
                        mLauncher.getSpeedDial().update();
                        Toast.makeText(mLauncher,
                                       StringUtil.getString(mLauncher, R.string.pop_add_success),
                                       Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mLauncher,
                                       StringUtil.getString(mLauncher, R.string.pop_add_fail),
                                       Toast.LENGTH_SHORT).show();
                    }
                    exitOnLongClickChangeAlpha();
                } else if (actionId == APP_UNINSTALL) {
                    mLauncher.startApplicationUninstallActivity(mSelectedInfo.componentName,
                                                                mSelectedInfo.flags);
                    exitOnLongClickChangeAlpha();
                } else if (actionId == APP_CHANGE_APP_ICON) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ChangeAppIconDBHelp.COLUMN_NAME_ICON_CHANGE_TYPE,
                                  ChangeAppIconDBEntity.ICON_CHANGE_TYPE_ALL_APPS_PAGE);
                    Intent intent2 = new Intent(mLauncher, ChangeAppIcon.class);
                    intent2.putExtras(bundle);
                    mLauncher.startActivity(intent2);
                    exitOnLongClickChangeAlpha();

                } else {
                    Toast.makeText(mLauncher.getApplicationContext(),
                                   PopuItem.getTitle() + " selected",
                                   Toast.LENGTH_SHORT).show();
                    exitOnLongClickChangeAlpha();
                }
            }
        });

        mPopu.setOnDismissListener(new PopuJar.OnDismissListener() {
            @Override
            public void onDismiss() {
                exitOnLongClickChangeAlpha();
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (Build.VERSION.SDK_INT >= 19) {
            LinearLayout mainLayout = (LinearLayout) findViewById(R.id.ll_all_apps_layout);
            int statusbarHeight = 0;
            statusbarHeight = DeviceUtils.getStatusBarHeight(Launcher.getInstance());
            mainLayout.setPadding(mainLayout.getPaddingLeft(), statusbarHeight,
                                  mainLayout.getPaddingRight(), mainLayout.getPaddingBottom());
        }

        mListview = (AllappsListview) findViewById(R.id.qs_listview);
        mLinearLayoutHide = (LinearLayout) findViewById(R.id.linearlayoutHide);
        mLinearLayoutHide.setOnClickListener(this);

        mImageViewGameCenter = (ImageView) findViewById(R.id.im_game_certer);
        mImageViewGameCenter.setOnClickListener(this);

        mlistviewLetters = (QsListViewLetters) findViewById(R.id.qs_app_letter_listview);
        mListview.setup(mlistviewLetters);

        //MobvistaSDK *begin*//
        initHotApp();
        //MobvistaSDK *end*//

        if (LauncherPreference.getAllAppStoreAlert()) {
            ((ImageView) findViewById(R.id.img_appstore_alert)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) findViewById(R.id.img_appstore_alert)).setVisibility(View.GONE);
        }
        if (mApps != null && mApps.size() > 0) {
            initialLayout();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void initHotApp() {
        relaytiveLayoutHotApp = (ViewGroup) findViewById(R.id.all_app_hot_app_parents);

        loadHandler();
    }

    private void hotAppOnClick() {
        LauncherPreference.setAllAppStoreAlert(false);
        //all app广告点击次数
        MobclickAgent.onEvent(mLauncher, "Adinallappclick");
        ((ImageView) findViewById(R.id.img_appstore_alert))
            .setVisibility(View.GONE);
    }

    private void loadHandler() {
        ViewGroup viewGroup = new RelativeLayout(Launcher.getInstance()) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hotAppOnClick();
                }
                return super.onTouchEvent(event);
            }
        };
        relaytiveLayoutHotApp.addView(viewGroup);

        Map<String, Object> properties = MvWallHandler.getWallProperties("216");
        properties.put(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_ID,
                       R.drawable.mobvista_wall_hot_app_img_logo);
        mvHandler = new MvWallHandler(properties, Launcher.getInstance(), viewGroup);

        //customer entry layout begin
        View view =
            LayoutInflater.from(Launcher.getInstance())
                .inflate(R.layout.customer_entry_allapp, null);
        view.findViewById(R.id.all_app_hot_app).setTag(
            MobVistaConstans.WALL_ENTRY_ID_IMAGEVIEW_IMAGE);
//        view.findViewById(R.id.newtip_area)
//            .setTag(MobVistaConstans.WALL_ENTRY_ID_VIEWGROUP_NEWTIP);

        view.findViewById(R.id.all_app_hot_app).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.all_app_hot_app) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        LauncherPreference.setAllAppStoreAlert(false);
                        //all app广告点击次数
                        MobclickAgent.onEvent(mLauncher, "Adinallappclick");
                        ((ImageView) findViewById(R.id.img_appstore_alert))
                            .setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        mvHandler.setHandlerCustomerLayout(view);
        //customer entry layout end */
        mvHandler.load();
    }

    public View getMiddleView() {
        View view = null;

        if (mListview.getChildCount() > 0) {
            int f = mListview.getFirstVisiblePosition();
            int l = mListview.getLastVisiblePosition();
            View listItem = mListview.getChildAt((l - f) / 2);
            GridView grid = (GridView) listItem.findViewById(R.id.listview_item_gridview);
            View gridItem = grid.getChildAt(0);
            view = gridItem.findViewById(R.id.gridview_application_icon);
        }

        return view;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mLauncher.mGuiding) {
            return true;
        }

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (isPopup()) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        // 当all app以侧边栏状态显示时，避免触摸事件穿透问题
        if (getScrollX() != 0) {
            final int action = event.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    final float x = event.getX();
                    if (x > 0 && x < mListview.getPaddingLeft()) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int startLeft = getWidth();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child.getVisibility() != View.GONE) {
                child.layout(startLeft, 0,
                             startLeft + getWidth(),
                             getHeight());
            }

            startLeft += getWidth();
        }
    }

    public boolean isPopup() {
        if (mPopu == null) {
            return false;
        }
        return mPopu.isShow();
    }

    public boolean isShowedAll() {
        return getScrollX() == getWidth();
    }

    private void exitOnLongClickChangeAlpha() {
        setViewAlpha(mSelectedView, false);
        mPopu.recoveryUninstallView();
    }

    public boolean dismissPopup() {
        if (mPopu != null && mPopu.isShow()) {
            mPopu.dismiss();
            return true;
        }
        return false;
    }

    private ArrayList<AppInfo> getCopyList(ArrayList<AppInfo> list) {
        ArrayList<AppInfo> arraylist = new ArrayList<AppInfo>();
        for (AppInfo info : list) {
            arraylist.add(info);
        }

        return arraylist;
    }

    public int getIndexForSection(String letter) {
        int pos = -1;
        for (int i = 0; i < mOrderedSection.size(); i++) {
            if (mOrderedSection.get(i).equals(letter)) {
                pos = i;
                return pos;
            }
        }
        return pos;
    }

    public void bindAllApplications(ArrayList<AppInfo> apps) {
        mApps = getCopyList(apps);

        if (mApps != null && mApps.size() > 0) {
            initDatas();
            initialLayout();
        }
    }

    public void bindHideApps(ArrayList<AppInfo> apps) {
        hidens = getCopyList(apps);
        ArrayList<String> packageNames = new ArrayList<String>();
        for (AppInfo appInfo : hidens) {
            packageNames.add(appInfo.componentName.getPackageName());
        }

        // 更新常用
        mLauncher.updateFavorite();
        isEdit = false;
        // 移除常用，桌面和最近安装，该方法中会更新AllApp的列表，实现刷新
        mLauncher.onHideAppChanged(hidens, true);
    }

    public void initialLayout() {
        if (mListview == null) {
            return;
        }

        if (mListViewAdapter == null) {
            mListViewAdapter = new QsListViewAdapter(mLauncher, mOrderedSection, mListDatas);
            mListview.setAdapter(mListViewAdapter);

            mlistviewLetters.setOnTouchingLetterChangedListener(
                new OnTouchingLetterChangedListener() {
                    @Override
                    public void onTouchingLetterChanged(
                        String s) {

                        int pos = getIndexForSection(s);
                        if (pos != -1) {
                            mListview.setSelection(pos);
                        }
                    }
                });
            if (mOrderedSection.size() > 0) {
                mlistviewLetters.setLetters(mOrderedSection, mLauncher.getResources()
                    .getInteger(R.integer.all_apps_letter_size));
                mlistviewLetters.invalidate();
            }
        } else {
            mListViewAdapter.notifyDataSetChanged();
            mlistviewLetters.invalidate();
        }
    }

    public void refreshAllApp() {
        appIconDBSearcherApp = new ChangeAppIconDBSearcherApp(mLauncher);
        for (int i = 0; i < mApps.size(); i++) {
            Bitmap bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                mApps.get(i).componentName.getPackageName() + "/"
                + mApps.get(i).componentName.getClassName(), 0, 0);
            if (bitmap != null) {
                mApps.get(i).iconBitmap = bitmap;
            }
        }
        if (mListViewAdapter != null && mlistviewLetters != null) {
            mListViewAdapter.notifyDataSetChanged();
            mlistviewLetters.invalidate();
        }
    }

    /**
     * 增加或者删除Apps的数据
     *
     * @param apps 变化的数据集合
     */
    public void notifyDataSetChanged(ArrayList<AppInfo> apps) {
        if (mApps == null || apps == null || apps.size() <= 0) {
            return;
        }
        ArrayList<AppInfo> remainList = getCopyList(apps);
        if (remainList.size() <= 0) {
            return;
        }

        ArrayList<AppInfo> added = new ArrayList<AppInfo>();
        ArrayList<AppInfo> removed = new ArrayList<AppInfo>();
        boolean needToRemove = false;
        AppInfo appInfo = null;
        for (AppInfo info : remainList) {
            needToRemove = false;
            for (int i = 0; i < mApps.size(); i++) {
                appInfo = mApps.get(i);
                if (appInfo.componentName.equals(info.componentName)) {
                    needToRemove = true;
                    break;
                }
            }

            if (needToRemove) {
                removed.add(appInfo);
                mApps.remove(appInfo);
            } else {
                added.add(info);
                mApps.add(info);
            }
        }
        String title;
        String firstLetter = "#";
        String UpperCaseLetter;
        int scetionIndex;
        ArrayList<Object> gridDatas;
        ListDatasSortComparator comparator = new ListDatasSortComparator();
        for (AppInfo info : added) {
            title = info.title.toString();
            firstLetter = FirstLetterMapper.getFirstLetter(title);
            if (firstLetter != null) {
                UpperCaseLetter = firstLetter.toUpperCase();
                if (mOrderedSection.contains(UpperCaseLetter)) {
                    scetionIndex = mOrderedSection.indexOf(UpperCaseLetter);
                    gridDatas = mListDatas.get(scetionIndex);
                    gridDatas.add(info);
                    Collections.sort(gridDatas, comparator);
                } else {
                    mOrderedSection.add(UpperCaseLetter);
                    Collections.sort(mOrderedSection);
                    scetionIndex = mOrderedSection.indexOf(UpperCaseLetter);
                    gridDatas = new ArrayList<Object>();
                    gridDatas.add(info);
                    Collections.sort(gridDatas, comparator);
                    mListDatas.add(scetionIndex, gridDatas);
                }
            }
        }
        for (AppInfo info : removed) {
            title = info.title.toString();
            firstLetter = FirstLetterMapper.getFirstLetter(title);
            if (firstLetter != null) {
                UpperCaseLetter = firstLetter.toUpperCase();
                if (mOrderedSection.contains(UpperCaseLetter)) {
                    scetionIndex = mOrderedSection.indexOf(UpperCaseLetter);
                    gridDatas = mListDatas.get(scetionIndex);
                    gridDatas.remove(info);
                    if (gridDatas.size() <= 0) {
                        mListDatas.remove(gridDatas);
                        mOrderedSection.remove(UpperCaseLetter);
                    }
                }
            }
        }
        mListViewAdapter.notifyDataSetChanged();
        mlistviewLetters.invalidate();
    }

    private void initDatas() {
        String title;
        String firstLetter;
        String UpperCaseLetter;
        HashMap<String, ArrayList<Object>> listDatasMap = new HashMap<String, ArrayList<Object>>();
        ArrayList<Object> listDatas;
        for (int i = 0; i < mApps.size(); i++) {
            Bitmap bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                mApps.get(i).componentName.getPackageName() + "/"
                + mApps.get(i).componentName.getClassName(), 0, 0);
            if (bitmap != null) {
                mApps.get(i).iconBitmap = bitmap;
            }
        }
        for (AppInfo info : mApps) {
            title = info.title.toString();
            firstLetter = FirstLetterMapper.getFirstLetter(title);
            if (firstLetter != null) {
                UpperCaseLetter = firstLetter.toUpperCase();
                if (listDatasMap.containsKey(UpperCaseLetter)) {
                    listDatas = listDatasMap.get(UpperCaseLetter);
                    listDatas.add(info);
                } else {
                    listDatas = new ArrayList<Object>();
                    listDatas.add(info);
                    listDatasMap.put(UpperCaseLetter, listDatas);
                }
            }
        }
        mOrderedSection.clear();
        mOrderedSection.addAll(listDatasMap.keySet());
        Collections.sort(mOrderedSection);
        ListDatasSortComparator comparator = new ListDatasSortComparator();
        mListDatas.clear();
        for (int i = 0; i < mOrderedSection.size(); i++) {
            listDatas = listDatasMap.get(mOrderedSection.get(i));
            Collections.sort(listDatas, comparator);
            mListDatas.add(listDatas);
        }
    }

    public void OnLongClickForGridItem(View view, AppInfo info) {
        if (isEdit) {
            return;
        }
        if (mPopu == null) {
            return;
        }
        mSelectedView = view;
        mSelectedInfo = info;
        setViewAlpha(view, true);
        if (info.flags == 0) {
            mPopu.hideUninstallView();
        }
        mPopu.show(view);
        UIEffectTools.onLongClickEffect(view);
    }

    /**
     * Set alpha value for each layout, in addition to the view outside
     *
     * @param view That view is not set transparency
     */
    private void setViewAlpha(View view, boolean alphaOut) {
        PropertyValuesHolder alphaValuesHolder;
        if (alphaOut) {
            alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", 1f,
                                                             0.2f);
        } else {
            alphaValuesHolder = PropertyValuesHolder.ofFloat("alpha", 0.2f,
                                                             1f);
        }

        ViewParent viewParent = view.getParent();

        if (viewParent == null || viewParent.getParent() == null) {
            return;
        }

        ViewParent viewParent2 = viewParent.getParent().getParent();
        if (viewParent2 != null && viewParent2 instanceof ListView) {
            ListView listView = (ListView) viewParent2;
            for (int i = 0; i < listView.getCount() && listView.getChildAt(i) != null; i++) {
                LinearLayout layout = (LinearLayout) (listView.getChildAt(i));
                for (int k = 0; k < layout.getChildCount(); k++) {
                    View child = layout.getChildAt(k);
                    if (child instanceof GridView) {
                        GridView gridView = (GridView) child;
                        for (int j = 0; j < gridView.getChildCount(); j++) {
                            if (!view.equals(gridView.getChildAt(j))) {
                                ObjectAnimator
                                    .ofPropertyValuesHolder(gridView.getChildAt(j),
                                                            alphaValuesHolder).setDuration(100)
                                    .start();
                            }
                        }
                    }
                }
            }
        }
    }

    // 点击按钮响应
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.linearlayoutHide) {
            final TextView icon = (TextView) v.findViewById(R.id.textViewHideIcon);
            if (!isEdit) {
                final PopuJar p = new PopuJar(mLauncher, PopuJar.VERTICAL);
                popujarTemp = p;
                View overMenuView = LayoutInflater.from(mLauncher)
                    .inflate(R.layout.popup_allapp_overmenu, null);
                OnClickListener l = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.button_popuphideapp:
                                isEdit = true;
                                // 将被隐藏的数据添加进所有列表
                                notifyDataSetChanged(hidens);
                                // 没有隐藏应用，手动更新listview
                                if (hidens.size() == 0) {
                                    mListViewAdapter.notifyDataSetChanged();
                                }
                                icon.setBackgroundResource(R.drawable.gou);
                                p.dismiss();
                                break;
                            case R.id.buttonPopupReminder:
                                p.dismiss();
                                Intent intentTipRemind = new Intent(mLauncher, TipsSetting.class);
                                mLauncher.startActivity(intentTipRemind);
                                break;
                            case R.id.buttonPopupWallpaper:
                                p.dismiss();
                                Intent
                                    intentWallpaper =
                                    new Intent(mLauncher, OnlineWallpaperActivity.class);
                                mLauncher.startActivity(intentWallpaper);
                                break;
                            case R.id.buttonPopupSetting:
                                p.dismiss();
                                Intent intentZenSetting = new Intent(mLauncher, ZenSetting.class);
                                mLauncher.startActivity(intentZenSetting);
                                // 进入Nano桌面设置的总次数
                                MobclickAgent.onEvent(mLauncher, "Clickintonanosetting");
                                break;
                            default:
                                break;
                        }
                    }
                };
                overMenuView.findViewById(R.id.button_popuphideapp).setOnClickListener(l);
                overMenuView.findViewById(R.id.buttonPopupReminder).setOnClickListener(l);
                overMenuView.findViewById(R.id.buttonPopupSetting).setOnClickListener(l);
                overMenuView.findViewById(R.id.buttonPopupWallpaper).setOnClickListener(l);
                TextView textView = (TextView) overMenuView.findViewById(R.id.hide_app_text);
                String text = StringUtil.getString(mLauncher, R.string.hideicon);
                textView.setText(text);
                textView = (TextView) overMenuView.findViewById(R.id.hide_tips_text);
                text = StringUtil.getString(mLauncher, R.string.tips_setting);
                textView.setText(text);
                textView = (TextView) overMenuView.findViewById(R.id.hide_wallpaper_text);
                text = StringUtil.getString(mLauncher, R.string.wallpaper);
                textView.setText(text);
                textView = (TextView) overMenuView.findViewById(R.id.hide_zen_text);
                text = StringUtil.getString(mLauncher, R.string.zen_settings);
                textView.setText(text);
                p.setContentView(overMenuView);
                p.setAnimStyle(PopuJar.ANIM_AUTO);
                p.show(v);
            } else {
                onHideApps();
            }
        } else if (v.getId() == R.id.all_app_hot_app) {
//            Intent intentHotApp = new Intent(mLauncher, AppStore.class);
            LauncherPreference.setAllAppStoreAlert(false);
            //all app广告点击次数
            MobclickAgent.onEvent(mLauncher, "Adinallappclick");
            //MobclickAgent.onEvent(mLauncher, "AllAppIntoAppStore");
            ((ImageView) findViewById(R.id.img_appstore_alert)).setVisibility(View.GONE);
//            mLauncher.startActivity(intentHotApp);
        } else if (v.getId() == R.id.im_game_certer) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
//                    Uri content_url = Uri.parse("http://www.rayjump.com/?s=40e160e33b24500df5d2bd7bf9f32d10&appId=21829");
//                    Uri content_url = Uri.parse("http://app.ht5game.com/game/cooee/index.html");
            Uri content_url = Uri.parse("http://www.coolauncher.cn/gamecenter");
            intent.setData(content_url);
            try {
                mLauncher.startActivity(intent);
            }catch (ActivityNotFoundException e){
                Toast.makeText(mLauncher,
                               StringUtil.getString(mLauncher, R.string.activity_not_found),
                               Toast.LENGTH_SHORT).show();
            }
            //游戏点击次数
            MobclickAgent.onEvent(mLauncher, "Gameclick");
        }
    }

    public void onHideApps() {
        findViewById(R.id.textViewHideIcon).setBackgroundResource(R.drawable.menu_moreoverflow);
        ArrayList<AppInfo> changedApps = getChangedApps();
        // 更新变化APP至常用中的内存状态
        mLauncher.onHideAppChanged(changedApps, false);
        // 更新变化APP的状态至数据库
        LauncherModel.saveFavoritesToDatabase(mLauncher, changedApps);
        hidens = new ArrayList<AppInfo>();
        ArrayList<String> packageNames = new ArrayList<String>();
        for (AppInfo appInfo : mApps) {
            if (appInfo.hide) {
                appInfo.launchTimes = 0;
                hidens.add(appInfo);
                packageNames.add(appInfo.componentName.getPackageName());
            }
        }
        // 保存隐藏的APP至数据库
        LauncherModel.saveFavoritesToDatabase(mLauncher, hidens);
        isEdit = false;
        mLauncher.onHideAppChanged(hidens, true);
        if (hidens.size() == 0) {
            mListViewAdapter.notifyDataSetChanged();
        }
    }

    // 过滤出隐藏->显示的APP，用于更新数据库中的状态，同时也更新常用中的内存状态
    private ArrayList<AppInfo> getChangedApps() {
        ArrayList<AppInfo> changed = new ArrayList<AppInfo>();
        for (AppInfo appInfo : mApps) {
            if (appInfo.hide && !appInfo.hideTemp) {
                changed.add(appInfo);
            }
            appInfo.hide = appInfo.hideTemp;
        }
        return changed;
    }

    public void hideApp(ArrayList<AppInfo> apps) {
        if (hidens != null && hidens.size() > 0) {
            for (int i = apps.size() - 1; i >= 0; i--) {
                AppInfo add = apps.get(i);
                for (AppInfo hide : hidens) {
                    if (hide.componentName.equals(add.componentName)) {
                        add.hide = true;
                        add.hideTemp = true;
                        apps.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public void removeAppInfo(ArrayList<AppInfo> removes) {
        hidens.removeAll(removes);
    }

    public AppInfo getmSelectedInfo() {
        return mSelectedInfo;
    }

    class ListDatasSortComparator implements Comparator<Object> {

        @Override
        public int compare(Object lhs, Object rhs) {
            // TODO Auto-generated method stub
            AppInfo a = (AppInfo) lhs;
            AppInfo b = (AppInfo) rhs;
            int result = Collator.getInstance().compare(a.title.toString().trim(),
                                                        b.title.toString().trim());
            if (result == 0) {
                result = a.componentName.compareTo(b.componentName);
            }
            return result;
        }
    }
}
