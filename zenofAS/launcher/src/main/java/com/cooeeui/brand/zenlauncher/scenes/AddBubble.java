package com.cooeeui.brand.zenlauncher.scenes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.addingapp.ApplicationSection;
import com.cooeeui.brand.zenlauncher.addingapp.ApplicationSimpleInfo;
import com.cooeeui.brand.zenlauncher.android.adapter.PinnedHeaderListAdapter;
import com.cooeeui.brand.zenlauncher.android.adapter.PinnedHeaderListAdapter.OnGridViewSelecte;
import com.cooeeui.brand.zenlauncher.android.view.LetterListView;
import com.cooeeui.brand.zenlauncher.android.view.LetterListView.OnTouchingLetterChangedListener;
import com.cooeeui.brand.zenlauncher.android.view.PinnedHeaderListView;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.utils.FirstLetterMapper;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AddBubble extends Dialog {

    public static final float DIALOG_RATIO = 0.8f;

    private Context mContext;
    private Launcher mLauncher;
    private ArrayList<AppInfo> mApps;

    private ArrayList<ApplicationSection> mApplicationSections;
    private PinnedHeaderListView mPinnedHeaderListView;
    private PinnedHeaderListAdapter mPinnedHeaderAdapter;
    private TreeMap<String, ApplicationSection> orderedMap;
    private Map<String, ApplicationSection>
        mAppSections =
        new LinkedHashMap<String, ApplicationSection>();
    private String[] mLetters;
    private LetterListView mLetterList;
    private ImageView image;
    private TextView text;
    private AppInfo mSelectedInfo;

    public AddBubble(Context context) {
        super(context, R.style.DialogStyle);
        mContext = context;
        mLauncher = (Launcher) context;
        mApps = copyList(mLauncher.getApps());
    }

    public ArrayList<AppInfo> copyList(List<AppInfo> list) {
        ArrayList<AppInfo> arraylist = new ArrayList<AppInfo>();
        for (int i = 0; i < list.size(); i++) {
            arraylist.add(list.get(i));
        }
        return arraylist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bubble);
        applications();
        mPinnedHeaderListView = (PinnedHeaderListView)
            this.findViewById(R.id.pinned_listview);
        mPinnedHeaderAdapter = new PinnedHeaderListAdapter(this.getContext(),
                                                           mApplicationSections);
        mPinnedHeaderListView.setAdapter(mPinnedHeaderAdapter);
        mPinnedHeaderListView.setPinnedHeaderView(this.getLayoutInflater().inflate(
            R.layout.include_pinnedheader, mPinnedHeaderListView, false));
        mPinnedHeaderListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("letterchange", "v:" + view.getId());

            }
        });
        mLetterList = (LetterListView)
            this.findViewById(R.id.app_letter_index);
        mPinnedHeaderAdapter.setOnGridViewSelecte(OnGridViewSelecteImpl);
        mLetterList.setOnTouchingLetterChangedListener(new
                                                           OnTouchingLetterChangedListener() {

                                                               @Override
                                                               public void onTouchingLetterChanged(
                                                                   String s) {

                                                                   int pos = getIndexForSection(s);
                                                                   if (pos != -1) {
                                                                       mPinnedHeaderListView
                                                                           .setSelection(pos);
                                                                   }
                                                               }
                                                           });

        int width = DeviceUtils.getScreenPixelsWidth(mContext);
        int height = DeviceUtils.getScreenPixelsHeight(mContext);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (width * DIALOG_RATIO);
        params.height = (int) (height * DIALOG_RATIO);
        window.setAttributes(params);

        image = (ImageView) findViewById(R.id.select_image);
        text = (TextView) findViewById(R.id.select_text);
        TextView mText = (TextView) findViewById(R.id.title_text);

        Button btn = (Button) findViewById(R.id.ok);
        btn.setText(StringUtil.getString(mContext, R.string.change_app_icon_ok));

        mText.setText(StringUtil.getString(mContext, R.string.add));

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelectedInfo != null) {
                    mLauncher.getSpeedDial().addBubbleView(mSelectedInfo.makeShortcut());
                    mLauncher.getSpeedDial().update();
                }
                AddBubble.this.dismiss();
            }
        });

    }

    OnGridViewSelecte OnGridViewSelecteImpl = new OnGridViewSelecte() {

        @Override
        public void OnItemSelected(Bitmap bitmap, String title) {
            int position = 0;
            for (int i = 0; i < mApps.size(); i++) {
                if (mApps.get(i).iconBitmap == bitmap) {
                    position = i;
                    break;
                }
            }
            image.setImageBitmap(mApps.get(position).iconBitmap);
            text.setText(mApps.get(position).title);

            mSelectedInfo = mApps.get(position);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (mPinnedHeaderListView != null) {
            mLetterList.setLetters(mLetters, 10);
        }

    }

    private void applications() {

        for (int i = 0; i < mApps.size(); i++) {
            String letter = FirstLetterMapper.getFirstLetter(mApps.get(i).title.toString());
            String l = letter.toUpperCase();

            ApplicationSimpleInfo app = new ApplicationSimpleInfo(mApps.get(i));
            ApplicationSection sec;
            if (mAppSections.containsKey(l)) {
                sec = mAppSections.get(l);
                sec.getmApplications().add(app);
                sec.setmSectionCount(sec.getmApplications().size());
            } else {
                List<ApplicationSimpleInfo> appList = new ArrayList<ApplicationSimpleInfo>();
                appList.add(app);
                sec = new ApplicationSection();
                sec.setmApplications(appList);
                sec.setmSectionCount(sec.getmSectionCount());
                sec.setmLetter(l);
                mAppSections.put(l, sec);
            }
        }

        // 这一步仅仅是为了得到一个排序号的HashMap
        orderedMap = new TreeMap(mAppSections);

        mApplicationSections = new ArrayList<ApplicationSection>();
        mLetters = new String[orderedMap.size()];
        Iterator iterator = orderedMap.keySet().iterator();
        int index = 0;
        while (iterator.hasNext()) {

            ApplicationSection appSec = orderedMap.get(iterator.next());
            mApplicationSections.add(appSec);
            mLetters[index] = appSec.getmLetter();
            index++;
        }

    }

    public int getIndexForSection(String letter) {

        int pos = -1;
        for (int i = 0; i < mLetters.length; i++) {

            if (mLetters[i].equals(letter)) {
                pos = i;

                return pos;
            }
        }

        return pos;
    }

}
