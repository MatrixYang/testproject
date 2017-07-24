package com.cooee.localsearch.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.cooee.localsearch.app.ItemApp;
import com.cooee.localsearch.contacts.Contacts;
import com.cooee.localsearch.contacts.ContactsHelper;
import com.cooee.localsearch.contacts.ItemContacts;
import com.cooee.localsearch.music.ItemMusic;
import com.cooee.localsearch.music.MusicLoader;
import com.cooee.t9search.model.PinyinUnit;
import com.cooee.t9search.util.PinyinUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by cuiqian on 2016/2/3.
 */
public class LocalSearchListImpl {

    private Context mContent;
    // 应用
    private List<ItemApp> allAppList = new ArrayList<ItemApp>();
    // 联系人
    private List<ItemContacts> allContactsList = new ArrayList<ItemContacts>();
    // 音乐
    private List<ItemMusic> allMusicList = new ArrayList<ItemMusic>();

    private Stack<ItemBase> itemAppList = new Stack<ItemBase>();
    private Stack<ItemBase> itemContactsList = new Stack<ItemBase>();
    private Stack<ItemBase> itemMusicList = new Stack<ItemBase>();

    private ContactsHelper contactsHelper;

    public LocalSearchListImpl(Context context) {
        mContent = context;
    }

    protected synchronized Stack<ItemBase> getAppList(String text) {
        itemAppList.clear();
        if (text != null && text.length() > 0) {
            List<PinyinUnit> srcUnit = new ArrayList<PinyinUnit>();
            PinyinUtil.chineseStringToPinyinUnit(text, srcUnit);
            for (ItemApp app : allAppList) {
                if (app.match(text, srcUnit)) {
                    itemAppList.add(app);
                }
            }
        }
        return itemAppList;
    }

    protected synchronized Stack<ItemBase> getContactsList(String text) {
        itemContactsList.clear();
        if (text != null && text.length() > 0) {
            List<PinyinUnit> srcUnit = new ArrayList<PinyinUnit>();
            PinyinUtil.chineseStringToPinyinUnit(text, srcUnit);

            for (ItemContacts contact : allContactsList) {
                if (contact.match(text, srcUnit)) {
                    itemContactsList.add(contact);
                }
            }
        }
        return itemContactsList;
    }

    protected synchronized Stack<ItemBase> getMusicList(String text) {
        itemMusicList.clear();
        if (text != null && text.length() > 0) {
            List<PinyinUnit> srcUnit = new ArrayList<PinyinUnit>();
            PinyinUtil.chineseStringToPinyinUnit(text, srcUnit);
            for (ItemMusic music : allMusicList) {
                if (music.match(text, srcUnit)) {
                    itemMusicList.add(music);
                }
            }
        }
        return itemMusicList;
    }

    // 应用
    private void loadContentApp() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = mContent.getPackageManager();
        List<ResolveInfo> resolveList = pm.queryIntentActivities(intent, 0);
        List<ItemApp> _allAppList = new ArrayList<ItemApp>();
        for (ResolveInfo info : resolveList) {
            ItemApp item = new ItemApp(mContent, info.activityInfo);
            _allAppList.add(item);
        }
        allAppList = _allAppList;
    }

    // 音乐
    private void loadContentMusic() {
        MusicLoader
            loader = MusicLoader
            .instance(mContent.getContentResolver());
        List<ItemMusic> _allMusicList = new ArrayList<ItemMusic>();
        List<MusicLoader.MusicInfo> musicList = loader.getMusicList();
        for (MusicLoader.MusicInfo info : musicList) {
            _allMusicList
                .add(new ItemMusic(info.musicId, info.musicName, info.musicArtist,
                                   info.musicAlbumArtPath,
                                   info.musicPath));
        }
        allMusicList = _allMusicList;
    }

    // 联系人
    private void loadContentContacts() {
        List<Contacts> contacts = contactsHelper.getBaseContacts();
        List<ItemContacts> _allContactsList = new ArrayList<ItemContacts>();
        for (Contacts contact : contacts) {
            _allContactsList.add(new ItemContacts(contact));
        }
        allContactsList = _allContactsList;
    }

    protected void loadContent() {
        new Thread() {

            public void run() {
                loadContentApp();
                loadContentMusic();
            }

            ;
        }.start();
        contactsHelper = ContactsHelper
            .getInstance(mContent);
        contactsHelper.setOnContactsLoad(new ContactsHelper.OnContactsLoad() {

            @Override
            public void onContactsLoadSuccess() {
                loadContentContacts();
            }

            @Override
            public void onContactsLoadFailed() {
            }
        });
        contactsHelper.startLoadContacts();
    }

    protected void initContent() {
        itemAppList.clear();
        itemContactsList.clear();
        itemMusicList.clear();
    }

    protected void onDestroy() {
        if (contactsHelper != null) {
            contactsHelper.setContactsChanged(true);
        }
    }

}
