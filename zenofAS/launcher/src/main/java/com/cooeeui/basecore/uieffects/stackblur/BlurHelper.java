package com.cooeeui.basecore.uieffects.stackblur;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.zenlauncher.R;

import java.lang.ref.WeakReference;

/**
 * 模糊效果的帮助接口
 *
 * @author added by Hugo.ye
 */
public final class BlurHelper {

    private static final String TAG = "BlurHelper";

    private Activity mContext;
    private WeakReference<BlurCallbacks> mCallbacks;

    private float mDefaultScaleFactor = 8f;
    private int mDefaultBlurRadius;
    private boolean mBluring;

    public interface BlurCallbacks {

        public void blurCompleted(Bitmap bluredBitmap);
    }

    public BlurHelper(Activity context, BlurCallbacks callbacks) {
        mContext = context;
        mCallbacks = new WeakReference<BlurCallbacks>(callbacks);
        mDefaultBlurRadius = mContext.getResources().getDimensionPixelSize(
            R.dimen.blur_default_radius);
    }

    /**
     * 在非 UI 线程里对view进行模糊操作，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传 ，所以请实现BlurCallbacks回调接口获取最终模糊的bitmap
     *
     * @param oriView 需要模糊的view
     */
    public void blurViewExtraWallpaperNonUiThread(View oriView) {
        blurViewExtraWallpaperNonUiThread(oriView, mDefaultBlurRadius, mDefaultScaleFactor);
    }

    /**
     * 在非 UI 线程里对view进行模糊操作，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传 ，所以请实现BlurCallbacks回调接口获取最终模糊的bitmap
     *
     * @param oriView 需要模糊的view
     * @param radius  模糊半径，必须 >=1
     */
    public void blurViewExtraWallpaperNonUiThread(View oriView, int radius) {
        blurViewExtraWallpaperNonUiThread(oriView, radius, mDefaultScaleFactor);
    }

    /**
     * 在非 UI 线程里对view进行模糊操作，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传 ，所以请实现BlurCallbacks回调接口获取最终模糊的bitmap
     *
     * @param oriView     需要模糊的view
     * @param radius      模糊半径，必须 >=1
     * @param scaleFactor 对oriView的缩放因子，必须 >=1f。对于模糊算法的一种优化，现将需要模糊的oriView进行缩放，然后进行模糊，减少计算量，增大效率，但是会削弱效果
     */
    public void blurViewExtraWallpaperNonUiThread(final View oriView, final int radius,
                                                  final float scaleFactor) {
        if (mBluring) {
            return;
        }

        mBluring = true;

        int width = oriView.getMeasuredWidth();
        int height = oriView.getMeasuredHeight();
        final Bitmap oriBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas captureCanvas = new Canvas(oriBitmap);
        final Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        boolean captureSuccess = false;
        if (width != 0 && height != 0) {

            // The first time to capture screen
            captureSuccess = captureViewBitmapToCanvas(oriView, captureCanvas, paint);

            // we make sure that capture screen success , so try again !
            if (!captureSuccess) {
                captureSuccess = captureViewBitmapToCanvas2(oriView, captureCanvas, paint);
            }

            if (captureSuccess) {
                ThreadUtil.execute(new Runnable() {

                    @Override
                    public void run() {

                        Bitmap bluredBitmap = fastBlur(oriBitmap, scaleFactor, radius, paint);

                        if (mCallbacks != null && mCallbacks.get() != null
                            && bluredBitmap != null) {
                            mCallbacks.get().blurCompleted(bluredBitmap);
                        }
                        mBluring = false;
                    }
                });
            } else {
                mBluring = false;
                Log.e(TAG, "capture the screen failed !", new RuntimeException());
            }
        } else { // 首次启动可能view还未初始化完成，确保背景模糊效果
            captureSuccess = captureWallpaperBitmapToCanvas(captureCanvas,
                                                            new Rect(0, 0,
                                                                     oriView.getMeasuredWidth(),
                                                                     oriView.getMeasuredHeight()),
                                                            paint);

            if (captureSuccess) {
                ThreadUtil.execute(new Runnable() {

                    @Override
                    public void run() {

                        Bitmap bluredBitmap = fastBlur(oriBitmap, scaleFactor, radius, paint);

                        if (mCallbacks != null && mCallbacks.get() != null
                            && bluredBitmap != null) {
                            mCallbacks.get().blurCompleted(bluredBitmap);
                        }
                        mBluring = false;
                    }
                });
            } else {
                mBluring = false;
                Log.e(TAG, "capture the wallpaper failed !", new RuntimeException());
            }
        }
    }

    /**
     * 在非 UI 线程里对壁纸进行模糊操作，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传 ，所以请实现BlurCallbacks回调接口获取最终模糊的bitmap
     */
    public void blurWallpaperNonUiThread() {
        if (mBluring) {
            return;
        }

        mBluring = true;

        int width = DeviceUtils.getScreenPixelsWidth(mContext);
        int height = DeviceUtils.getRealScreenPixelsHeight(mContext);
        final Bitmap oriBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas captureCanvas = new Canvas(oriBitmap);
        final Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        boolean captureSuccess = false;
        captureSuccess =
            captureWallpaperBitmapToCanvas(captureCanvas, new Rect(0, 0, width, height), paint);
        if (captureSuccess) {
            ThreadUtil.execute(new Runnable() {

                @Override
                public void run() {

                    Bitmap
                        bluredBitmap =
                        fastBlur(oriBitmap, mDefaultScaleFactor, mDefaultBlurRadius, paint);

                    if (mCallbacks != null && mCallbacks.get() != null && bluredBitmap != null) {
                        mCallbacks.get().blurCompleted(bluredBitmap);
                    }
                    mBluring = false;
                }
            });
        } else {
            mBluring = false;
            Log.e(TAG, "capture the screen failed !", new RuntimeException());
        }
    }

    /**
     * 模糊接口，对原始oriBitmap进行模糊，返回模糊后的bitmap
     *
     * @param oriBitmap 需要模糊的bitmap
     */
    public Bitmap fastBlur(Bitmap oriBitmap) {
        return fastBlur(oriBitmap, mDefaultScaleFactor, mDefaultBlurRadius, new Paint(
            Paint.FILTER_BITMAP_FLAG));
    }

    /**
     * 模糊接口，对原始oriBitmap进行模糊，返回模糊后的bitmap
     *
     * @param oriBitmap   需要模糊的bitmap
     * @param scaleFactor 对oriView的缩放因子，必须 >=1f。对于模糊算法的一种优化，现将需要模糊的oriView进行缩放，然后进行模糊，减少计算量，增大效率，但是会削弱效果
     * @param radius      模糊半径，必须 >=1
     * @param paint       可以为null,或者Paint.FILTER_BITMAP_FLAG 进行抗锯齿
     */
    public Bitmap fastBlur(Bitmap oriBitmap, float scaleFactor, int radius, Paint paint) {
        Bitmap bluredBitmap = null;
        if (scaleFactor > 1f) {
            Bitmap overlayBitmap = Bitmap.createBitmap(
                (int) (oriBitmap.getWidth() / scaleFactor),
                (int) (oriBitmap.getHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
            Canvas blurCanvas = new Canvas(overlayBitmap);
            blurCanvas.scale(1 / scaleFactor, 1 / scaleFactor);
            blurCanvas.drawBitmap(oriBitmap, 0, 0, paint);
            bluredBitmap = FastBlur.doBlur(overlayBitmap, radius, true);
        } else {
            bluredBitmap = FastBlur.doBlur(oriBitmap, radius, true);
        }

        return bluredBitmap;
    }

    private boolean captureWallpaperBitmapToCanvas(Canvas canvas, Rect dst, Paint paint) {
        boolean rst = false;
        try {
            Bitmap wpBitmap = getWallpaperBitmap();
            canvas.drawBitmap(wpBitmap, null, dst, paint);
            rst = true;
        } catch (Exception e) {
            Log.e(TAG, "captureWallpaperBitmapToCanvas failed !", e);
        }
        return rst;
    }

    private boolean captureViewBitmapToCanvas(View oriView, Canvas canvas, Paint paint) {
        boolean captureSuccess = false;
        try {
            captureWallpaperBitmapToCanvas(canvas, new Rect(0, 0, oriView.getMeasuredWidth(),
                                                            oriView.getMeasuredHeight()), paint);
            oriView.draw(canvas);
            captureSuccess = true;
        } catch (Exception e) {
            Log.e(TAG, "captureViewBitmapToCanvas failed ! ", e);
        }

        return captureSuccess;
    }

    private boolean captureViewBitmapToCanvas2(View oriView, Canvas canvas, Paint paint) {
        boolean captureSuccess = false;
        try {
            captureWallpaperBitmapToCanvas(canvas, new Rect(0, 0, oriView.getMeasuredWidth(),
                                                            oriView.getMeasuredHeight()), paint);
            Bitmap captureBitmap = getViewBitmap(oriView);
            canvas.drawBitmap(captureBitmap, 0, 0, paint);
            captureSuccess = true;
        } catch (Exception e) {
            Log.e(TAG, "captureViewBitmapToCanvas2 failed !", e);
        }
        return captureSuccess;
    }

    /**
     * get current wallpaper's bitmap
     */
    private Bitmap getWallpaperBitmap() {
        Bitmap wpBitmap = null;
        try {
            WallpaperManager wpm = WallpaperManager.getInstance(mContext);
            wpBitmap = ((BitmapDrawable) wpm.getDrawable()).getBitmap();
        } catch (Exception e) {
            Log.e(TAG, "get wallpaper bitmap failed !", e);
        }

        return wpBitmap;
    }

    /**
     * Draw the view into a bitmap.
     *
     * @param v 需要绘制的View
     * @return 返回Bitmap对象
     */
    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }

        boolean successed = true;

        // The first try
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "The first try failed getViewBitmap(" + v + ")",
                  new RuntimeException());
            successed = false;
        }

        // The scecond try
        if (!successed) {
            int widthSpec = View.MeasureSpec.makeMeasureSpec(v.getMeasuredWidth(),
                                                             View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(v.getMeasuredHeight(),
                                                              View.MeasureSpec.EXACTLY);
            v.measure(widthSpec, heightSpec);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

            v.buildDrawingCache();
            cacheBitmap = v.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e(TAG, "The scecond try failed getViewBitmap(" + v + ")",
                      new RuntimeException());
                return null;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

}
