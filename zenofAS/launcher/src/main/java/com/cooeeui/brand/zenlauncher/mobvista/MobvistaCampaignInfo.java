package com.cooeeui.brand.zenlauncher.mobvista;

import android.graphics.Bitmap;

import com.mobvista.msdk.out.Campaign;

/**
 * Created by cuiqian on 2016/4/6.
 */
public class MobvistaCampaignInfo{

    /**
     * 广告是否有效
     */
    private boolean isValid = true;
    /**
     * 广告被成功加载，主要是Banner图片和Icon图片
     */
    private boolean isLoaded = false;

    private Campaign mCampaign = null;
    private Bitmap mBannerBitmap = null;
    private Bitmap mIconBitmap = null;

    public void setCampaign(Campaign campaign){
        mCampaign = campaign;
    }
    public Campaign getCampaign(){
        return mCampaign;
    }
    public void setBannerBitmap(Bitmap bannerBitmap){
        mBannerBitmap = bannerBitmap;
    }
    public Bitmap getBannerBitmap(){
        return mBannerBitmap;
    }
    public void setIconBitmap(Bitmap bannerBitmap){
        mIconBitmap = bannerBitmap;
    }
    public Bitmap getIconBitmap(){
        return mIconBitmap;
    }
    public void setValid(boolean valid){
        isValid = valid;
    }
    public boolean isValid(){
        return isValid;
    }

    public void setLoaded(boolean loaded){
        isLoaded = loaded;
    }

    public boolean isLoaded(){
        return isLoaded;
    }

    public void release(){
        mBannerBitmap = null;
        mIconBitmap = null;
    }
}
