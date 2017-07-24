package com.cooeeui.brand.zenlauncher.localsearch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.localsearch.app.ItemApp;
import com.cooee.localsearch.base.ItemBase;
import com.cooee.localsearch.contacts.Contacts;
import com.cooee.localsearch.contacts.ItemContacts;
import com.cooee.localsearch.music.ItemMusic;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by cuiqian on 2016/1/26.
 */
public class SearchListAdapter extends BaseAdapter {

    private Context mContext;
    private InputMethodManager imm;
    private LayoutInflater mInflater;
    // 当前
    Stack<MyItemBase> itemList = new Stack<MyItemBase>();
    int itemListCnt;
    CharSequence mSearchText = "";

    public SearchListAdapter(Context context) {
        mContext = context;
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void onClick(
        int index) {
        itemList.get(index).onClickItem();
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public boolean isEnabled(
        int position) {
        return itemList.get(position).isEnabled();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(
        int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(
        int position) {
        return position;
    }

    @Override
    public View getView(
        int position,
        View convertView,
        ViewGroup parent) {
        MyItemBase item = itemList.get(position);
        if (item instanceof MyItemApp) {

        }
        return item.getConvertView(parent);
    }

    public void setSearchText(CharSequence s) {
        mSearchText = s;
    }

    public void updateAppList(Stack<ItemBase> appList) {
        // 应用
        String text = StringUtil.getString(mContext, R.string.kuso_local_app_name);
        itemList.add(new MyItemText(text));//
        List<AppInfo> appInfos = new ArrayList<>();
        for (ItemBase app : appList) {
            if (app instanceof ItemApp) {
                ItemApp itemApp = (ItemApp) app;
                AppInfo appInfo = new AppInfo();
                appInfo.activityInfo = itemApp.activityInfo;
                appInfo.name = itemApp.name;
                appInfos.add(appInfo);
            }
        }

        if (appInfos.size() > 0) {
            MyItemApp myItemApp = new MyItemApp(appInfos);
            itemList.add(myItemApp);
            itemList.add(new MyItemDivider());
        }

        if (itemList.peek() instanceof MyItemDivider) {
            itemList.pop();
        }
        if (itemList.peek() instanceof MyItemText) {
            itemList.pop();
        }

        itemListCnt = itemList.size();
    }

    public void updateContactsList(Stack<ItemBase> contactsList) {
        // 联系人
        String text = StringUtil.getString(mContext, R.string.kuso_local_contact_name);
        itemList.add(new MyItemText(text));
        for (ItemBase contact : contactsList) {
            if (contact instanceof ItemContacts) {
                ItemContacts itemContacts = (ItemContacts) contact;
                MyItemContacts myItemApp = new MyItemContacts(itemContacts.contact);
                itemList.add(myItemApp);
                itemList.add(new MyItemDivider());
            }

        }
        if (itemList.peek() instanceof MyItemDivider) {
            itemList.pop();
        }
        if (itemList.peek() instanceof MyItemText) {
            itemList.pop();
        }
        itemListCnt = itemList.size();
    }

    public void updateMusicList(Stack<ItemBase> musicList) {
        // 音乐
        String text = StringUtil.getString(mContext, R.string.kuso_local_music_name);
        itemList.add(new MyItemText(text));
        for (ItemBase music : musicList) {
            if (music instanceof ItemMusic) {
                ItemMusic itemMusic = (ItemMusic) music;
                MyItemMusic
                    myItemMusic =
                    new MyItemMusic(itemMusic.audioId, itemMusic.musicName, itemMusic.musicArtist,
                                    itemMusic.musicAlbumArtPath);
                itemList.add(myItemMusic);
                itemList.add(new MyItemDivider());
            }
        }
        if (itemList.peek() instanceof MyItemDivider) {
            itemList.pop();
        }
        if (itemList.peek() instanceof MyItemText) {
            itemList.pop();
        }
        itemListCnt = itemList.size();
    }

    public void clearSearchList() {
        itemList.clear();
        itemListCnt = 0;
    }


    abstract class MyItemBase {

        protected View convertView;

        protected abstract View getConvertView(
            ViewGroup parent);

        protected abstract void onClickItem();

        public boolean isEnabled() {
            return true;
        }

        public SpannableStringBuilder highlight(String text, String target) {
            return TextUtilTools.highlight(text, target);
        }
    }

    class MyItemDivider extends MyItemBase {

        @Override
        public View getConvertView(
            ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    mInflater.inflate(R.layout.kuso_divider_horizontal, parent, false);
            }
            return convertView;
        }

        @Override
        protected void onClickItem() {
        }

        public boolean isEnabled() {
            return false;
        }
    }

    class MyItemText extends MyItemBase {

        String content;

        public MyItemText(String content) {
            this.content = content;
        }

        @Override
        public View getConvertView(
            ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    mInflater.inflate(R.layout.kuso_search_list_clean, parent, false);
                TextView
                    text =
                    (TextView) convertView.findViewById(R.id.kuso_search_clean_history_text);
                text.setText(content);
            }
            return convertView;
        }

        @Override
        protected void onClickItem() {
        }

        public boolean isEnabled() {
            return false;
        }
    }

    class AppInfo {

        ActivityInfo activityInfo;
        String name;
    }

    class AppGirdAdapter extends BaseAdapter {

        List<AppInfo> appInfos = new ArrayList<AppInfo>();

        AppGirdAdapter(List<AppInfo> appinfos) {
            this.appInfos = appinfos;
        }

        @Override
        public int getCount() {
            return appInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    mInflater.inflate(R.layout.kuso_search_list_item_app, parent, false);

                //get icon drawable
                int
                    iconSize =
                    mContext.getResources()
                        .getDimensionPixelSize(R.dimen.kuso_localsearch_app_icon_size);
                BitmapDrawable
                    icon =
                    (BitmapDrawable) appInfos.get(position).activityInfo
                        .loadIcon(mContext.getPackageManager());
                icon.setBounds(0, 0, iconSize, iconSize);

                //get highlight text
                SpannableStringBuilder
                    textString =
                    TextUtilTools.highlight(appInfos.get(position).name, mSearchText.toString());

                //icon and hight text set to textview
                TextView
                    textView =
                    (TextView) convertView.findViewById(R.id.kuso_search_list_item_title);
                textView.setText(textString);
                textView.setCompoundDrawables(null, icon, null, null);


            }

            return convertView;
        }
    }

    // 应用
    class MyItemApp extends MyItemBase {

        List<AppInfo> appInfos = new ArrayList<>();

        public MyItemApp(List<AppInfo> appInfos) {
            this.appInfos = appInfos;
        }

        @Override
        public View getConvertView(
            ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    mInflater.inflate(R.layout.kuso_search_list_gird_app, parent, false);
                ((GridView) convertView).setAdapter(new AppGirdAdapter(appInfos));
                ((GridView) convertView).setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            UIEffectTools.onClickEffect(view);
                            Intent intent = new Intent();
                            imm.hideSoftInputFromWindow(convertView.getWindowToken(), 0);
                            intent.setClassName(appInfos.get(position).activityInfo.packageName,
                                                appInfos.get(position).activityInfo.name);
                            mContext.startActivity(intent);
                        }
                    });
            }
            return convertView;
        }

        @Override
        protected void onClickItem() {

        }
    }

    /*
     * 联系人
     */
    class MyItemContacts extends MyItemBase implements View.OnClickListener {

        protected Contacts contact;

        public MyItemContacts(Contacts contact) {
            this.contact = contact;
        }

        @Override
        public View getConvertView(
            ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    mInflater.inflate(R.layout.kuso_search_list_item_contact, parent, false);

                TextView
                    textViewTitle =
                    (TextView) convertView.findViewById(R.id.kuso_search_list_item_title);
                textViewTitle.setText(highlight(contact.getName(), mSearchText.toString()));

                ImageView imgSms = (ImageView) convertView.findViewById(R.id.kuso_contact_sms);
                imgSms.setOnClickListener(this);

                ImageView imgDial = (ImageView) convertView.findViewById(R.id.kuso_contact_dail);
                imgDial.setOnClickListener(this);
            }
            return convertView;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.kuso_contact_sms:
                    Intent
                        sendIntent =
                        new Intent(Intent.ACTION_SENDTO,
                                   Uri.parse("smsto:" + contact.getPhoneNumber()));
                    mContext.startActivity(sendIntent);
                    break;
                case R.id.kuso_contact_dail:
                    Intent
                        intentPhone =
                        new Intent(Intent.ACTION_CALL,
                                   Uri.parse("tel:" + contact.getPhoneNumber()));
                    mContext.startActivity(intentPhone);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onClickItem() {
            Intent
                intent =
                new Intent(Intent.ACTION_VIEW,
                           Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                                                String.valueOf(contact.getContactsID())));
            mContext.startActivity(intent);
        }

    }

    /*
     * Music
     */
    class MyItemMusic extends MyItemBase {

        protected int audioId;
        protected String musicArtist;
        protected String musicName;
        protected String musicAlbumArtPath;

        public MyItemMusic(int audioId,
                           String musicName,
                           String musicArtist,
                           String musicAlbumArtPath) {
            this.audioId = audioId;
            this.musicName = musicName;
            this.musicArtist = musicArtist;
            this.musicAlbumArtPath = musicAlbumArtPath;
        }

        @Override
        public View getConvertView(
            ViewGroup parent) {
            if (convertView == null) {
                convertView =
                    mInflater.inflate(R.layout.kuso_search_list_item_music, parent, false);
                TextView
                    textViewNusicName =
                    (TextView) convertView.findViewById(R.id.kuso_search_list_item_title);
                textViewNusicName.setText(highlight(musicName, mSearchText.toString()));

                TextView
                    textViewArtist =
                    (TextView) convertView.findViewById(R.id.kuso_search_list_item_artist);
                textViewArtist.setText(musicArtist);

                Bitmap albumArtBitmap = null;
                if (musicAlbumArtPath != null){
                    albumArtBitmap = BitmapFactory.decodeFile(musicAlbumArtPath);
                }

                if (albumArtBitmap == null){
                    albumArtBitmap =
                        BitmapFactory
                            .decodeResource(mContext.getResources(), R.drawable.kuso_music_icon);
                }

                IconImageView
                    imageViewIcon =
                    (IconImageView) convertView.findViewById(R.id.kuso_search_list_item_icon);
                imageViewIcon.setBitmap(albumArtBitmap);
            }
            return convertView;
        }

        @Override
        protected void onClickItem() {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                .withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                  String.valueOf(audioId)));
            mContext.startActivity(intent);
        }
    }
}
