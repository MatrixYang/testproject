package com.cooeeui.brand.zenlauncher.wallpaper.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cooeeui.wallpaper.flowlib.AsymmetricItem;

public class ItemLayoutInfo implements AsymmetricItem {

    private int columnSpan;
    private float rowSpan;
    private int position;
    private int viewType;

    public ItemLayoutInfo() {
        this(1, 1, 0, 0);
    }

    public ItemLayoutInfo(int columnSpan, float rowSpan, int position, int viewType) {
        this.columnSpan = columnSpan;
        this.rowSpan = rowSpan;
        this.position = position;
        this.viewType = viewType;
    }

    public ItemLayoutInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int getColumnSpan() {
        return columnSpan;
    }

    @Override
    public float getRowSpan() {
        return rowSpan;
    }

    public int getPosition() {
        return position;
    }

    public int getViewType() {
        return viewType;
    }

    @Override
    public String toString() {
        return String.format("%s: %sx%s;%s", position, rowSpan, columnSpan, viewType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        columnSpan = in.readInt();
        rowSpan = in.readFloat();
        position = in.readInt();
        viewType = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(columnSpan);
        dest.writeFloat(rowSpan);
        dest.writeInt(position);
        dest.writeInt(viewType);
    }

    /* Parcelable interface implementation */
    public static final Parcelable.Creator<ItemLayoutInfo> CREATOR = new Parcelable.Creator<ItemLayoutInfo>() {

        @Override
        public ItemLayoutInfo createFromParcel(@NonNull Parcel in) {
            return new ItemLayoutInfo(in);
        }

        @Override
        @NonNull
        public ItemLayoutInfo[] newArray(int size) {
            return new ItemLayoutInfo[size];
        }
    };
}
