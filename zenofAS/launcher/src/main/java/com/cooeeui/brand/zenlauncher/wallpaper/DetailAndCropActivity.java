package com.cooeeui.brand.zenlauncher.wallpaper;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.basecore.utilities.NetworkAvailableUtils;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.wallpaper.croplib.CropImageView;
import com.cooeeui.wallpaper.croplib.CropUtil;
import com.cooeeui.wallpaper.croplib.HighlightView;
import com.cooeeui.wallpaper.croplib.ImageViewTouchBase;
import com.cooeeui.wallpaper.croplib.MonitoredActivity;
import com.cooeeui.wallpaper.croplib.RotateBitmap;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Steve on 2015/8/8.
 */
public class DetailAndCropActivity extends MonitoredActivity
    implements CropImageView.SaveStatus, DialogUtil.DialogCancelListener {

    public static final int NETERROR = 00;
    public static final int NETSUCCESS = 11;
    // Static final constants
    private static final String TAG = "DetailAndCropActivity";

    // Output image
    private int maxX = 0;
    private int maxY = 0;
    private int exifRotation;
    private Uri mSourceUri;
    private Bitmap mSourceBitmap;
    private boolean isSaving;
    private int sampleSize;
    private RotateBitmap rotateBitmap;
    private CropImageView imageView;
    private HighlightView cropView;
    private DialogUtil dialogUtil = null;
    private Button setWallPaper;
    private ImageView shareWallpaper;
    private ImageButton downloadWallpaper;
    private View linearError;
    private boolean hasTaskRun = false;
    private DownloadImageTask downloadImageTask;
    private String wallpaperURL;
    private String imageName;

    private TextView mWallpaperSelectorFixed = null;
    private TextView mWallpaperSelectorScrollable = null;
    private boolean mWallpaperFixed = false;
    private final Handler mHandler = new MyHandler(this);

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

        private final WeakReference<DetailAndCropActivity> mOuter;

        public MyHandler(DetailAndCropActivity outer) {
            mOuter = new WeakReference<DetailAndCropActivity>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailAndCropActivity outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case NETERROR:
                        if (dialogUtil != null) {
                            dialogUtil.cancelLoadingDialog();
                        }
                        imageView.setVisibility(View.INVISIBLE);
                        linearError.setVisibility(View.VISIBLE);
                        setWallPaper.setEnabled(false);
                        shareWallpaper.setEnabled(false);
                        downloadWallpaper.setEnabled(false);
                        break;
                    case NETSUCCESS:
                        imageView.setVisibility(View.VISIBLE);
                        linearError.setVisibility(View.INVISIBLE);
                        setWallPaper.setEnabled(true);
                        shareWallpaper.setEnabled(true);
                        downloadWallpaper.setEnabled(true);
                        break;
                }
            }
        }
    }

    //保存图片的同时返回截图
    private Bitmap onSetWallPaper() {
        if (cropView == null || isSaving) {
            return null;
        }
        isSaving = true;
        Bitmap croppedImage;
        Rect r = cropView.getScaledCropRect(sampleSize);
        int width = r.width();
        int height = r.height();

        int outWidth = width;
        int outHeight = height;

        if (maxX > 0 && maxY > 0 && (width > maxX || height > maxY)) {
            float ratio = (float) width / (float) height;
            if ((float) maxX / (float) maxY > ratio) {
                outHeight = maxY;
                outWidth = (int) ((float) maxY * ratio + .5f);
            } else {
                outWidth = maxX;
                outHeight = (int) ((float) maxX / ratio + .5f);
            }
        }
        try {
            //根据悬浮框的大小截图
            croppedImage = decodeRegionCrop(r, outWidth, outHeight);
        } catch (IllegalArgumentException e) {
            recycleResource();
            finish();
            return null;
        }

        return croppedImage;
    }

    private void recycleResource() {
        if (imageView != null) {
            imageView.highlightViews.clear();
            imageView.clear();
        }
        if (rotateBitmap != null) {
            rotateBitmap.recycle();
            rotateBitmap = null;
        }
        if (mSourceBitmap != null && !mSourceBitmap.isRecycled()) {
            mSourceBitmap.recycle();
            mSourceBitmap = null;
        }
        System.gc();
    }

    private Bitmap decodeRegionCrop(Rect rect, int outWidth, int outHeight) {
        // Release memory now
        InputStream is = null;
        Bitmap croppedImage = null;
        try {

            //权限被拒绝了
            if (mSourceUri != null) {
                ContentResolver contentResolver = getContentResolver();
                is = contentResolver.openInputStream(mSourceUri);
            } else if (mSourceBitmap != null) {
                is = WallpaperUtil.Bitmap2InputStream(mSourceBitmap);
            }

            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            //原始图片的宽度和高度
            final int width = decoder.getWidth();
            final int height = decoder.getHeight();

            if (exifRotation != 0) {
                // Adjust crop area to account for image rotation
                Matrix matrix = new Matrix();
                matrix.setRotate(-exifRotation);

                RectF adjusted = new RectF();
                matrix.mapRect(adjusted, new RectF(rect));

                // Adjust to account for origin at 0,0
                adjusted.offset(adjusted.left < 0 ? width : 0, adjusted.top < 0 ? height : 0);
                rect =
                    new Rect((int) adjusted.left, (int) adjusted.top, (int) adjusted.right,
                             (int) adjusted.bottom);
            }

            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
                if (rect.width() > outWidth || rect.height() > outHeight) {
                    Matrix matrix = new Matrix();
                    matrix.postScale((float) outWidth / rect.width(),
                                     (float) outHeight / rect.height());
                    croppedImage =
                        Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(),
                                            croppedImage.getHeight(), matrix, true);
                }
            } catch (IllegalArgumentException e) {
                // Rethrow with some extra information
                throw new IllegalArgumentException(
                    "Rectangle " + rect + " is outside of the image ("
                    + width + "," + height + "," + exifRotation + ")", e);
            }

        } catch (IOException e) {
            recycleResource();
            finish();
        } catch (OutOfMemoryError e) {
            recycleResource();
            finish();
        } finally {
            CropUtil.closeSilently(is);
        }
        return croppedImage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_detail_activity);

        initViews();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            wallpaperURL = extras.getString(OnlineWallpaperActivity.EXTRA_DOWNLOAD_URL);
            imageName = extras.getString(OnlineWallpaperActivity.EXTRA_DOWNLOAD_NAME);
            if (downloadImageTask != null
                && downloadImageTask.getStatus() != AsyncTask.Status.FINISHED) {
                downloadImageTask.cancel(true);
                downloadImageTask = null;
            }
            downloadImageTask = new DownloadImageTask();
            downloadImageTask.execute(wallpaperURL);

        } else {
            downloadWallpaper.setVisibility(View.INVISIBLE);
            Uri data = getIntent().getData();
            beginCrop(data, null);
        }

    }

    private void initViews() {
        initWallpaperTypeSelector();
        initOthers();
    }

    private void initWallpaperTypeSelector() {
        mWallpaperSelectorFixed = (TextView) findViewById(R.id.myViewFixed);
        mWallpaperSelectorFixed.setText(
            StringUtil.getString(this, R.string.wallPaper_selector_fixed));
        mWallpaperSelectorScrollable = (TextView) findViewById(R.id.myViewScrollable);
        mWallpaperSelectorScrollable
            .setText(StringUtil.getString(this, R.string.wallPaper_selector_scrollable));

        mWallpaperFixed = LauncherPreference.getWallpaperFixed();

        if (mWallpaperFixed) {

            Drawable
                drawable = getResources().getDrawable(R.drawable.wallpaper_fixed_highlight);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mWallpaperSelectorFixed.setCompoundDrawables(null, drawable, null, null);

            drawable = getResources().getDrawable(R.drawable.wallpaper_scrollable_normal);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mWallpaperSelectorScrollable.setCompoundDrawables(null, drawable, null, null);

            mWallpaperSelectorFixed
                .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_HIGHLIGHT_COLOR);
            mWallpaperSelectorScrollable
                .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_INVALID_COLOR);
        } else {

            Drawable drawable = getResources().getDrawable(R.drawable.wallpaper_fixed_normal);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mWallpaperSelectorFixed.setCompoundDrawables(null, drawable, null, null);

            drawable = getResources().getDrawable(R.drawable.wallpaper_scrollable_highlight);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mWallpaperSelectorScrollable.setCompoundDrawables(null, drawable, null, null);

            mWallpaperSelectorFixed
                .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_INVALID_COLOR);
            mWallpaperSelectorScrollable
                .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_HIGHLIGHT_COLOR);
        }

        mWallpaperSelectorFixed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mWallpaperFixed) {

                    LauncherPreference.setWallpaperrFixed(true);

                    mWallpaperFixed = true;

                    Drawable
                        drawable =
                        getResources().getDrawable(R.drawable.wallpaper_fixed_highlight);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                                       drawable.getMinimumHeight());
                    mWallpaperSelectorFixed.setCompoundDrawables(null, drawable, null, null);

                    drawable = getResources().getDrawable(R.drawable.wallpaper_scrollable_normal);
                    drawable
                        .setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mWallpaperSelectorScrollable.setCompoundDrawables(null, drawable, null, null);

                    mWallpaperSelectorFixed
                        .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_HIGHLIGHT_COLOR);
                    mWallpaperSelectorScrollable
                        .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_INVALID_COLOR);

                    beginCrop(mSourceUri, mSourceBitmap);
                }


            }
        });
        mWallpaperSelectorScrollable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mWallpaperFixed) {

                    LauncherPreference.setWallpaperrFixed(false);

                    mWallpaperFixed = false;

                    Drawable
                        drawable =
                        getResources().getDrawable(R.drawable.wallpaper_fixed_normal);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                                       drawable.getMinimumHeight());
                    mWallpaperSelectorFixed.setCompoundDrawables(null, drawable, null, null);

                    drawable =
                        getResources().getDrawable(R.drawable.wallpaper_scrollable_highlight);
                    drawable
                        .setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    mWallpaperSelectorScrollable.setCompoundDrawables(null, drawable, null, null);

                    mWallpaperSelectorFixed
                        .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_INVALID_COLOR);
                    mWallpaperSelectorScrollable
                        .setTextColor(WallpaperUtil.WALLPAPWER_SELECTOR_TEXT_HIGHLIGHT_COLOR);

                    beginCrop(mSourceUri, mSourceBitmap);
                }


            }
        });
    }

    private void initOthers() {
        linearError = findViewById(R.id.linearNetError);
        linearError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkAvailableUtils.isNetworkAvailable(DetailAndCropActivity.this)
                    && !hasTaskRun) {
                    if (downloadImageTask != null
                        && downloadImageTask.getStatus() != AsyncTask.Status.FINISHED) {
                        downloadImageTask.cancel(true);
                    }
                    downloadImageTask = new DownloadImageTask();
                    downloadImageTask.execute(wallpaperURL);
                }
            }
        });
        setWallPaper = (Button) findViewById(R.id.button_setWallPaper);
        setWallPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        saveBitmapToExternalStorage(mSourceBitmap, imageName, false);
                        setWallpaper();
                    }
                });
            }
        });

        shareWallpaper = (ImageView) findViewById(R.id.shareWallpaper);
        shareWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = null;
                if (mSourceUri != null) {
                    uri = mSourceUri;
                } else {
                    uri = getShareBitmapURI(mSourceBitmap, imageName);
                }
                if (uri != null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(shareIntent);
                }
            }
        });
        downloadWallpaper = (ImageButton) findViewById(R.id.downloadWallpaper);
        downloadWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSourceBitmap != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            saveBitmapToExternalStorage(mSourceBitmap, imageName, true);
                        }
                    });
                }
            }
        });

        imageView = (CropImageView) findViewById(R.id.crop_image);
        imageView.context = this;
        imageView.center(true, true);
        imageView.setRecycler(new ImageViewTouchBase.Recycler() {
            @Override
            public void recycle(Bitmap b) {
                b.recycle();
                System.gc();
            }
        });
        ((TextView) findViewById(R.id.tv_set_wallpaper)).setText(
            StringUtil.getString(this, R.string.wallpaper_set_wallpaper));
        ((TextView) findViewById(R.id.tv_wallpaper_net_error)).setText(
            StringUtil.getString(this, R.string.wallpaper_check_net));
        ((Button) findViewById(R.id.button_setWallPaper)).setText(
            StringUtil.getString(this, R.string.wallpaper_set_as_wallpaper));
    }

    //开始剪切功能
    private void beginCrop(Uri sourceUri, Bitmap sourceBitmap) {
        setupFromIntent(sourceUri, sourceBitmap);
        startCrop();
    }


    private void setupFromIntent(Uri sourceUri, Bitmap sourceBitmap) {
        mSourceUri = sourceUri;
        mSourceBitmap = sourceBitmap;
        if (mSourceUri != null) {
            exifRotation = CropUtil.getExifRotation(
                CropUtil.getFromMediaUri(this, getContentResolver(), mSourceUri));

            InputStream is = null;
            try {
                sampleSize = calculateBitmapSampleSize(sourceUri, null);
                is = getContentResolver().openInputStream(sourceUri);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = sampleSize;
                rotateBitmap = new RotateBitmap(
                    BitmapFactory.decodeStream(is, null, option), exifRotation);
            } catch (IOException e) {
                Log.e(TAG, "Error reading image: " + e.getMessage(), e);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "OOM reading image: " + e.getMessage(), e);
            } finally {
                CropUtil.closeSilently(is);
            }
        } else if (mSourceBitmap != null) {
            exifRotation = 0;
            InputStream is = null;
            try {
                sampleSize = calculateBitmapSampleSize(null, sourceBitmap);
                is = WallpaperUtil.Bitmap2InputStream(sourceBitmap);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inSampleSize = sampleSize;
                rotateBitmap = new RotateBitmap(
                    BitmapFactory.decodeStream(is, null, option), exifRotation);
            } catch (IOException e) {
                Log.e(TAG, "Error reading image: " + e.getMessage(), e);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "OOM reading image: " + e.getMessage(), e);
            } finally {
                CropUtil.closeSilently(is);
            }
        }
    }

    private int calculateBitmapSampleSize(Uri bitmapUri, Bitmap sourceBitmap) throws IOException {
        int sampleSize = 1;
        int screenW = DeviceUtils.getScreenPixelsWidth(this);
        int screenH = DeviceUtils.getRealScreenPixelsHeight(this);
        if (bitmapUri != null) {
            InputStream is = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                is = getContentResolver().openInputStream(bitmapUri);
                BitmapFactory.decodeStream(is, null, options); // Just get image size
            } finally {
                CropUtil.closeSilently(is);
            }

            while (options.outHeight / sampleSize > screenH
                   || options.outWidth / sampleSize > screenW) {
                sampleSize = sampleSize << 1;
            }
        } else if (sourceBitmap != null) {
            while (sourceBitmap.getHeight() / sampleSize > screenH
                   || sourceBitmap.getWidth() / sampleSize > screenW) {
                sampleSize = sampleSize << 1;
            }
        }
        return sampleSize;
    }


    private void startCrop() {
        if (isFinishing()) {
            return;
        }

        imageView.setImageRotateBitmapResetBase(rotateBitmap, true);

        CropUtil.startBackgroundJob(this, null, getResources().getString(R.string.crop__wait),
                                    new Runnable() {
                                        public void run() {
                                            final CountDownLatch latch = new CountDownLatch(1);
                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    if (imageView.getScale() == 1F) {
                                                        imageView.center(true, true);
                                                    }
                                                    latch.countDown();
                                                }
                                            });
                                            try {
                                                latch.await();
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                            new Cropper().crop();
                                        }
                                    }, mHandler);
    }

    private void setWallpaper() {
        Bitmap cropedBitmap = onSetWallPaper();
        if (WallpaperUtil.setWallpaper(this, cropedBitmap)) {
            Toast.makeText(this,
                           StringUtil.getString(this, R.string.wallPaperSetSuccess),
                           Toast.LENGTH_LONG).show();

            recycleResource();
            //这里之所以选择直接跳转，是因为OnlineWallpaperActivity启动模式为singletask
            Intent intent = new Intent(this, OnlineWallpaperActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, StringUtil.getString(this, R.string.wallPaperSetFailed),
                           Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onDestroy() {
        recycleResource();
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    @Override
    public boolean isSaving() {
        return isSaving;
    }

    /**
     * save the bitmap
     *
     * @param bm      to be saved bitmap
     * @param picName the pic name ,null is the local map
     * @param toast   whether pop toast
     */
    public void saveBitmapToExternalStorage(Bitmap bm, String picName, boolean toast) {
        if (bm == null || TextUtils.isEmpty(picName) || !WallpaperUtil.isSdCardExist()) {
            return;
        }
        File skRoot = Environment.getExternalStorageDirectory();
        File file = new File(skRoot.getPath() + WallpaperUtil.WALLPAPER_STORAGE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file.getPath(), picName);
        if (f.exists()) {
            if (toast) {
                Toast.makeText(this, StringUtil.getString(this, R.string.wallpaper_save_success),
                               Toast.LENGTH_LONG).show();
            }
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            if (toast) {
                Toast.makeText(this, StringUtil.getString(this, R.string.wallpaper_save_success),
                               Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri getShareBitmapURI(Bitmap bm, String picName) {

        if (TextUtils.isEmpty(picName) || !WallpaperUtil.isSdCardExist()) {
            return null;
        }
        File skRoot = Environment.getExternalStorageDirectory();
        File file = new File(skRoot.getPath() + WallpaperUtil.WALLPAPER_STORAGE_PATH);

        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file.getPath(), picName);
        if (f.exists()) {
            return Uri.parse(f.getAbsolutePath());
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (f.exists()) {
            return Uri.parse(f.getAbsolutePath());
        }
        return null;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            super.onKeyDown(keyCode, event);
            if (dialogUtil != null) {
                dialogUtil.cancelLoadingDialog();
            }
            recycleResource();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCancelDialog() {
        this.finish();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {


        public DownloadImageTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hasTaskRun = true;
            mHandler.obtainMessage(NETSUCCESS).sendToTarget();
            dialogUtil = new DialogUtil(DetailAndCropActivity.this);
            dialogUtil.setDialogCancelListener(DetailAndCropActivity.this);
            String message = StringUtil.getString(DetailAndCropActivity.this,
                                                  R.string.wallPaper_detailDialogMessage);
            dialogUtil.showLoadingDialogOutsideCancelable(message, 12, false);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            InputStream in = null;
            try {
                in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CropUtil.closeSilently(in);
            }
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (this.isCancelled()) {
                return;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            hasTaskRun = false;
            if (result == null) {
                mHandler.obtainMessage(NETERROR).sendToTarget();
            } else {
                if (dialogUtil != null) {
                    dialogUtil.cancelLoadingDialog();
                }
                mHandler.obtainMessage(NETSUCCESS).sendToTarget();
                beginCrop(null, result);
            }
        }
    }

    private class Cropper {

        //设置截图的正方形
        private void makeDefault() {
            if (rotateBitmap == null) {
                return;
            }

            int wallpaperWidth = DeviceUtils.getScreenPixelsWidth(DetailAndCropActivity.this);
            int wallpaperHeight = DeviceUtils.getRealScreenPixelsHeight(DetailAndCropActivity.this);
            //多屏壁纸情况
            if( !LauncherPreference.getWallpaperFixed()){
                wallpaperWidth = WallpaperUtil.WALLPAPER_ROLL_FACTOR * DeviceUtils.getScreenPixelsWidth(DetailAndCropActivity.this);
                wallpaperHeight = DeviceUtils.getRealScreenPixelsHeight(DetailAndCropActivity.this);
            }

            HighlightView hv = new HighlightView(imageView);
            final int width = rotateBitmap.getWidth();
            final int height = rotateBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            // Make the default size about 4/5 of the width or height
            //int cropWidth = Math.min(width, height) * 4 / 5;
            //控制最初的默认悬浮框的大小比例
            int cropWidth = Math.min(width, height);

            @SuppressWarnings("SuspiciousNameCombination")
            int cropHeight = cropWidth;

            if (wallpaperWidth != 0 && wallpaperHeight != 0) {
                float scaleX = (float) wallpaperWidth / width;
                float scaleY = (float) wallpaperHeight / height;
                if (scaleX >= scaleY) {
                    cropWidth = width;
                    cropHeight = (int) (wallpaperHeight / scaleX);
                } else {
                    cropHeight = height;
                    cropWidth = (int) (wallpaperWidth / scaleY);
                }

            }

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(imageView.getUnrotatedMatrix(), imageRect, cropRect,
                     wallpaperWidth != 0 && wallpaperHeight != 0);
            imageView.highlightViews.clear();
            imageView.add(hv);
        }

        public void crop() {
            mHandler.post(new Runnable() {
                public void run() {
                    makeDefault();
                    imageView.invalidate();
                    if (imageView.highlightViews.size() == 1) {
                        cropView = imageView.highlightViews.get(0);
                        cropView.setFocus(true);
                    }
                }
            });
        }
    }
}


