package com.cooeeui.wallpaper.local;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.wallpaper.R;

public class WallPaperViewCache {

    private View baseView;
    private TextView textView;
    private ImageView imageView;

    public WallPaperViewCache(View baseView) {
        this.baseView = baseView;
    }

    public ImageView getImageView() {
        if (imageView == null) {
            imageView = (ImageView) baseView.findViewById(R.id.wallpaper_pic_item);
        }
        return imageView;
    }

}
