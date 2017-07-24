/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cooeeui.brand.zenlauncher.apps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

import com.cooeeui.zenlauncher.R;


final public class Utilities {

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;

    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                                                       Paint.FILTER_BITMAP_FLAG));
    }

    static int sColors[] = {
        0xffff0000, 0xff00ff00, 0xff0000ff
    };
    static int sColorIndex = 0;

    /**
     * Returns a FastBitmapDrawable with the icon, accurately sized.
     */
    public static Drawable createIconDrawable(Bitmap icon) {
        FastBitmapDrawable d = new FastBitmapDrawable(icon);
        d.setFilterBitmap(true);
        resizeIconDrawable(d);
        return d;
    }

    /**
     * Resizes an icon drawable to the correct icon size.
     */
    static void resizeIconDrawable(Drawable icon) {
        icon.setBounds(0, 0, sIconTextureWidth, sIconTextureHeight);
    }


    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources()
                                                        .getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth,
                                                      textureHeight, Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth - width) / 2;
            final int top = (textureHeight - height) / 2;

            @SuppressWarnings("all")
            // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) {
                    sColorIndex = 0;
                }
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left + width, top + height,
                                debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left + width, top + height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        sIconWidth = sIconHeight = (int) resources
            .getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;
    }
}
