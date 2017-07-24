package com.cooeeui.brand.zenlauncher.wallpaper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.wallpaper.model.ItemInfo;
import com.cooeeui.brand.zenlauncher.wallpaper.model.ItemLayoutInfo;
import com.cooeeui.zenlauncher.R;
import com.facebook.ads.NativeAd;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.MvNativeHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

/**
 * Sample adapter implementation extending from AsymmetricGridViewAdapter<DemoItem> This is the
 * easiest way to get started.
 */
public class DefaultListAdapter extends ArrayAdapter<ItemLayoutInfo> implements ItemAdapter {

    private final LayoutInflater layoutInflater;
    private HashMap<String, SoftReference<Drawable>>
        imageCache;
    private Context mContext;
    //    private HashMap<Integer, NativeAd> nativeAdList = new HashMap<>();
    private HashMap<Integer, Object> nativeAdList = new HashMap<>();
    private MvNativeHandler nativeHandle;

    public DefaultListAdapter(Context context, List<ItemLayoutInfo> items) {
        super(context, 0, items);
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
        imageCache = new HashMap<>();
    }

    public DefaultListAdapter(Context context) {
        super(context, 0);
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
        imageCache = new HashMap<>();
    }

    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        View v = null;
        if (OnlineWallpaperActivity.allInfo == null) {
            return v;
        }
        ItemLayoutInfo item = getItem(position);
        boolean isRegular = item.getViewType() == 0;

        if (convertView == null) {
            if (isRegular) {
                v = layoutInflater.inflate(R.layout.adapter_item, parent, false);
            } else {
                v = layoutInflater.inflate(R.layout.ad_unit, parent, false);
            }
        } else {
            v = convertView;
        }
        if (isRegular) {
            final ImageView imageView = (ImageView) v.findViewById(R.id.imageview);
            final ItemInfo itemInfo = OnlineWallpaperActivity.allInfo.get(item.getPosition());
            if (imageCache.containsKey(itemInfo.getThumbimg())
                && imageCache.get(itemInfo.getThumbimg()).get() != null) {
                SoftReference<Drawable> softReference = imageCache.get(itemInfo.getThumbimg());
                Drawable drawable = softReference.get();
                imageView.setImageDrawable(drawable);
            } else {
                Picasso.with(mContext)
                    .load(itemInfo.getThumbimg())
                    .skipMemoryCache()
                    .error(R.drawable.wallpaper_default)
                    .placeholder(R.drawable.wallpaper_default)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (!imageCache.containsKey(itemInfo.getThumbimg())) {
                                imageCache.put(itemInfo.getThumbimg(),
                                               new SoftReference<Drawable>(
                                                   imageView.getDrawable()));
                            }
                        }

                        @Override
                        public void onError() {
                        }
                    });
            }
        } else {
            if (nativeAdList.get(position) instanceof NativeAd) {
                // facebook ad begin
                ImageView nativeAdCover = (ImageView) v.findViewById(R.id.native_ad_cover);
                ImageView nativeAdIcon = (ImageView) v.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = (TextView) v.findViewById(R.id.native_ad_title);
                TextView nativeAdBody = (TextView) v.findViewById(R.id.native_ad_body);
                Button
                    nativeAdCallToAction = (Button) v.findViewById(R.id.native_ad_call_to_action);

                // Setting the Text
                nativeAdCallToAction
                    .setText(((NativeAd) nativeAdList.get(position)).getAdCallToAction());
                nativeAdCallToAction.setVisibility(View.VISIBLE);
                nativeAdTitle.setText(((NativeAd) nativeAdList.get(position)).getAdTitle());
                nativeAdBody.setText(((NativeAd) nativeAdList.get(position)).getAdBody());

                // Downloading and setting the ad icon.
                NativeAd.Image adIcon = ((NativeAd) nativeAdList.get(position)).getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

                NativeAd.Image
                    adCoverImage =
                    ((NativeAd) nativeAdList.get(position)).getAdCoverImage();
                NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdCover);

                ((NativeAd) nativeAdList.get(position)).registerViewForInteraction(v);
                //facebook ad end
            } else if (nativeAdList.get(position) instanceof Campaign) {
                final ImageView nativeAdCover = (ImageView) v.findViewById(R.id.native_ad_cover);
                final ImageView nativeAdIcon = (ImageView) v.findViewById(R.id.native_ad_icon);
                final TextView nativeAdTitle = (TextView) v.findViewById(R.id.native_ad_title);
                final TextView nativeAdBody = (TextView) v.findViewById(R.id.native_ad_body);

                nativeAdBody.setText(((Campaign) nativeAdList.get(position)).getAppDesc());
                nativeAdTitle.setText(((Campaign) nativeAdList.get(position)).getAppName());

                //mobvista绑定点击事件
                nativeHandle.registerView(nativeAdCover, (Campaign) nativeAdList.get(position));
                final String urlIcon = ((Campaign) nativeAdList.get(position)).getIconUrl();
                if (imageCache.containsKey(urlIcon)
                    && imageCache.get(urlIcon).get() != null) {
                    SoftReference<Drawable> softReference = imageCache.get(urlIcon);
                    Drawable drawable = softReference.get();
                    nativeAdIcon.setImageDrawable(drawable);
                } else {
                    Picasso.with(mContext)
                        .load(urlIcon)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (!imageCache.containsKey(urlIcon)) {
                                    imageCache.put(urlIcon,
                                                   new SoftReference<Drawable>(
                                                       nativeAdIcon.getDrawable()));
                                }
                            }

                            @Override
                            public void onError() {
                            }
                        });
                }

                // Downloading and setting the ad icon.
                final String urlImage = ((Campaign) nativeAdList.get(position)).getImageUrl();
                if (imageCache.containsKey(urlImage)
                    && imageCache.get(urlImage).get() != null) {
                    SoftReference<Drawable> softReference = imageCache.get(urlImage);
                    Drawable drawable = softReference.get();
                    nativeAdCover.setImageDrawable(drawable);
                } else {
                    Picasso.with(mContext)
                        .load(urlImage)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdCover, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (!imageCache.containsKey(urlImage)) {
                                    imageCache.put(urlImage,
                                                   new SoftReference<Drawable>(
                                                       nativeAdCover.getDrawable()));
                                }
                            }

                            @Override
                            public void onError() {
                            }
                        });
                }
            }

//            final ImageView nativeAdCover = (ImageView) v.findViewById(R.id.native_ad_cover);
//            final ImageView nativeAdIcon = (ImageView) v.findViewById(R.id.native_ad_icon);
//            TextView nativeAdTitle = (TextView) v.findViewById(R.id.native_ad_title);
//            TextView nativeAdBody = (TextView) v.findViewById(R.id.native_ad_body);
//            final String url = nativeAdList.get(position).getIcon_gp();
//            if (imageCache.containsKey(url)
//                && imageCache.get(url).get() != null) {
//                SoftReference<Drawable> softReference = imageCache.get(url);
//                Drawable drawable = softReference.get();
//                nativeAdIcon.setImageDrawable(drawable);
//            } else {
//                Picasso.with(mContext)
//                    .load(url)
//                    .skipMemoryCache()
//                    .error(R.drawable.wallpaper_default)
//                    .placeholder(R.drawable.wallpaper_default)
//                    .into(nativeAdIcon, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            if (!imageCache.containsKey(url)) {
//                                imageCache.put(url,
//                                               new SoftReference<Drawable>(
//                                                   nativeAdIcon.getDrawable()));
//                            }
//                        }
//
//                        @Override
//                        public void onError() {
//                        }
//                    });
//            }
//
//            // Downloading and setting the ad icon.
//            final String url2 = nativeAdList.get(position).getCreatives().get("1200x627_0");
//            if (imageCache.containsKey(url2)
//                && imageCache.get(url2).get() != null) {
//                SoftReference<Drawable> softReference = imageCache.get(url2);
//                Drawable drawable = softReference.get();
//                nativeAdCover.setImageDrawable(drawable);
//            } else {
//                Picasso.with(mContext)
//                    .load(url2)
//                    .skipMemoryCache()
//                    .error(R.drawable.wallpaper_default)
//                    .placeholder(R.drawable.wallpaper_default)
//                    .into(nativeAdCover, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            if (!imageCache.containsKey(url2)) {
//                                imageCache.put(url2,
//                                               new SoftReference<Drawable>(
//                                                   nativeAdCover.getDrawable()));
//                            }
//                        }
//
//                        @Override
//                        public void onError() {
//                        }
//                    });
//            }
        }
        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? 1 : 0;
    }

    public void appendItems(List<ItemLayoutInfo> newItems) {
        addAll(newItems);
        notifyDataSetChanged();
    }

    public void setItems(List<ItemLayoutInfo> moreItems) {
        clear();
        appendItems(moreItems);
    }

    public void setNativeAd(int position, Object nativeAd) {
        this.nativeAdList.put(position, nativeAd);
    }

    public void setMobVistaNativeHandle(MvNativeHandler nativeHandle) {
        this.nativeHandle = nativeHandle;
    }
}
