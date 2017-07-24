package com.cooeeui.wallpaper.local;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooeeui.wallpaper.DetailAndCropActivity;
import com.cooeeui.wallpaper.R;
import com.cooeeui.wallpaper.WallpaperUtil;
import com.umeng.message.PushAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class WallPaperLocalActivity extends Activity implements View.OnClickListener {

    private static final int PICK_FROM_FILE = 1;
    private GridView mGridView;
    private ArrayList<String> filePaths;
    private WallpaperAdpter wallpaperAdpter;
    private LinearLayout llGallery;
    private LinearLayout llLiveWallPaper;
    private ImageView imgWallpaperBack;
    private Uri mImageCaptureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_local);
        initView();
        initData();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent
                    intent =
                    new Intent(WallPaperLocalActivity.this, DetailAndCropActivity.class);
                Uri uri = Uri.fromFile(new File(filePaths.get(position)));
                intent.setData(uri);
                startActivity(intent);
            }
        });

        PushAgent.getInstance(this).onAppStart();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.wallpaper_gridview);
        llGallery = (LinearLayout) findViewById(R.id.ll_wallpaper_gallery);
        llLiveWallPaper = (LinearLayout) findViewById(R.id.ll_wallpaper_livewallpaper);
        imgWallpaperBack = (ImageView) findViewById(R.id.wallpaper_back);
        llGallery.setOnClickListener(this);
        llLiveWallPaper.setOnClickListener(this);
        imgWallpaperBack.setOnClickListener(this);
    }

    private void initData() {
        if (WallpaperUtil.isSdCardExist()) {
            filePaths = new ArrayList<String>();
            File skRoot = Environment.getExternalStorageDirectory();
            File file = new File(skRoot.getPath() + WallpaperUtil.WALLPAPER_STORAGE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }

            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    filePaths.add(files[i].getAbsolutePath());
                }
            }
            //实力化几个ImageAndText 对象
            List<WallPaperImage> listImageAndText = new ArrayList<WallPaperImage>();
            for (int i = 0; i < filePaths.size(); i++) {
                WallPaperImage iat = new WallPaperImage(filePaths.get(i));
                listImageAndText.add(iat);
            }
            wallpaperAdpter = new WallpaperAdpter(this, listImageAndText, mGridView);
            mGridView.setAdapter(wallpaperAdpter);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_wallpaper_gallery:
                Intent intent = new Intent();
                //act=android.intent.action.PICK dat=content://media/external/images/media typ=image/*
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"),
                                       PICK_FROM_FILE);
                break;
            case R.id.ll_wallpaper_livewallpaper:
                final Intent
                    liveWallpaper =
                    new Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER");
                startActivitySafely(liveWallpaper);
                break;
            case R.id.wallpaper_back:
                finish();
                break;
        }
    }

    public boolean startActivitySafely(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {

        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_FILE:
                mImageCaptureUri = data.getData();
                Intent
                    intent =
                    new Intent(WallPaperLocalActivity.this, DetailAndCropActivity.class);
                intent.setData(mImageCaptureUri);
                startActivity(intent);
                //这里不能finish，不然系统会收回对URI的权限。
            default:
                break;
        }
    }


}
