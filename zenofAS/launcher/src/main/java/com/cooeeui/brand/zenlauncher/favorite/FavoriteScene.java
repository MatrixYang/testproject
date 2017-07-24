package com.cooeeui.brand.zenlauncher.favorite;

import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cooeeui.basecore.uieffects.stackblur.BlurHelper;
import com.cooeeui.basecore.uieffects.stackblur.BlurHelper.BlurCallbacks;
import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.brand.zenlauncher.scenes.MyDeviceManager;
import com.cooeeui.zenlauncher.R;

import java.lang.ref.WeakReference;

/**
 * @author added by Hugo.ye
 */
public class FavoriteScene extends RelativeLayout implements BlurCallbacks, View.OnClickListener {

    private Context mContext;
    /**
     * favorite 界面真实容器
     */
    public RelativeLayout container;
    /**
     * brightness control 界面真实容器
     */
    private FrameLayout mFrameLayoutBrightGroup;

    /**
     * 模糊相关字段
     */
    private BlurHelper mBlurHelper;
    public ImageView bluredView;

    private final Handler mHandler = new MyHandler(this);

    private long firstClickTime = 0;
    private long secondClickTime = 0;

    /**
     * @param context
     */
    public FavoriteScene(Context context) {
        super(context);
        mContext = context;
        mBlurHelper = new BlurHelper(Launcher.getInstance(), this);
        setOnClickListener(this);
    }

    /**
     * @param context
     * @param attrs
     */
    public FavoriteScene(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mBlurHelper = new BlurHelper(Launcher.getInstance(), this);
        setOnClickListener(this);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public FavoriteScene(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mBlurHelper = new BlurHelper(Launcher.getInstance(), this);
        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        container = (RelativeLayout) findViewById(R.id.rl_favorite_container);
        mFrameLayoutBrightGroup = (FrameLayout) findViewById(R.id.frameLayoutBrightGroup);
        int statusbarHeight = 0;
        int navigationHeight = 0;
        if (Build.VERSION.SDK_INT >= 19) {
            statusbarHeight =
                DeviceUtils.getStatusBarHeight(Launcher.getInstance());
            if (!DeviceUtils.isSpecialDevicesForNavigationbar()
                && LauncherAppState.HasNavigationBar(Launcher.getInstance())) {
                navigationHeight =
                    getResources().getDimensionPixelSize(R.dimen.navigation_height);
            }
            container.setPadding(container.getPaddingLeft(), statusbarHeight,
                                 container.getPaddingRight(), navigationHeight);
            mFrameLayoutBrightGroup.setPadding(mFrameLayoutBrightGroup.getPaddingLeft(),
                                               statusbarHeight,
                                               mFrameLayoutBrightGroup.getPaddingRight(),
                                               navigationHeight);
        }

        bluredView = (ImageView) findViewById(R.id.iv_blured_view);

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        ViewParent parent = container.getParent();
        View linear = ((View) parent).findViewById(R.id.LinearLayout_bright);
        linear.setVisibility(View.VISIBLE);
        View speedContainer = ((View)
                                   parent).findViewById(R.id.speedy_container);
        int Y = (int) speedContainer.getY();
        int height = speedContainer.getHeight();

    }

    /**
     * 模糊favorite整体界面，在非UI线程里执行，模糊后的bitmap会通过回调接口'blurCompleted'返回
     */
    public void blurFavoriteScene() {
        if (isBlurEnable()) {
            if (Build.VERSION.SDK_INT >= 16) {
                bluredView.setBackground(null);
            } else {
                bluredView.setBackgroundDrawable(null);
            }
            mBlurHelper.blurWallpaperNonUiThread();
        }
    }

    private boolean isLiveWallpaper() {
        boolean rst = false;

        WallpaperManager wpm = WallpaperManager.getInstance(mContext);
        if (wpm.getWallpaperInfo() != null) {
            rst = true;
        }

        return rst;
    }

    public boolean isBlurEnable() {
        boolean enable = true;

        enable = SettingPreference.getBlurFlag() && !isLiveWallpaper();

        return enable;
    }

    @Override
    public void onClick(View v) {
        if (SettingPreference.getAdvanced()) {
            secondClickTime = System.currentTimeMillis();
            if (secondClickTime - firstClickTime < 500) {
                DevicePolicyManager mDPM =
                    (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName
                    mDeviceAdminSample =
                    new ComponentName(mContext, MyDeviceManager.class);
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

        private final WeakReference<FavoriteScene> mOuter;

        public MyHandler(FavoriteScene outer) {
            mOuter = new WeakReference<FavoriteScene>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            Bitmap bluredBitmap = (Bitmap) msg.obj;
            if (Build.VERSION.SDK_INT >= 16) {
                bluredView.setBackground(new BitmapDrawable(getResources(),
                                                            bluredBitmap));
            } else {
                bluredView.setBackgroundDrawable(new BitmapDrawable(getResources(),
                                                                    bluredBitmap));
            }
        }
    }

    @Override
    public void blurCompleted(Bitmap bluredBitmap) {
        // TODO Auto-generated method stub
        Message msg = new Message();
        msg.obj = bluredBitmap;
        mHandler.sendMessage(msg);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isPointInsideView(ev.getX(), ev.getY(), SpeedySetting.controlViewGroup)) {
                return super.dispatchTouchEvent(ev);
            } else if (SpeedySetting.isBrightControlShow) {
                if (dismissBrightSeekbar()) {
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private boolean dismissBrightSeekbar() {
        if (SpeedySetting.isBrightControlShow && SpeedySetting.controlView != null) {
            SpeedySetting.onPostBrightness();
            SpeedySetting.isBrightControlShow = false;
            SpeedySetting.controlView = null;
            return true;
        }
        return false;
    }

    public static boolean isPointInsideView(float x, float y, View view) {
        if (view == null) {
            return false;
        }
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        // point is inside view bounds
        if ((x > viewX && x < (viewX + view.getWidth())) &&
            (y > viewY && y < (viewY + view.getHeight()))) {
            return true;
        } else {
            return false;
        }
    }
}
