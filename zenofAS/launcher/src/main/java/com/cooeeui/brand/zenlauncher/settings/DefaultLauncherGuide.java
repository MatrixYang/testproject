package com.cooeeui.brand.zenlauncher.settings;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultLauncherGuide {

    private static final int MESSAGE_SHOW_DELAY_TIME = 800;
    private static final int MESSAGE_HIDE_DELAY_TIME = 10000;
    private static final int HANDLER_LOOP_DELAY_TIME = 200;

    private static final int MESSAGE_SHOW_GUIDE = 0;
    private static final int MESSAGE_HIDE_GUIDE = 1;

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private View mGuideWindow;
    private View mGuideView;
    private ActivityManager mActivityManager;
    private final Handler mHandler = new MyHandler(this);
    private Runnable mRunable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mHandler.postDelayed(this, HANDLER_LOOP_DELAY_TIME);

            String processName;

            processName = mActivityManager.getRunningAppProcesses().get(0).processName;

            if (!"system:ui".equals(processName)) {
                removeGuide();
            }
        }
    };


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

        private final WeakReference<DefaultLauncherGuide> mOuter;

        public MyHandler(DefaultLauncherGuide outer) {
            mOuter = new WeakReference<DefaultLauncherGuide>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            DefaultLauncherGuide outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case MESSAGE_SHOW_GUIDE:
                        Animation animation = AnimationUtils.loadAnimation(mContext,
                                                                           R.anim.default_launcher_guide_in);
                        animation.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {

                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {
                                mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_GUIDE,
                                                                 MESSAGE_HIDE_DELAY_TIME);
                            }
                        });
                        mGuideView.startAnimation(animation);
                        mGuideView.setVisibility(View.VISIBLE);
                        break;
                    case MESSAGE_HIDE_GUIDE:
                        Animation animationOut = AnimationUtils.loadAnimation(mContext,
                                                                              R.anim.default_launcher_guide_out);
                        animationOut.setAnimationListener(new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation arg0) {

                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {

                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {
                                removeGuide();
                            }
                        });
                        mGuideView.startAnimation(animationOut);
                        mGuideView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }

    public DefaultLauncherGuide(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    }

    public void showGuide() {
        if (settingsDefaultLauncherForXiaoMiAndHuaWei(mContext)) {
            return;
        }
        invokeResolverActivity();

        showSettingsDefaultLauncherDialog();
    }

    private void showSettingsDefaultLauncherDialog() {
        if (mHandler != null) {
            mHandler.postDelayed(mRunable, HANDLER_LOOP_DELAY_TIME);
        }

        if (mGuideWindow == null) {
            int width = DeviceUtils.getScreenPixelsWidth(mContext);
            int height = DeviceUtils.getScreenPixelsHeight(mContext);
            if (width > height) {
                width = height;
            }
            mParams = new WindowManager.LayoutParams();
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mParams.format = PixelFormat.RGBA_8888;
            mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            mParams.width = width;
            mParams.height = LayoutParams.WRAP_CONTENT;
            mParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            mParams.x = 0;
            mParams.y = 0;

            mGuideWindow =
                LayoutInflater.from(mContext).inflate(R.layout.default_launcher_guide,
                                                      null);
            mGuideView =
                mGuideWindow.findViewById(R.id.default_launcher_guide_view);
            mWindowManager.addView(mGuideWindow, mParams);
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_GUIDE,
                                             MESSAGE_SHOW_DELAY_TIME);

            TextView textView = (TextView) mGuideWindow.findViewById(R.id.guide_step1);
            String text = StringUtil.getString(mContext, R.string.default_launcher_guide_step1);
            textView.setText(text);

            textView = (TextView) mGuideWindow.findViewById(R.id.guide_step2);
            text = StringUtil.getString(mContext, R.string.default_launcher_guide_step2);
            textView.setText(text);

        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_GUIDE,
                                             MESSAGE_SHOW_DELAY_TIME);
        }
        mGuideWindow.invalidate();
        mWindowManager.updateViewLayout(mGuideWindow, mParams);
    }

    public void removeGuide() {
        if (mGuideWindow != null) {
            mWindowManager.removeView(mGuideWindow);
            mGuideWindow = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mRunable);
            mHandler.removeMessages(MESSAGE_SHOW_GUIDE);
            mHandler.removeMessages(MESSAGE_HIDE_GUIDE);
        }
    }

    public ResolveInfo getCurDefaultLauncher() {
        ResolveInfo curInfo = null;
        PackageManager localPackageManager = mContext.getPackageManager();
        Intent localIntent = new Intent(Intent.ACTION_MAIN);
        localIntent.addCategory(Intent.CATEGORY_HOME);
        List localList = localPackageManager.queryIntentActivities(localIntent, 0);
        ArrayList localArrayList1 = new ArrayList();
        ArrayList localArrayList2 = new ArrayList();
        Iterator localIterator1 = localList.iterator();
        Iterator localIterator2;
        IntentFilter localIntentFilter;

        while (localIterator1.hasNext()) {
            curInfo = (ResolveInfo) localIterator1.next();
            localArrayList1.clear();
            localArrayList2.clear();
            localPackageManager.getPreferredActivities(localArrayList1, localArrayList2,
                                                       curInfo.activityInfo.packageName);
            localIterator2 = localArrayList1.iterator();

            while (localIterator2.hasNext()) {
                localIntentFilter = (IntentFilter) localIterator2.next();
                if (localIntentFilter.hasAction(Intent.ACTION_MAIN)
                    && localIntentFilter.hasCategory(Intent.CATEGORY_HOME)) {
                    return curInfo;
                }
            }
        }

        return null;
    }

    public boolean isDefaultLauncher() {
        ResolveInfo resolveInfo = getCurDefaultLauncher();
        if (resolveInfo != null) {
            String str = resolveInfo.activityInfo.applicationInfo.packageName;
            Launcher instance = Launcher.getInstance();
            if (instance == null) {
                return true;
            }
            if (str.equals(instance.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    public void clearPreDefaultSetting() {
        ResolveInfo resolveInfo = getCurDefaultLauncher();
        if (resolveInfo != null) {
            String str = resolveInfo.activityInfo.applicationInfo.packageName;
            if (!str.equals(Launcher.getInstance().getPackageName())) {
                PackageManager p = mContext.getPackageManager();
                ComponentName cn = new ComponentName(Launcher.getInstance().getPackageName(),
                                                     DefaultLauncher.class.getName());
                p.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                             PackageManager.DONT_KILL_APP);
                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.addCategory(Intent.CATEGORY_DEFAULT);
                p.resolveActivity(selector, PackageManager.GET_RESOLVED_FILTER);
                p.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                             PackageManager.DONT_KILL_APP);
            }
        }
    }

    // 打开系统桌面选择器
    private void invokeResolverActivity() {
        clearPreDefaultSetting();
        PackageManager p = mContext.getPackageManager();
        ComponentName cn = new ComponentName(mContext, DefaultLauncher.class);
        p.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                     PackageManager.DONT_KILL_APP);
        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mContext.startActivity(selector);
        p.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                                     PackageManager.DONT_KILL_APP);
    }

    /**
     * 对华为和小米设置默认桌面进行特殊处理
     *
     * @param context 上下文对象
     * @return <pre>当是华为和小米手机时
     *      返回 true
     *      当不是华为和小米手机时
     *      返回 false
     * </pre>
     */
    public boolean settingsDefaultLauncherForXiaoMiAndHuaWei(Context context) {
        boolean isSettingsSuccess = false;
        try {
            //这种方式是solo和go桌面处理方式即打开settings界面！
            //com.android.settings/.applications.PreferredListSettings
            //这种方式是Hola处理方式即直接打开选择默认桌面选择器！
            // START {act=android.intent.action.MAIN cat=[android.intent.category.HOME] cmp=android/com.android.internal.app.ResolverActivity (has extras) u=0}
            if (DeviceUtils.isXiaomiDevices()) {
                isSettingsSuccess = true;
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                intent.setComponent(
                    new ComponentName("android", "com.android.internal.app.ResolverActivity"));
                intent.getIntExtra("u", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }//com.android.settings.Settings$PreferredListSettingsActivity
            if (DeviceUtils.isHuaWeiDevices()) {
                isSettingsSuccess = true;
                Intent localIntent1 = new Intent("android.intent.action.MAIN");
                Intent
                    localIntent2 =
                    localIntent1.setClassName("com.android.settings",
                                              "com.android.settings.Settings$PreferredListSettingsActivity");
                localIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(localIntent2);
            }
        } catch (Exception e) {
            Log.e("zen launcher", "Default launcher Settings error!");
        }
        return isSettingsSuccess;

    }
}
