package com.cooeeui.brand.zenlauncher.wallpaper.local;

/**
 * Created by Administrator on 2015/8/7.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class AsyncImageLoader {

    private static String TAG = "diaoliang";
    private HashMap<String, SoftReference<Drawable>> imageCache;

    public AsyncImageLoader() {
        imageCache = new HashMap<String, SoftReference<Drawable>>();
    }

    public static Drawable loadImageFromUrl(String url) {
        Options options = new Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(url, options);
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    public Drawable loadDrawable(final String imageUrl, final String Tag,
                                 final ImageCallback imageCallback) {
        if (imageCache.containsKey(imageUrl)) {

            SoftReference<Drawable> softReference = imageCache.get(imageUrl);
            Drawable drawable = softReference.get();
            if (drawable != null) {
                return drawable;
            }
            if (drawable == null) {
            }
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback
                    .imageLoaded((Drawable) message.obj, imageUrl, Tag);
            }
        };
        new Thread() {
            @Override
            public void run() {
                Drawable drawable = loadImageFromUrl(imageUrl);
                imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
                Message message = handler.obtainMessage(0, drawable);
                handler.sendMessage(message);
            }
        }.start();
        return null;
    }

    public interface ImageCallback {

        public void imageLoaded(Drawable imageDrawable, String imageUrl,
                                String Tag);

    }
}
