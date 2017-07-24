package com.cooeeui.wallpaper;

import android.widget.ListAdapter;

import com.cooeeui.wallpaper.model.ItemLayoutInfo;

import java.util.List;

public interface ItemAdapter extends ListAdapter {

    void appendItems(List<ItemLayoutInfo> newItems);

    void setItems(List<ItemLayoutInfo> moreItems);
}
