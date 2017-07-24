package com.cooeeui.brand.zenlauncher.addingapp;

import com.cooeeui.brand.zenlauncher.android.adapter.ObjectEntity;

import java.util.List;

public class ApplicationSection extends ObjectEntity {

    //
    private String mTitle;

    private int mSectionCount;

    //
    private List<ApplicationSimpleInfo> mApplications;

    private String mLetter;

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getmSectionCount() {
        return mSectionCount;
    }

    public void setmSectionCount(int mSectionCount) {
        this.mSectionCount = mSectionCount;
    }

    public List<ApplicationSimpleInfo> getmApplications() {
        return mApplications;
    }

    public void setmApplications(List<ApplicationSimpleInfo> mApplications) {
        this.mApplications = mApplications;
    }

    public String getmLetter() {
        return mLetter;
    }

    public void setmLetter(String mLetter) {
        this.mLetter = mLetter;
    }

}
