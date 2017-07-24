package com.cooee.localsearch.base;

import com.cooee.t9search.model.PinyinUnit;
import com.cooee.t9search.model.T9PinyinUnit;
import com.cooee.t9search.util.PinyinUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuiqian on 2016/2/1.
 */
public abstract class ItemBase {

    protected boolean match(
        String text,
        List<PinyinUnit> srcUnit) {
        return false;
    }

    protected boolean T9Match(
        String name,
        List<PinyinUnit> srcUnit) {
        PinyinUnit src = srcUnit.get(0);
        if (src.isPinyin()) {
            return false;
        }
        String srcNumber = src.getT9PinyinUnitIndex().get(0).getNumber();
        String srcPY = src.getT9PinyinUnitIndex().get(0).getPinyin().toLowerCase();

        List<PinyinUnit> appSrcUnit = new ArrayList<PinyinUnit>();
        PinyinUtil.chineseStringToPinyinUnit(name, appSrcUnit);
        if (srcNumber.length() > appSrcUnit.size()) {
            return false;
        }
        for (int i = 0; i < srcNumber.length(); i++) {
            char number = srcNumber.charAt(i);
            char py = srcPY.charAt(i);
            T9PinyinUnit
                appT9Unit = appSrcUnit.get(i).getT9PinyinUnitIndex().get(0);
            char appNumber = appT9Unit.getNumber().charAt(0);
            char appPY = appT9Unit.getPinyin().charAt(0);
            if (number == py) {
                //输入的是数字，只需要比较number
                if (appNumber != number) {
                    return false;
                }
            } else {
                if (appNumber != number || appPY != py) {
                    return false;
                }
            }
        }
        return true;
    }
}
