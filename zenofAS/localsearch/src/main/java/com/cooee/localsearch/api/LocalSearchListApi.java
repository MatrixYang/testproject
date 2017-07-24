package com.cooee.localsearch.api;
import android.content.Context;
import com.cooee.localsearch.base.ItemBase;
import com.cooee.localsearch.base.LocalSearchListImpl;

import java.util.Stack;

/**
 * Created by cuiqian on 2016/2/1.
 */
public class LocalSearchListApi extends LocalSearchListImpl {

    public LocalSearchListApi(Context context){
       super(context);
    }

    public synchronized Stack<ItemBase> getAppList(String text){
        return super.getAppList(text);
    }

    public synchronized Stack<ItemBase> getContactsList(String text){
        return super.getContactsList(text);
    }

    public synchronized Stack<ItemBase> getMusicList(String text){
        return super.getMusicList(text);
    }

    public void loadContent() {
        super.loadContent();
    }
    public void initContent(){
        super.initContent();
    }
    public void onDestroy() {
        super.onDestroy();
    }
}
