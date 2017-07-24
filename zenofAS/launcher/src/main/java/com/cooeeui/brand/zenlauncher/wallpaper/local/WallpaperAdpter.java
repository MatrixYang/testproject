package com.cooeeui.brand.zenlauncher.wallpaper.local;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cooeeui.zenlauncher.R;

import java.util.List;


/**
 * Created by xingwang lee on 2015/8/7.
 */
public class WallpaperAdpter extends BaseAdapter {

    private static String TAG = "diaoliang";
    private static int URLCOUNT = 0;
    private Context context;
    private List<WallPaperImage> wallpaperUris;
    private GridView gridView;
    private AsyncImageLoader asyncImageLoader;

    public WallpaperAdpter(Context context, List<WallPaperImage> wallpaperUris, GridView gridView) {
        this.context = context;
        this.wallpaperUris = wallpaperUris;
        asyncImageLoader = new AsyncImageLoader();
        this.gridView = gridView;
    }

    @Override
    public int getCount() {
        return wallpaperUris.size();
    }

    @Override
    public Object getItem(int position) {
        return wallpaperUris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the views from XML
        View rowView = convertView;
        WallPaperViewCache viewCache;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.wallpaper_item, null);
            viewCache = new WallPaperViewCache(rowView);
            rowView.setTag(viewCache);
        } else {
            viewCache = (WallPaperViewCache) rowView.getTag();
        }
        WallPaperImage imageAndText = (WallPaperImage) getItem(position);

        // Load the image and set it on the ImageView
        String imageUrl = imageAndText.getImageUrl();
        ImageView imageView = viewCache.getImageView();

        String Tag = imageUrl + URLCOUNT;
        imageView.setTag(Tag);
        URLCOUNT++;
        Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl, Tag,
                                                             new AsyncImageLoader.ImageCallback() {
                                                                 public void imageLoaded(
                                                                     Drawable imageDrawable,
                                                                     String imageUrl, String Tag) {
                                                                     ImageView
                                                                         imageViewByTag =
                                                                         (ImageView) gridView
                                                                             .findViewWithTag(Tag);
                                                                     if (imageViewByTag != null) {
                                                                         imageViewByTag
                                                                             .setScaleType(
                                                                                 ImageView.ScaleType.CENTER_CROP);
                                                                         imageViewByTag
                                                                             .setImageDrawable(
                                                                                 imageDrawable);
                                                                     }
                                                                 }
                                                             });
        if (cachedImage == null) {
            imageView.setBackgroundResource(R.drawable.wallpaper_default);
        } else {
            imageView.setImageDrawable(cachedImage);
        }
        return rowView;
    }

}
