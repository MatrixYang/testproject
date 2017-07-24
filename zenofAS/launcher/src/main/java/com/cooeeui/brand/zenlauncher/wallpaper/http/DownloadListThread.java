package com.cooeeui.brand.zenlauncher.wallpaper.http;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.cooeeui.basecore.utilities.DateUtil;
import com.cooeeui.brand.zenlauncher.wallpaper.model.ItemInfo;
import com.cooeeui.brand.zenlauncher.wallpaper.model.ListInfo;
import com.cooeeui.brand.zenlauncher.wallpaper.util.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 下载数据所用的线程
 *
 * Created by Steve on 2015/7/21.
 */
public class DownloadListThread extends Thread {

    private static final String ACTION_LIST = "1300";
    private final String
        SERVER_URL =
        "http://uifolder.coolauncher.com.cn/iloong/pui/ServicesEngine/DataService";
    private Object syncObject = new Object();
    private volatile boolean isExit = false;
    private Context mContext;
    private List<ListInfo> info = new ArrayList<ListInfo>();
    private LoadSuccessListener listener;

    public DownloadListThread(Context context) {
        this.mContext = context;
    }

    public void setListener(LoadSuccessListener listener) {
        this.listener = listener;
    }

    public void stopRun() {
        isExit = true;
    }

    @Override
    public void run() {
        String today = DateUtil.getNowDate();
        String res = "";
        //标识是否解析成功
        boolean isSucceed = false;
        String tempRes = PreferencesUtils.getString(mContext, today, "");
        if (TextUtils.isEmpty(tempRes)) {
            //clear the content
            PreferencesUtils.clearAll(mContext);
            //没有缓存进行网络请求
            String params = HttpParamsUtil.getParams(mContext, ACTION_LIST, true);
            if (params != null) {
                //http请求数据
                try {
                    res = new CustomHttpURLConnection().post(SERVER_URL, params);
                    PreferencesUtils.putString(mContext, today, res);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isExit) {
                    synchronized (syncObject) {
                        return;
                    }
                }
            }
        } else {
            res = tempRes;
        }
        Log.d("wallpaperRES", res);
        if (res != null) {
            String content = res;
            JSONObject json = null;
            //解析返回的json文本内容
            try {
                json = new JSONObject(content);
                int retCode = json.getInt("retcode");
                //判断是否有数据
                if (retCode == 0) {
                    //获取列表
                    String reslist = json.getString("reslist");
                    json = new JSONObject(reslist);
                    //遍历所有数据节点
                    for (Iterator<String> keys = json.keys(); keys.hasNext(); ) {
                        String key = (String) keys.next();
                        //只提取壁纸
                        if (!key.equals("1")) {
                            continue;
                        }
                        //每一个tab类型的内容
                        JSONObject tmJson = (JSONObject) json.get(key);
                        ListInfo listInfo = new ListInfo();
                        ArrayList<Integer> indexList = new ArrayList();
                        //遍历tab下的所有内容，每个tab内容对应一个ListInfo对象
                        for (Iterator<String> mIterator = tmJson.keys();
                             mIterator.hasNext(); ) {
                            String mKey = (String) mIterator.next();
                            if (mKey.equals("tabid")) {
                                listInfo.setTabid(tmJson.get(mKey).toString());
                            } else if (mKey.equals("enname")) {
                                listInfo.setEnname(tmJson.get(mKey).toString());
                            } else if (mKey.equals("cnname")) {
                                listInfo.setCnname(tmJson.get(mKey).toString());
                            } else if (mKey.equals("twname")) {
                                listInfo.setTwname(tmJson.get(mKey).toString());
                            } else if (mKey.equals("typeid")) {
                                listInfo.setTypeid(tmJson.get(mKey).toString());
                            } else {
                                //每个tab下的资源元素对应一个ItemInfo对象，里面包含了图片的名称以及路径等内容
                                JSONObject jsObj = (JSONObject) tmJson.get(mKey);
                                ItemInfo itemInfo = new ItemInfo();
                                itemInfo.setIndex(mKey);
                                itemInfo.setResid(jsObj.getString("resid"));
                                itemInfo.setEnname(jsObj.getString("enname"));
                                itemInfo.setCnname(jsObj.getString("cnname"));
                                // itemInfo.setTwname(jsObj.getString("twname"));
                                itemInfo.setResurl(jsObj.getString("resurl"));
                                itemInfo.setPackname(jsObj.getString("packname"));
                                itemInfo.setSize(jsObj.getString("size"));
                                itemInfo.setAuthor(jsObj.getString("author"));
                                itemInfo.setAboutchinese(jsObj.getString("aboutchinese"));
                                itemInfo.setVersion(jsObj.getString("version"));
                                itemInfo.setVersionname(jsObj.getString("versionname"));
                                itemInfo.setAboutenglish(jsObj.getString("aboutenglish"));
                                itemInfo.setPrice(jsObj.getString("price"));
                                itemInfo.setPricedetail(jsObj.getString("pricedetail"));
                                itemInfo.setPricePoint(jsObj.getString("pricepoint"));
                                itemInfo.setIcon(jsObj.getString("icon"));
                                itemInfo.setThumbimg(jsObj.getString("thumbimg"));
                                try {
                                    itemInfo.setEnginepackname(
                                        jsObj.getString("enginepackname"));
                                    itemInfo.setEngineurl(jsObj.getString("engineurl"));
                                    itemInfo.setEnginesize(jsObj.getString("enginesize"));
                                } catch (JSONException e) {
                                    itemInfo.setEnginepackname(null);
                                    itemInfo.setEngineurl(null);
                                    itemInfo.setEnginesize(null);
                                }
                                try {
                                    itemInfo.setEnginedesc(jsObj.getString("enginedesc"));
                                } catch (JSONException e) {
                                    itemInfo.setEnginedesc(null);
                                }
                                try {
                                    itemInfo.setThirdparty(jsObj.getString("thirdparty"));
                                } catch (JSONException e) {
                                    itemInfo.setThirdparty(null);
                                }
                                try {
                                    JSONArray preview = jsObj.getJSONArray("previewlist");
                                    String[] pre = new String[preview.length()];
                                    for (int k = 0; k < preview.length(); k++) {
                                        pre[k] = preview.getString(k);
                                    }
                                    itemInfo.setPreviewlist(pre);
                                } catch (JSONException e) {
                                    itemInfo.setPreviewlist(new String[]{
                                        itemInfo.getThumbimg()
                                    });
                                }

                                if (Build.VERSION.SDK_INT < 21) {
                                    int keyNum = Integer.parseInt(mKey);
                                    int currIndex = calculatePosition(keyNum, indexList);
                                    listInfo.getItemList().add(currIndex, itemInfo);
                                    indexList.add(currIndex, keyNum);
                                }else{
                                    listInfo.getItemList().add(itemInfo);
                                }
                            }
                        }
                        info.add(listInfo);
                    }
                    isSucceed = true;
                }else{
                    PreferencesUtils.putString(mContext, today, "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            isSucceed = false;
        }
        //反馈结果
        if (isSucceed) {
            listener.onSuccess(info);
        } else {
            listener.onFailed();
        }
    }

    public interface LoadSuccessListener {

        public void onSuccess(List<ListInfo> info);

        public void onFailed();
    }

    private int calculatePosition(int curr, ArrayList<Integer> list) {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            if (curr > list.get(i)) {
                index = i + 1;
            } else {
                return index;
            }
        }
        return index;
    }
}

