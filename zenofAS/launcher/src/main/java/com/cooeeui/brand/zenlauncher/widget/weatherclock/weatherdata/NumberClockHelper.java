package com.cooeeui.brand.zenlauncher.widget.weatherclock.weatherdata;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.View;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NumberClockHelper {

    public static final String
        ACTION_SAVE_WEATHER_DATA_FINISH =
        "com.cooee.save.weather.data.finish";

    public static String StringChange(String str) {
        String[] names = str.split(" ");
        if (names.length == 1) {
            return str;
        } else if (names.length == 2) {
            return names[1];
        } else {
            return str;
        }
    }

    public static boolean isHaveInternet(Context context) {
        try {
            ConnectivityManager manger = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manger.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int codeForPath(String weathercode) {
        int code = Integer.parseInt(weathercode);
        Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int ResorceId = 0;
        if (hour >= 18) {
            switch (code) {
                case 0:
                case 1:
                case 2:
                case 23:
                case 24:
                case 25:
                case 3:
                case 4:
                case 11:
                case 12:
                case 37:
                case 38:
                case 39:
                case 40:
                case 45:
                case 47:
                case 5:
                case 6:
                case 7:
                case 17:
                case 18:
                case 35:
                case 19:
                case 22:
                case 20:
                case 21:
                case 31:
                case 32:
                case 33:
                case 34:
                case 36:
                    ResorceId = R.drawable.smallweather_reaching;
                    break;
                case 8:
                case 9:
                case 10:
                    ResorceId = R.drawable.smallweather_rainshowerslate;
                    break;
                case 13:
                case 42:
                case 46:
                case 14:
                case 15:
                case 16:
                case 41:
                case 43:
                    ResorceId = R.drawable.smallweather_snowshowerslate;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 44:
                    ResorceId = R.drawable.smallweather_latecloudy;
                    break;
                default:
                    ResorceId = R.drawable.smallweather_unknow;
                    break;
            }
        } else {
            switch (code) {
                case 0:
                case 1:
                case 2:
                    ResorceId = R.drawable.smallweather_jufeng;
                    break;
                case 23:
                case 24:
                case 25:
                    ResorceId = R.drawable.smallweather_bigwindy;
                    break;
                case 3:
                case 4:
                case 11:
                case 12:
                case 37:
                case 38:
                case 39:
                case 40:
                case 45:
                case 47:
                    ResorceId = R.drawable.smallweather_thunderstorms;
                    break;
                case 5:
                case 6:
                case 7:
                    ResorceId = R.drawable.smallweather_sleet;
                    break;
                case 8:
                case 9:
                case 10:
                    ResorceId = R.drawable.smallweather_smallrain;
                    break;
                case 13:
                case 42:
                case 46:
                    ResorceId = R.drawable.smallweather_baosnow;
                    break;
                case 14:
                case 15:
                case 16:
                    ResorceId = R.drawable.smallweather_smallsnow;
                    break;
                case 17:
                case 18:
                case 35:
                    ResorceId = R.drawable.smallweather_bingbao;
                    break;
                case 19:
                case 22:
                    ResorceId = R.drawable.smallweather_sand;
                    break;
                case 20:
                case 21:
                    ResorceId = R.drawable.smallweather_fog;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 44:
                    ResorceId = R.drawable.smallweather_cloudyday;
                    break;
                case 31:
                case 32:
                case 33:
                case 34:
                case 36:
                    ResorceId = R.drawable.smallweather_sunny;
                    break;
                case 41:
                case 43:
                    ResorceId = R.drawable.smallweather_bigsnow;
                    break;
                default:
                    ResorceId = R.drawable.smallweather_unknow;
                    break;
            }
        }
        return ResorceId;
    }

    public static int codeForPathBig(String weathercode) {
        int code = Integer.parseInt(weathercode);
        Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int ResorceId = 0;
        if (hour >= 18) {
            switch (code) {
                case 0:
                case 1:
                case 2:
                case 23:
                case 24:
                case 25:
                case 3:
                case 4:
                case 11:
                case 12:
                case 37:
                case 38:
                case 39:
                case 40:
                case 45:
                case 47:
                case 5:
                case 6:
                case 7:
                case 17:
                case 18:
                case 35:
                case 19:
                case 22:
                case 20:
                case 21:
                case 31:
                case 32:
                case 33:
                case 34:
                case 36:
                    ResorceId = R.drawable.bigweather_reaching;
                    break;
                case 8:
                case 9:
                case 10:
                    ResorceId = R.drawable.bigweather_rainshowerslate;
                    break;
                case 13:
                case 42:
                case 46:
                case 14:
                case 15:
                case 16:
                case 41:
                case 43:
                    ResorceId = R.drawable.bigweather_snowshowerslate;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 44:
                    ResorceId = R.drawable.bigweather_latecloudy;
                    break;
                default:
                    ResorceId = R.drawable.bigweather_unknow;
                    break;
            }
        } else {
            switch (code) {
                case 0:
                case 1:
                case 2:
                case 23:
                case 24:
                case 25:
                    ResorceId = R.drawable.bigweather_jufeng;
                    break;
                case 3:
                case 4:
                case 11:
                case 12:
                case 37:
                case 38:
                case 39:
                case 40:
                case 45:
                case 47:
                    ResorceId = R.drawable.bigweather_thunderstorms;
                    break;
                case 5:
                case 6:
                case 7:
                    ResorceId = R.drawable.bigweather_sleet;
                    break;
                case 8:
                case 9:
                case 10:
                    ResorceId = R.drawable.bigweather_smallrain;
                    break;
                case 13:
                case 42:
                case 46:
                    ResorceId = R.drawable.bigweather_baosnow;
                    break;
                case 14:
                case 15:
                case 16:
                    ResorceId = R.drawable.bigweather_smallsnow;
                    break;
                case 17:
                case 18:
                case 35:
                    ResorceId = R.drawable.bigweather_binbao;
                    break;
                case 19:
                case 22:
                    ResorceId = R.drawable.bigweather_sand;
                    break;
                case 20:
                case 21:
                    ResorceId = R.drawable.bigweather_fog;
                    break;
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 44:
                    ResorceId = R.drawable.bigweather_cloudyday;
                    break;
                case 31:
                case 32:
                case 33:
                case 34:
                case 36:
                    ResorceId = R.drawable.bigweather_sunny;
                    break;
                case 41:
                case 43:
                    ResorceId = R.drawable.bigweather_bigsnow;
                    break;
                default:
                    ResorceId = R.drawable.bigweather_unknow;
                    break;
            }
        }
        return ResorceId;
    }

    public static int codeForSmallPath(String weathercode) {
        int code = Integer.parseInt(weathercode);
        String path = null;
        int ResorceId = 0;
        switch (code) {
            case 0:
            case 1:
            case 2:
                ResorceId = R.drawable.smallweather_jufeng;
                break;
            case 23:
            case 24:
            case 25:
                ResorceId = R.drawable.smallweather_bigwindy;
                break;
            case 3:
            case 4:
            case 11:
            case 12:
            case 37:
            case 38:
            case 39:
            case 40:
            case 45:
            case 47:
                ResorceId = R.drawable.smallweather_thunderstorms;
                break;
            case 5:
            case 6:
            case 7:
                ResorceId = R.drawable.smallweather_sleet;
                break;
            case 8:
            case 9:
            case 10:
                ResorceId = R.drawable.smallweather_smallrain;
                break;
            case 13:
            case 42:
            case 46:
                ResorceId = R.drawable.smallweather_baosnow;
                break;
            case 14:
            case 15:
            case 16:
                ResorceId = R.drawable.smallweather_smallsnow;
                break;
            case 17:
            case 18:
            case 35:
                ResorceId = R.drawable.smallweather_bingbao;
                break;
            case 19:
            case 22:
                ResorceId = R.drawable.smallweather_sand;
                break;
            case 20:
            case 21:
                ResorceId = R.drawable.smallweather_fog;
                break;
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 44:
                ResorceId = R.drawable.smallweather_cloudyday;
                break;
            case 31:
            case 32:
            case 33:
            case 34:
            case 36:
                ResorceId = R.drawable.smallweather_sunny;
                break;
            case 41:
            case 43:
                ResorceId = R.drawable.smallweather_bigsnow;
                break;
            default:
                ResorceId = R.drawable.smallweather_unknow;
                break;
        }
        return ResorceId;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int ReturnMaxInFive(int num1, int num2, int num3, int num4,
                                      int num5) {
        int[] intArray = {
            num1, num2, num3, num4, num5
        };
        int max = intArray[0];
        for (int i = 0; i < intArray.length; i++) {
            if (intArray[i] > max) {
                max = intArray[i];
            }
        }
        return max;
    }

    public static int ReturnMinInFive(int num1, int num2, int num3, int num4,
                                      int num5) {
        int[] intArray = {
            num1, num2, num3, num4, num5
        };
        int min = intArray[0];
        for (int i = 0; i < intArray.length; i++) {
            if (intArray[i] < min) {
                min = intArray[i];
            }
        }
        return min;
    }

    public static Bitmap drawCurve(Context context, int hightmp1, int hightmp2,
                                   int hightmp3, int hightmp4, int hightmp5, int lowtmp1,
                                   int lowtmp2,
                                   int lowtmp3, int lowtmp4, int lowtmp5) {
        Bitmap backImage = null;
        // TODO 整个图片的宽度，后期要放在dimens文件当中
        float width = dip2px(context, 280f);
        float height = 0;
        int screenPixelsWidth = DeviceUtils.getScreenPixelsWidth(context);
        int tempTextSize = 13;

        float moveY = dip2px(context, 13);
        float jianxi = dip2px(context, 20);
        float moveJx = dip2px(context, 20);

        if (screenPixelsWidth == 240) {
            width = dip2px(context, 1600f);
            height = dip2px(context, 1000f);
            tempTextSize = 100;
            jianxi = dip2px(context, 100);
        } else if (screenPixelsWidth < 320) {
            width = dip2px(context, 400f);
            height = dip2px(context, 220f);
        } else if (screenPixelsWidth == 320) {
            width = dip2px(context, 400f);
            height = dip2px(context, 180f);
        } else if (screenPixelsWidth == 480) {
            height = dip2px(context, 182f);
        } else {
            int screenPixelsHeight = DeviceUtils.getScreenPixelsHeight(context);
            int statusBarHeight = DeviceUtils.getStatusBarHeight(context);
            int dip2px = dip2px(context, 210f);
            int dip2px2 = dip2px(context, 245f);
            height = screenPixelsHeight - statusBarHeight - dip2px - dip2px2;
        }

        backImage = Bitmap.createBitmap((int) width, (int) height, Config.ARGB_8888);
        Canvas canvas = new Canvas(backImage);
        canvas.drawColor(Color.TRANSPARENT);// .TRANSPARENT .TRANSPARENT
        Paint paint = new Paint();
        paint.setAntiAlias(true);// 防锯齿
        paint.setDither(true);// 防抖动
        paint.setSubpixelText(true);
        paint.setARGB(255, 255, 255, 255);
        paint.setStrokeWidth(dip2px(context, 1));
        paint.setTextSize(dip2px(context, tempTextSize));

        int maxHighNum = ReturnMaxInFive(hightmp1, hightmp2, hightmp3, hightmp4, hightmp5);
        int minHighNum = ReturnMinInFive(hightmp1, hightmp2, hightmp3, hightmp4, hightmp5);
        int maxLowNum = ReturnMaxInFive(lowtmp1, lowtmp2, lowtmp3, lowtmp4, lowtmp5);
        int minLowNum = ReturnMinInFive(lowtmp1, lowtmp2, lowtmp3, lowtmp4, lowtmp5);

        if (DeviceUtils.getScreenPixelsWidth(context) <= 480) {
            moveJx = dip2px(context, 55);
        }

        Bitmap Dot = BitmapFactory.decodeResource(context.getResources(), R.drawable.dot);

        // 将整个图片的宽度平分成五份
        float fifthWidth = width / 5;
        // 小圆点的宽度和高度
        int dotWidth = Dot.getWidth();
        int dotHeight = Dot.getHeight();
        // 第一个小圆点的左边七点作为基准
        float dotLeftBase = (fifthWidth - dotWidth) / 2;
        // 绘制最高温度,连线1->2->3->4->5
        // 假如每日最高温度都一样大的是，上方曲线将会是一条直线
        if (maxHighNum - minHighNum == 0) {
            float y = height / 4 - moveY;
            float top = (height / 2 - dotHeight) / 2 - moveY;
            // 在每份的中间位置为绘制点
            float startX1 = (1 * fifthWidth) / 2;
            float startY1 = y;
            // 下一份的中间位置为终点
            float stopX1 = (3 * fifthWidth) / 2;
            float stopY1 = y;
            canvas.drawLine(startX1, startY1, stopX1, stopY1, paint);

            float startX2 = (3 * fifthWidth) / 2;
            float startY2 = y;
            float stopX2 = (5 * fifthWidth) / 2;
            float stopY2 = y;
            canvas.drawLine(startX2, startY2, stopX2, stopY2, paint);

            float startX3 = (5 * fifthWidth) / 2;
            float startY3 = y;
            float stopX3 = (7 * fifthWidth) / 2;
            float stopY3 = y;
            canvas.drawLine(startX3, startY3, stopX3, stopY3, paint);

            float startX4 = (7 * fifthWidth) / 2;
            float startY4 = y;
            float stopX4 = (9 * fifthWidth) / 2;
            float stopY4 = y;
            canvas.drawLine(startX4, startY4, stopX4, stopY4, paint);

            float left1 = dotLeftBase;
            float top1 = top;
            canvas.drawBitmap(Dot, left1, top1, paint);

            float left2 = dotLeftBase + fifthWidth;
            float top2 = top;
            canvas.drawBitmap(Dot, left2, top2, paint);

            float left3 = dotLeftBase + fifthWidth * 2;
            float top3 = top;
            canvas.drawBitmap(Dot, left3, top3, paint);

            float left4 = dotLeftBase + fifthWidth * 3;
            float top4 = top;
            canvas.drawBitmap(Dot, left4, top4, paint);

            float left5 = dotLeftBase + fifthWidth * 4;
            float top5 = top;
            canvas.drawBitmap(Dot, left5, top5, paint);

            float x1 = (fifthWidth - paint.measureText(hightmp1 + "°")) / 2;
            float y1 = height / 4 + dotHeight / 2;
            canvas.drawText(hightmp1 + "°", x1, y1, paint);

            float x2 = (fifthWidth - paint.measureText(hightmp2 + "°")) / 2 + fifthWidth;
            float y2 = height / 4 + dotHeight / 2;
            canvas.drawText(hightmp2 + "°", x2, y2, paint);

            float x3 = (fifthWidth - paint.measureText(hightmp3 + "°")) / 2 + fifthWidth * 2;
            float y3 = height / 4 + dotHeight / 2;
            canvas.drawText(hightmp3 + "°", x3, y3, paint);

            float x4 = (fifthWidth - paint.measureText(hightmp4 + "°")) / 2 + fifthWidth * 3;
            float y4 = height / 4 + dotHeight / 2;
            canvas.drawText(hightmp4 + "°", x4, y4, paint);

            float x5 = (fifthWidth - paint.measureText(hightmp5 + "°")) / 2 + fifthWidth * 4;
            float y5 = height / 4 + dotHeight / 2;
            canvas.drawText(hightmp5 + "°", x5, y5, paint);

        } else {
            float everyHeight = (height / 2 - moveJx - jianxi) / (maxHighNum - minHighNum);
            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2, height / 2 - moveJx - (hightmp1 - minHighNum)
                                                         * everyHeight, dotLeftBase
                                                                        + fifthWidth + dotWidth / 2,
                height / 2
                - moveJx - (hightmp2 - minHighNum) * everyHeight,
                paint);

            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2 + fifthWidth,
                height / 2 - moveJx - (hightmp2 - minHighNum) * everyHeight,
                dotLeftBase + fifthWidth * 2
                + dotWidth / 2, height / 2 - moveJx
                                - (hightmp3 - minHighNum) * everyHeight, paint);
            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2 + fifthWidth * 2,
                height / 2 - moveJx - (hightmp3 - minHighNum) * everyHeight,
                dotLeftBase + fifthWidth * 3
                + dotWidth / 2, height / 2 - moveJx
                                - (hightmp4 - minHighNum) * everyHeight, paint);
            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2 + fifthWidth * 3,
                height / 2 - moveJx - (hightmp4 - minHighNum) * everyHeight,
                dotLeftBase + fifthWidth * 4
                + dotWidth / 2, height / 2 - moveJx
                                - (hightmp5 - minHighNum) * everyHeight, paint);
            canvas.drawBitmap(Dot, dotLeftBase,
                              height / 2 - moveJx - (hightmp1 - minHighNum) * everyHeight
                              - dotHeight / 2, paint);
            canvas.drawBitmap(Dot,
                              dotLeftBase + fifthWidth, height / 2
                                                        - moveJx
                                                        - (hightmp2 - minHighNum) * everyHeight
                                                        - dotHeight / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 2, height / 2 - moveJx - (hightmp3 - minHighNum)
                                                                            * everyHeight
                                                      - dotHeight / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 3, height / 2 - moveJx - (hightmp4 - minHighNum)
                                                                            * everyHeight
                                                      - dotHeight / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 4, height / 2 - moveJx - (hightmp5 - minHighNum)
                                                                            * everyHeight
                                                      - dotHeight / 2, paint);

            canvas.drawText(hightmp1 + "°",
                            (fifthWidth - paint.measureText(hightmp1 + "°")) / 2, height
                                                                                  / 2 - moveJx -
                                                                                  (hightmp1
                                                                                   - minHighNum)
                                                                                  * everyHeight
                                                                                  + dotHeight / 2
                                                                                  + moveY,
                            paint);
            canvas.drawText(hightmp2 + "°",
                            (fifthWidth - paint.measureText(hightmp2 + "°")) / 2 + width
                                                                                   / 5,
                            height / 2 - moveJx - (hightmp2 - minHighNum)
                                                  * everyHeight + dotHeight / 2 + moveY,
                            paint);
            canvas.drawText(hightmp3 + "°",
                            (fifthWidth - paint.measureText(hightmp3 + "°")) / 2 + width
                                                                                   / 5 * 2,
                            height / 2 - moveJx - (hightmp3 - minHighNum) * everyHeight
                            + dotHeight / 2 + moveY, paint);
            canvas.drawText(hightmp4 + "°",
                            (fifthWidth - paint.measureText(hightmp4 + "°")) / 2 + width
                                                                                   / 5 * 3,
                            height / 2 - moveJx - (hightmp4 - minHighNum) * everyHeight
                            + dotHeight / 2 + moveY, paint);
            canvas.drawText(hightmp5 + "°",
                            (fifthWidth - paint.measureText(hightmp5 + "°")) / 2 + width
                                                                                   / 5 * 4,
                            height / 2 - moveJx - (hightmp5 - minHighNum) * everyHeight
                            + dotHeight / 2 + moveY, paint);
        }

        if (maxLowNum - minLowNum == 0) {
            float top = (height / 2 - dotHeight) / 2 - moveY;

            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2,
                (height / 2 - dotHeight) / 2
                + dotHeight / 2 - moveY + height / 2,
                dotLeftBase + fifthWidth
                + dotWidth / 2,
                (height / 2 + dotHeight) / 2 - dotHeight / 2
                - moveY + height / 2, paint);

            canvas.drawLine(
                dotLeftBase + fifthWidth
                + dotWidth / 2,
                (height / 2 - dotHeight) / 2 + dotHeight / 2
                - moveY + height / 2, (fifthWidth - dotWidth)
                                      / 2 + fifthWidth * 2 + dotWidth / 2,
                (height / 2 - dotHeight) / 2 + dotHeight / 2
                - moveY + height / 2, paint);

            canvas.drawLine(
                dotLeftBase + fifthWidth * 2
                + dotWidth / 2,
                (height / 2 - dotHeight) / 2 + dotHeight / 2
                - moveY + height / 2,
                dotLeftBase + fifthWidth * 3
                + dotWidth / 2,
                (height / 2 - dotHeight) / 2 + dotHeight / 2
                - moveY + height / 2, paint);

            canvas.drawLine(
                dotLeftBase + fifthWidth * 3
                + dotWidth / 2,
                (height / 2 - dotHeight) / 2 + dotHeight / 2
                - moveY + height / 2,
                dotLeftBase + fifthWidth * 4
                + dotWidth / 2,
                (height / 2 - dotHeight) / 2 + dotHeight / 2
                - moveY + height / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase,
                              top + height
                                    / 2, paint);
            canvas.drawBitmap(Dot,
                              dotLeftBase + fifthWidth,
                              top + height / 2,
                              paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 2, top + height
                                                            / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 3, top + height
                                                            / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 4, top + height
                                                            / 2, paint);

            canvas.drawText(lowtmp1 + "°",
                            (fifthWidth - paint.measureText(lowtmp1 + "°")) / 2, height
                                                                                 / 4 + dotHeight / 2
                                                                                 + height / 2,
                            paint);
            canvas.drawText(lowtmp2 + "°",
                            (fifthWidth - paint.measureText(lowtmp2 + "°")) / 2 + width
                                                                                  / 5,
                            height / 4 + dotHeight / 2 + height
                                                         / 2, paint);
            canvas.drawText(lowtmp3 + "°",
                            (fifthWidth - paint.measureText(lowtmp3 + "°")) / 2 + width
                                                                                  / 5 * 2,
                            height / 4 + dotHeight / 2
                            + height / 2, paint);
            canvas.drawText(lowtmp4 + "°",
                            (fifthWidth - paint.measureText(lowtmp4 + "°")) / 2 + width
                                                                                  / 5 * 3,
                            height / 4 + dotHeight / 2
                            + height / 2, paint);
            canvas.drawText(lowtmp5 + "°",
                            (fifthWidth - paint.measureText(lowtmp5 + "°")) / 2 + width
                                                                                  / 5 * 4,
                            height / 4 + dotHeight / 2
                            + height / 2, paint);
        } else {
            float everyHeight = (height / 2 - moveJx - jianxi)
                                / (maxLowNum - minLowNum);
            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2, height - moveJx - (lowtmp1 - minLowNum)
                                                     * everyHeight, dotLeftBase
                                                                    + fifthWidth + dotWidth / 2,
                height - moveJx
                - (lowtmp2 - minLowNum) * everyHeight, paint);

            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2 + fifthWidth,
                height - moveJx - (lowtmp2 - minLowNum) * everyHeight,
                dotLeftBase + fifthWidth * 2
                + dotWidth / 2, height - moveJx
                                - (lowtmp3 - minLowNum) * everyHeight, paint);
            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2 + fifthWidth * 2,
                height - moveJx - (lowtmp3 - minLowNum) * everyHeight,
                dotLeftBase + fifthWidth * 3
                + dotWidth / 2, height - moveJx
                                - (lowtmp4 - minLowNum) * everyHeight, paint);
            canvas.drawLine(
                dotLeftBase + dotWidth
                              / 2 + fifthWidth * 3,
                height - moveJx - (lowtmp4 - minLowNum) * everyHeight,
                dotLeftBase + fifthWidth * 4
                + dotWidth / 2, height - moveJx
                                - (lowtmp5 - minLowNum) * everyHeight, paint);
            canvas.drawBitmap(Dot, dotLeftBase,
                              height - moveJx - (lowtmp1 - minLowNum) * everyHeight
                              - dotHeight / 2, paint);
            canvas.drawBitmap(Dot,
                              dotLeftBase + fifthWidth, height
                                                        - moveJx
                                                        - (lowtmp2 - minLowNum) * everyHeight
                                                        - dotHeight / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 2,
                              height - moveJx - (lowtmp3 - minLowNum) * everyHeight
                              - dotHeight / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 3,
                              height - moveJx - (lowtmp4 - minLowNum) * everyHeight
                              - dotHeight / 2, paint);
            canvas.drawBitmap(Dot, dotLeftBase + fifthWidth
                                                 * 4,
                              height - moveJx - (lowtmp5 - minLowNum) * everyHeight
                              - dotHeight / 2, paint);

            canvas.drawText(lowtmp1 + "°",
                            (fifthWidth - paint.measureText(lowtmp1 + "°")) / 2, height
                                                                                 - moveJx - (lowtmp1
                                                                                             - minLowNum)
                                                                                            * everyHeight
                                                                                 + dotHeight / 2
                                                                                 + moveY, paint);
            canvas.drawText(lowtmp2 + "°",
                            (fifthWidth - paint.measureText(lowtmp2 + "°")) / 2 + width
                                                                                  / 5,
                            height - moveJx - (lowtmp2 - minLowNum)
                                              * everyHeight + dotHeight / 2 + moveY,
                            paint);
            canvas.drawText(lowtmp3 + "°",
                            (fifthWidth - paint.measureText(lowtmp3 + "°")) / 2 + width
                                                                                  / 5 * 2,
                            height - moveJx - (lowtmp3 - minLowNum)
                                              * everyHeight + dotHeight / 2 + moveY,
                            paint);
            canvas.drawText(lowtmp4 + "°",
                            (fifthWidth - paint.measureText(lowtmp4 + "°")) / 2 + width
                                                                                  / 5 * 3,
                            height - moveJx - (lowtmp4 - minLowNum)
                                              * everyHeight + dotHeight / 2 + moveY,
                            paint);
            canvas.drawText(lowtmp5 + "°",
                            (fifthWidth - paint.measureText(lowtmp5 + "°")) / 2 + width
                                                                                  / 5 * 4,
                            height - moveJx - (lowtmp5 - minLowNum)
                                              * everyHeight + dotHeight / 2 + moveY,
                            paint);
        }

        return backImage;
    }

    public static Weather getWeatherForeign(SharedPreferences sharedPref) {
        if (sharedPref.getBoolean("numberweatherstate", false)) {
            Weather weather = new Weather();
            weather.setWeathercity(sharedPref.getString(
                "numberweathercityname", null));
            weather.setResultCity(sharedPref.getString("numberresultcityname", null));
            weather.setWeathercode(sharedPref.getString("numberweathercode",
                                                        null));
            weather.setWeathercondition(sharedPref.getString(
                "numberweathercondition", null));
            weather.setCurrtmp(sharedPref.getString("numberweathercurrenttmp",
                                                    null));
            weather.setShidu(sharedPref.getString("numberweathershidu", null));
            List<Weather> list = new ArrayList<Weather>();
            Weather weather01 = new Weather();
            weather01.setWeathercode(sharedPref.getString(
                "numberlistweathercode0", null));
            weather01.setHightmp(sharedPref.getString(
                "numberlistweatherhighTmp0", null));
            weather01.setLowtmp(sharedPref.getString(
                "numberlistweatherlowTmp0", null));
            weather01.setWeatherweek(sharedPref.getString(
                "numberlistweatherweek0", null));
            weather01.setWeathercondition(sharedPref.getString(
                "numberlistweathercodition0", null));
            list.add(weather01);
            Weather weather02 = new Weather();
            weather02.setWeathercode(sharedPref.getString(
                "numberlistweathercode1", null));
            weather02.setHightmp(sharedPref.getString(
                "numberlistweatherhighTmp1", null));
            weather02.setLowtmp(sharedPref.getString(
                "numberlistweatherlowTmp1", null));
            weather02.setWeatherweek(sharedPref.getString(
                "numberlistweatherweek1", null));
            weather02.setWeathercondition(sharedPref.getString(
                "numberlistweathercodition1", null));
            list.add(weather02);
            Weather weather03 = new Weather();
            weather03.setWeathercode(sharedPref.getString(
                "numberlistweathercode2", null));
            weather03.setHightmp(sharedPref.getString(
                "numberlistweatherhighTmp2", null));
            weather03.setLowtmp(sharedPref.getString(
                "numberlistweatherlowTmp2", null));
            weather03.setWeatherweek(sharedPref.getString(
                "numberlistweatherweek2", null));
            weather03.setWeathercondition(sharedPref.getString(
                "numberlistweathercodition2", null));
            list.add(weather03);
            Weather weather04 = new Weather();
            weather04.setWeathercode(sharedPref.getString(
                "numberlistweathercode3", null));
            weather04.setHightmp(sharedPref.getString(
                "numberlistweatherhighTmp3", null));
            weather04.setLowtmp(sharedPref.getString(
                "numberlistweatherlowTmp3", null));
            weather04.setWeatherweek(sharedPref.getString(
                "numberlistweatherweek3", null));
            weather04.setWeathercondition(sharedPref.getString(
                "numberlistweathercodition3", null));
            list.add(weather04);
            Weather weather05 = new Weather();
            weather05.setWeathercode(sharedPref.getString(
                "numberlistweathercode4", null));
            weather05.setHightmp(sharedPref.getString(
                "numberlistweatherhighTmp4", null));
            weather05.setLowtmp(sharedPref.getString(
                "numberlistweatherlowTmp4", null));
            weather05.setWeatherweek(sharedPref.getString(
                "numberlistweatherweek4", null));
            weather05.setWeathercondition(sharedPref.getString(
                "numberlistweathercodition4", null));
            list.add(weather05);
            weather.setList(list);
            return weather;
        }
        return null;
    }

    public static void setWeatherForeign(Context context, SharedPreferences sharedPref,
                                         Weather weather) {
        Editor ed = sharedPref.edit();
        if (weather != null && weather.getList() != null
            && weather.getList().size() >= YahooClient.FORECAST_DAY_MIN) {
            ed.putBoolean("numberweatherstate", true);
            ed.putString("numberresultcityname", weather.getResultCity());
            ed.putString("numberweathercityname", weather.getWeathercity());
            ed.putString("numberweathercode", weather.getWeathercode());
            ed.putString("numberweathercondition",
                         weather.getWeathercondition());
            ed.putString("numberweathercurrenttmp", weather.getCurrtmp());
            ed.putString("numberweathershidu", weather.getShidu());
            ed.putString("numberlistweathercode0", weather.getList().get(0)
                .getWeathercode());
            ed.putString("numberlistweatherhighTmp0", weather.getList().get(0)
                .getHightmp());
            ed.putString("numberlistweatherlowTmp0", weather.getList().get(0)
                .getLowtmp());
            ed.putString("numberlistweatherweek0", weather.getList().get(0)
                .getWeatherweek());
            ed.putString("numberlistweathercodition0", weather.getList().get(0)
                .getWeathercondition());
            ed.putString("numberlistweathercode1", weather.getList().get(1)
                .getWeathercode());
            ed.putString("numberlistweatherhighTmp1", weather.getList().get(1)
                .getHightmp());
            ed.putString("numberlistweatherlowTmp1", weather.getList().get(1)
                .getLowtmp());
            ed.putString("numberlistweatherweek1", weather.getList().get(1)
                .getWeatherweek());
            ed.putString("numberlistweathercodition1", weather.getList().get(1)
                .getWeathercondition());
            ed.putString("numberlistweathercode2", weather.getList().get(2)
                .getWeathercode());
            ed.putString("numberlistweatherhighTmp2", weather.getList().get(2)
                .getHightmp());
            ed.putString("numberlistweatherlowTmp2", weather.getList().get(2)
                .getLowtmp());
            ed.putString("numberlistweatherweek2", weather.getList().get(2)
                .getWeatherweek());
            ed.putString("numberlistweathercodition2", weather.getList().get(2)
                .getWeathercondition());
            ed.putString("numberlistweathercode3", weather.getList().get(3)
                .getWeathercode());
            ed.putString("numberlistweatherhighTmp3", weather.getList().get(3)
                .getHightmp());
            ed.putString("numberlistweatherlowTmp3", weather.getList().get(3)
                .getLowtmp());
            ed.putString("numberlistweatherweek3", weather.getList().get(3)
                .getWeatherweek());
            ed.putString("numberlistweathercodition3", weather.getList().get(3)
                .getWeathercondition());
            ed.putString("numberlistweathercode4", weather.getList().get(4)
                .getWeathercode());
            ed.putString("numberlistweatherhighTmp4", weather.getList().get(4)
                .getHightmp());
            ed.putString("numberlistweatherlowTmp4", weather.getList().get(4)
                .getLowtmp());
            ed.putString("numberlistweatherweek4", weather.getList().get(4)
                .getWeatherweek());
            ed.putString("numberlistweathercodition4", weather.getList().get(4)
                .getWeathercondition());
            ed.commit();

            // 保存天气数据后通知天气widget更新
            context.sendBroadcast(new Intent(ACTION_SAVE_WEATHER_DATA_FINISH));
        }
    }

    public static Bitmap drawString(Context context, String str1, String str2,
                                    String str3, String str4, String str5) {
        Bitmap backImage = null;
        float width = dip2px(context, 280f);
        float height = dip2px(context, 20f);
        float fifthWidth = width / 5;
        backImage = Bitmap.createBitmap((int) width, (int) height,
                                        Config.ARGB_8888);
        Canvas canvas = new Canvas(backImage);
        canvas.drawColor(Color.TRANSPARENT);// .TRANSPARENT .TRANSPARENT
        Paint paint = new Paint();
        paint.setAntiAlias(true);// 防锯齿
        paint.setDither(true);// 防抖动
        paint.setSubpixelText(true);
        paint.setARGB(255, 255, 255, 255);
        paint.setTextSize(dip2px(context, 12));
        FontMetrics fontMetrics = paint.getFontMetrics();
        float lineHeight = (float) Math.ceil(fontMetrics.descent
                                             - fontMetrics.ascent);
        float posY = backImage.getHeight()
                     - (backImage.getHeight() - lineHeight) / 2 - fontMetrics.bottom;
        canvas.drawText(str1, (fifthWidth - paint.measureText(str1)) / 2, posY,
                        paint);
        canvas.drawText(str2, (fifthWidth - paint.measureText(str2)) / 2 + width
                                                                           / 5, posY, paint);
        canvas.drawText(str3, (fifthWidth - paint.measureText(str3)) / 2 + width
                                                                           / 5 * 2, posY, paint);
        canvas.drawText(str4, (fifthWidth - paint.measureText(str4)) / 2 + width
                                                                           / 5 * 3, posY, paint);
        canvas.drawText(str5, (fifthWidth - paint.measureText(str5)) / 2 + width
                                                                           / 5 * 4, posY, paint);
        return backImage;
    }

    public static void saveData(List<String> list, String PATH) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            // 存入数据
            File file = new File(PATH);
            if (file.exists()) {
                file.delete();
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file.toString());
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(list);
        } catch (Exception e) {
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> GetData(String PATH) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        ArrayList<String> savedArrayList = new ArrayList<String>();
        try {
            File file = new File(PATH);
            if (!file.exists()) {
                return savedArrayList;
            } else {
                fileInputStream = new FileInputStream(file.toString());
                objectInputStream = new ObjectInputStream(fileInputStream);
                savedArrayList = (ArrayList<String>) objectInputStream
                    .readObject();
                return savedArrayList;
            }
        } catch (Exception e) {
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void saveDataForeign(List<CityResult> list, String PATH) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            File file = new File(PATH);
            if (file.exists()) {
                file.delete();
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file.toString());
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(list);
        } catch (Exception e) {
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<CityResult> GetDataForeign(String PATH) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        ArrayList<CityResult> savedArrayList = new ArrayList<CityResult>();
        try {
            File file = new File(PATH);
            if (!file.exists()) {
                return savedArrayList;
            } else {
                fileInputStream = new FileInputStream(file.toString());
                objectInputStream = new ObjectInputStream(fileInputStream);
                savedArrayList = (ArrayList<CityResult>) objectInputStream
                    .readObject();
                return savedArrayList;
            }
        } catch (Exception e) {
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void RotationAnimal(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        anim.setDuration(700);
        anim.start();
    }

    public static CharSequence getCurrentWeatherTitle(Context context, String weathercondition) {
        int code = Integer.parseInt(weathercondition);
        String ResorceId;
        switch (code) {
            case 0:
            case 1:
            case 2:
            case 23:
            case 24:
            case 25:
                ResorceId = StringUtil.getString(context,
                                                 R.string.weather_content_title_tropical_storm);
                break;
            case 3:
            case 4:
            case 11:
            case 12:
            case 37:
            case 38:
            case 39:
            case 40:
            case 45:
            case 47:
                ResorceId = StringUtil.getString(context,
                                                 R.string.weather_content_title_thunder_storms);
                break;
            case 5:
            case 6:
            case 7:
                ResorceId = StringUtil.getString(context, R.string.weather_content_title_sleet);
                break;
            case 8:
            case 9:
            case 10:
                ResorceId = StringUtil.getString(context, R.string.weather_content_title_drizzle);
                break;
            case 13:
            case 42:
            case 46:
                ResorceId = StringUtil.getString(context, R.string.weather_content_snow_showers);
                break;
            case 14:
            case 15:
            case 16:
                ResorceId = StringUtil.getString(context, R.string.weather_content_blowing_snow);
                break;
            case 17:
            case 18:
            case 35:
                ResorceId = StringUtil.getString(context, R.string.weather_content_hail);
                break;
            case 19:
            case 22:
                ResorceId = StringUtil.getString(context, R.string.weather_content_dust);
                break;
            case 20:
            case 21:
                ResorceId = StringUtil.getString(context, R.string.weather_content_foggy);
                break;
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 44:
                ResorceId = StringUtil.getString(context, R.string.weather_content_mostly_cloudy);
                break;
            case 31:
            case 32:
            case 33:
            case 34:
            case 36:
                ResorceId = StringUtil.getString(context, R.string.weather_content_sunny);
                break;
            case 41:
            case 43:
                ResorceId = StringUtil.getString(context, R.string.weather_content_heavy_snow);
                break;
            default:
                ResorceId = StringUtil.getString(context, R.string.weather_content_not_available);
                break;
        }
        return ResorceId;
    }

    public static String LanWeek(Context context, String weatherweek) {
        if (weatherweek.equals("Mon")) {
            return StringUtil.getString(context, R.string.firstweek_foreign);
        } else if (weatherweek.equals("Tue")) {
            return StringUtil.getString(context, R.string.secondweek_foreign);
        } else if (weatherweek.equals("Wed")) {
            return StringUtil.getString(context, R.string.thirdweek_foreign);
        } else if (weatherweek.equals("Thu")) {
            return StringUtil.getString(context, R.string.forthweek_foreign);
        } else if (weatherweek.equals("Fri")) {
            return StringUtil.getString(context, R.string.fiveweek_foreign);
        } else if (weatherweek.equals("Sat")) {
            return StringUtil.getString(context, R.string.sixweek_foreign);
        } else if (weatherweek.equals("Sun")) {
            return StringUtil.getString(context, R.string.sevenweek_foreign);
        }

        return weatherweek;
    }

    public static void setCityResult(SharedPreferences sharepreference, CityResult result) {
        Editor edit = sharepreference.edit();
        edit.putString(Parameter.currentCityId,
                       result.getWoeid());
        edit.putString(Parameter.currentCityName,
                       result.getCityName());
        edit.putString(Parameter.currentCountry,
                       result.getCountry());

        edit.commit();
    }

    public static CityResult getCityResult(SharedPreferences sharepreference) {
        CityResult cityResult = new CityResult(sharepreference.getString(
            Parameter.currentCityId, null), sharepreference.getString(
            Parameter.currentCityName, null), sharepreference.getString(
            Parameter.currentCountry, null));
        return cityResult;
    }

    public static boolean checkWeatherData(Weather weatherCFSuccess) {
        return weatherCFSuccess != null && weatherCFSuccess.getList() != null
               && weatherCFSuccess.getList().size() >= YahooClient.FORECAST_DAY_MIN;
    }

    public static void saveWeather(Context context, SharedPreferences sharedPreferences,
                                   Weather weather) {
        if (NumberClockHelper.checkWeatherData(weather)) {
            NumberClockHelper
                .setWeatherForeign(context, sharedPreferences, weather);
        }
    }

    /**
     * 注意：包含定位功能
     */
    public static void updateWeatherThread(final Context context, final int what) {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                updateWeather(context, what);
            }
        });
    }

    /**
     * 注意：包含定位功能
     */
    public static void updateWeather(final Context context, final int what) {
        SharedPreferences sharepreference = PreferenceManager.getDefaultSharedPreferences(context);
        final CityResult cityResult = getCityResult(sharepreference);
        final String
            unit =
            sharepreference.getString(Parameter.currentunit, Parameter.DEFAULT_UNIT);
        if (cityResult.getCityName() == null) {
            //定位
            CityAutoPosition.weatherAutoPosition(context);
        } else {
            YahooClient.getWeatherInfo(cityResult, unit, context, what);
        }
    }

}
