package com.cooeeui.wallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import com.cooeeui.wallpaper.util.CommonTools;
import com.cooeeui.wallpaper.util.PreferencesUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by user on 2015/8/13.
 */
final public class WallpaperUtil {

    public static final String TAG = "WallpaperUtil";

    public static int WALLPAPWER_SELECTOR_TEXT_INVALID_COLOR = 0xFF535962;
    public static int WALLPAPWER_SELECTOR_TEXT_HIGHLIGHT_COLOR = 0xFFCDE0FF;

    /*seted wallpaper width will be 2 * screenWidth */
    public static int WALLPAPER_ROLL_FACTOR = 2;
    public static final String WALLPAPER_STORAGE_PATH = "/zenlauncher/wallpaper";

    public static void setDefaultWallpaper(Activity context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap oriBitmap = BitmapFactory.decodeResource(context.getResources(),
                                                        R.drawable.wallpaper_01,
                                                        options);
        if (oriBitmap != null) {
            saveDefaultWallpaper(oriBitmap);    // 将默认壁纸保存至存储卡
            setWallpaper(context, oriBitmap);
        }
    }

    public static void suggestWallpaperDimensions(Activity activity) {
        boolean isWallpaperFixed = PreferencesUtils
            .getBoolean(activity, PreferencesUtils.SP_WALLPAPER_FIXED, true);
        int width = CommonTools.getScreenPixelsWidth(activity);
        int height = CommonTools.getRealScreenPixelsHeight(activity);
        //多屏壁纸情况
        if (!isWallpaperFixed) {
            width = WALLPAPER_ROLL_FACTOR * CommonTools.getScreenPixelsWidth(activity);
            height = CommonTools.getRealScreenPixelsHeight(activity);
        }
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
        wallpaperManager.suggestDesiredDimensions(width, height);
    }

    /**
     * 传递进来的bitmap会根据当前设备分辨率进行缩放，并且设置完成后会被释放回收。
     *
     * 除了首次开机setDefaultWallpaper，只提供给wallpaper访问。 wallpaper是一个独立的进程，其他进程访问，sharedPreferences值不对
     * 如果想提供给其他进程访问，需要以MODE_MULTI_PROCESS模式重新加载sharedPreferences
     */
    public static boolean setWallpaper(Activity context, Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        int width = CommonTools.getScreenPixelsWidth(context);
        int height = CommonTools.getRealScreenPixelsHeight(context);
        //多屏壁纸情况
        if (!PreferencesUtils.getBoolean(context, PreferencesUtils.SP_WALLPAPER_FIXED, true)) {
            width = WALLPAPER_ROLL_FACTOR * CommonTools.getScreenPixelsWidth(context);
            height = CommonTools.getRealScreenPixelsHeight(context);
        }
        InputStream wallpaperStream = null;
        Bitmap wallpaperBitmap = null;
        try {
            wallpaperBitmap = scaleAndCropBitmap(bitmap, width, height);
            if (wallpaperBitmap == null) {
                return false;
            }
            WallpaperManager wpm = WallpaperManager.getInstance(context);
            wpm.suggestDesiredDimensions(width, height);
            wpm.forgetLoadedWallpaper();
            wallpaperStream = Bitmap2InputStream(wallpaperBitmap);
            wpm.setStream(wallpaperStream);
            wallpaperStream.close();
            wallpaperBitmap.recycle();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (wallpaperStream != null) {
                try {
                    wallpaperStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (wallpaperBitmap != null && !wallpaperBitmap.isRecycled()) {
                wallpaperBitmap.recycle();
            }
        }
    }

    public static void saveDefaultWallpaper(Bitmap bitmap) {
        if (isSdCardExist()) {
            File skRoot = Environment.getExternalStorageDirectory();
            File file = new File(skRoot.getPath() + WALLPAPER_STORAGE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }

            File filePath = new File(file.getPath() + "/default.jpg");//将要保存图片的路径
            if (!filePath.exists()) {
                try {
                    BufferedOutputStream
                        bos =
                        new BufferedOutputStream(new FileOutputStream(filePath));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap scaleAndCropBitmap(Bitmap bitmap, int destW, int destH) {
        Bitmap resizeBmp = null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float x, y, scale;
        Matrix matrix = new Matrix();
        if (width > height) {
            scale = ((float) destH / height);
            x = (width - destW * height / destH) / 2;   //获取bitmap源文件中x做表需要偏移的像数大小
            y = 0;
        } else if (width < height) {
            scale = ((float) destW / width);
            x = 0;
            y = (height - destH * width / destW) / 2;   //获取bitmap源文件中y做表需要偏移的像数大小
        } else {
            scale = ((float) destW / width);
            x = 0;
            y = 0;
        }
        matrix.postScale(scale, scale); // 长和宽放大缩小的比例
        try {
            resizeBmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) (width - x),
                                            (int) (height - y), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resizeBmp;
    }

    public static InputStream Bitmap2InputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        if (baos != null) {
            try {
                baos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return is;
    }

    public static boolean isSdCardExist() {

        if (Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED)) {//判断是否已经挂载
            return true;
        }
        return false;
    }
}
