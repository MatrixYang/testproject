package com.cooee.localsearch.app;

import android.content.Context;
import android.content.pm.ActivityInfo;

import com.cooee.localsearch.base.ItemBase;
import com.cooee.t9search.model.PinyinUnit;
import com.cooee.t9search.util.PinyinUtil;

import java.util.ArrayList;
import java.util.List;

// 应用
public class ItemApp extends ItemBase {
    public ActivityInfo activityInfo;
    public String name;
    private List<PinyinUnit> dstUnit;
    public ItemApp(Context content, ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
        dstUnit = new ArrayList<PinyinUnit>();
        String title = activityInfo.loadLabel(content.getPackageManager()).toString();
        name = removeAllSpace(title);
        PinyinUtil.chineseStringToPinyinUnit(name, dstUnit);
    }
    /**
     * 去除空格
     */
    private String removeAllSpace(
        String str) {
        String tmpstr = str.replace(" ", "");
        return tmpstr;
    }
    @Override
    public boolean match(
        String text,
        List<PinyinUnit> srcUnit) {
        if (T9Match(name, srcUnit)) {
            return true;
        } else {
            return name.toLowerCase().contains(text.toLowerCase());
        }
    }
}
