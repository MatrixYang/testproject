package com.cooeeui.wallpaper.model;

import java.util.ArrayList;
import java.util.List;

//每个tab对应的model
public class ListInfo {

    private String tabid;
    private String enname;
    private String cnname;
    private String twname;
    private String typeid;
    private List<ItemInfo> itemList = new ArrayList<ItemInfo>();

    public String getTabid() {
        return tabid;
    }

    public void setTabid(
        String tabid) {
        this.tabid = tabid;
    }

    public String getEnname() {
        return enname;
    }

    public void setEnname(
        String enname) {
        this.enname = enname;
    }

    public String getCnname() {
        return cnname;
    }

    public void setCnname(
        String cnname) {
        this.cnname = cnname;
    }

    public String getTwname() {
        return twname;
    }

    public void setTwname(
        String twname) {
        this.twname = twname;
    }

    public List<ItemInfo> getItemList() {
        return itemList;
    }

    public void setItemList(
        List<ItemInfo> itemList) {
        this.itemList = itemList;
    }

    public String getTypeid() {
        return typeid;
    }

    public void setTypeid(
        String typeid) {
        this.typeid = typeid;
    }

    @Override
    public String toString() {
        String
            sb =
            "tabid:" + tabid + ";enname" + enname + ";cnname" + ";twname" + twname + ";typeid"
            + typeid;
        return sb;
    }
}
