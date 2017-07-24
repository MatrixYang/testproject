package com.cooeeui.brand.zenlauncher.wallpaper;

import com.cooeeui.brand.zenlauncher.wallpaper.model.ItemLayoutInfo;

import java.util.ArrayList;
import java.util.List;

final class ItemUtils {

    int currentOffset;

    ItemUtils() {
    }

    public List<ItemLayoutInfo> moarItems(int qty,int num,ArrayList<Integer> adIndex) {
        List<ItemLayoutInfo> items = new ArrayList<>();
        int adNum = 0;
        for (int i = 0; i < qty; i++) {
            int colSpan;
            if (((i - adNum) / 3) % 2 == 0) {
                if ((i - adNum) % 3 == 0) {
                    colSpan = 2;
                } else {
                    colSpan = 1;
                }
            } else {
                if ((i - adNum) % 3 == 2) {
                    colSpan = 2;
                } else {
                    colSpan = 1;
                }
            }
            float rowSpan = colSpan;
            ItemLayoutInfo item;
            if (num >= 0 && adIndex != null && adIndex.size() == num){
                item = new ItemLayoutInfo(colSpan, rowSpan, currentOffset + i, 0);
                for (int j = 0; j < num; j++){
                    if (i == adIndex.get(j)) {
                        rowSpan = 1.5f;
                        colSpan = 3;
                        adNum += 1;
                        item = new ItemLayoutInfo(colSpan, rowSpan, currentOffset + i, 1);
                    }
                }
            }else{
                item = new ItemLayoutInfo(colSpan, rowSpan, currentOffset + i, 0);
            }
            items.add(item);
        }
        return items;
    }
}
