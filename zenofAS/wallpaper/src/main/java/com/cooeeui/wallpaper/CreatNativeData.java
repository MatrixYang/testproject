package com.cooeeui.wallpaper;

import android.util.Log;

import com.kmob.kmobsdk.NativeAdData;

import org.json.JSONObject;

public class CreatNativeData {

    /**
     * 通过广告传入的数据生成一个NativeAdData
     */
    public static NativeData createNativeData(JSONObject object) {
        String summary = "";
        String headline = "";
        String adcategory = "";
        String appRating = "";
        String adlogo = "";
        String details = "";
        String adlogoWidth = "";
        String adlogoHeight = "";
        String review = "";
        String appinstalls = "";
        String download = "";
        String adplaceid = "";
        String adid = "";
        String clickurl = "";
        String interactiontype = "";
        String open_type = "";
        String hurl = "";
        String hdetailurl = "";
        String pkgname = "";
        String appsize = "";
        String version = "";
        String versionname = "";
        String ctimg = "";
        String hiimg = "";
        String click_record_url = "";
        try {
            if (object.has(NativeAdData.SUMMARY_TAG)) {
                summary = object.getString(NativeAdData.SUMMARY_TAG);
            }
            if (object.has(NativeAdData.HEADLINE_TAG)) {
                headline = object.getString(NativeAdData.HEADLINE_TAG);
            }
            if (object.has(NativeAdData.ADCATEGORY_TAG)) {
                adcategory = object.getString(NativeAdData.ADCATEGORY_TAG);
            }
            if (object.has(NativeAdData.APPRATING_TAG)) {
                appRating = object.getString(NativeAdData.APPRATING_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_TAG)) {
                adlogo = object.getString(NativeAdData.ADLOGO_TAG);
            }
            if (object.has(NativeAdData.DETAILS_TAG)) {
                details = object.getString(NativeAdData.DETAILS_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_WIDTH_TAG)) {
                adlogoWidth = object.getString(NativeAdData.ADLOGO_WIDTH_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_HEIGHT_TAG)) {
                adlogoHeight = object.getString(NativeAdData.ADLOGO_HEIGHT_TAG);
            }
            if (object.has(NativeAdData.REVIEW_TAG)) {
                review = object.getString(NativeAdData.REVIEW_TAG);
            }
            if (object.has(NativeAdData.APPINSTALLS_TAG)) {
                appinstalls = object.getString(NativeAdData.APPINSTALLS_TAG);
            }
            if (object.has(NativeAdData.DOWNLOAD_TAG)) {
                download = object.getString(NativeAdData.DOWNLOAD_TAG);
            }
            if (object.has(NativeAdData.ADPLACE_ID_TAG)) {
                adplaceid = object.getString(NativeAdData.ADPLACE_ID_TAG);
            }
            if (object.has(NativeAdData.AD_ID_TAG)) {
                adid = object.getString(NativeAdData.AD_ID_TAG);
            }
            if (object.has(NativeAdData.CLICKURL_TAG)) {
                clickurl = object.getString(NativeAdData.CLICKURL_TAG);
            }
            if (object.has(NativeAdData.INTERACTION_TYPE_TAG)) {
                interactiontype = object
                    .getString(NativeAdData.INTERACTION_TYPE_TAG);
            }
            if (object.has(NativeAdData.OPEN_TYPE_TAG)) {
                open_type = object.getString(NativeAdData.OPEN_TYPE_TAG);
            }
            if (object.has(NativeAdData.HURL_TAG)) {
                hurl = object.getString(NativeAdData.HURL_TAG);
            }
            if (object.has(NativeAdData.HDETAILURL_TAG)) {
                hdetailurl = object.getString(NativeAdData.HDETAILURL_TAG);
            }
            if (object.has(NativeAdData.PKGNAME_TAG)) {
                pkgname = object.getString(NativeAdData.PKGNAME_TAG);
            }
            if (object.has(NativeAdData.APPSIZE_TAG)) {
                appsize = object.getString(NativeAdData.APPSIZE_TAG);
            }
            if (object.has(NativeAdData.VERSION_TAG)) {
                version = object.getString(NativeAdData.VERSION_TAG);
            }
            if (object.has(NativeAdData.VERSIONNAME_TAG)) {
                versionname = object.getString(NativeAdData.VERSIONNAME_TAG);
            }
            if (object.has(NativeAdData.CTIMG_TAG)) {
                ctimg = object.getString(NativeAdData.CTIMG_TAG);
            }
            if (object.has(NativeAdData.HIIMG_TAG)) {
                hiimg = object.getString(NativeAdData.HIIMG_TAG);
            }
            if (object.has(NativeAdData.CLICK_RECORD_URL_TAG)) {
                click_record_url = object
                    .getString(NativeAdData.CLICK_RECORD_URL_TAG);
            }
            return new NativeData(summary, headline, adcategory, appRating,
                                  adlogo, details, adlogoWidth, adlogoHeight, review,
                                  appinstalls, download, adplaceid, adid, clickurl,
                                  interactiontype, open_type, hurl, hdetailurl, pkgname,
                                  appsize, version, versionname, ctimg, hiimg,
                                  click_record_url);
        } catch (Exception e) {
            Log.e("KMOB", "addAdView e " + e.toString());
        }
        return null;
    }
}
