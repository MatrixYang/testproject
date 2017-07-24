package com.cooeeui.brand.zenlauncher.scenes.utils;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cooeeui.zenlauncher.R;

import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static Bitmap getIcon(Resources resources, int iconId) {
        int iconSize = resources.getDimensionPixelSize(R.dimen.image_size);
        return getIcon(resources, iconId, iconSize);
    }

    public static Bitmap resizeBitmap(Bitmap bm, int length, boolean recycle) {
        Bitmap tmp = Bitmap.createScaledBitmap(bm, length, length, true);
        if (recycle) {
            bm.recycle();
        }
        return tmp;
    }

    public static void closeSilently(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            is.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    public static int computeSampleSizeLarger(int w, int h,
                                              int minSideLength) {
        int initialSize = Math.min(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) {
            return 1;
        }

        return initialSize <= 8
               ? Integer.highestOneBit(initialSize)
               : initialSize / 8 * 8;
    }

    public static Bitmap getIcon(Resources resources, int iconId, int length) {
        InputStream is = null;

        try {
            is = resources.openRawResource(iconId);
        } catch (NotFoundException e) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        closeSilently(is);

        try {
            is = resources.openRawResource(iconId);
        } catch (NotFoundException e) {
            return null;
        }

        options.inSampleSize = computeSampleSizeLarger(
            options.outWidth, options.outHeight, length);
        options.inJustDecodeBounds = false;
        Bitmap result = BitmapFactory.decodeStream(is, null, options);
        closeSilently(is);

        if (result == null) {
            return null;
        }

        if (result.getWidth() != length) {
            result = resizeBitmap(result, length, true);
        }

        return result;
    }
}
