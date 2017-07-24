package com.cooee.localsearch.music;


import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MusicLoader {

    private static MusicLoader musicLoader;
    private static ContentResolver contentResolver;

    public static MusicLoader instance(
        ContentResolver pContentResolver) {
        if (musicLoader == null) {
            contentResolver = pContentResolver;
            musicLoader = new MusicLoader();
        }
        return musicLoader;
    }

    private MusicLoader() { //利用ContentResolver的query函数来查询数据，然后将得到的结果放到MusicInfo对象中，最后放到数组中
    }

    public List<MusicInfo> getMusicList() {
        ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
        // 加入封装音乐信息的代码
        // 查询所有歌曲
        ContentResolver musicResolver = contentResolver;
        Cursor
            musicCursor =
            musicResolver
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        int musicColumnIndex;
        if (null != musicCursor && musicCursor.getCount() > 0) {
            for (musicCursor.moveToFirst(); !musicCursor.isAfterLast(); musicCursor.moveToNext()) {
                MusicInfo musicDataMap = new MusicInfo();
                musicColumnIndex = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID);
                musicDataMap.musicId = musicCursor.getInt(musicColumnIndex);
                Random random = new Random();
                musicDataMap.musicRating = Math.abs(random.nextInt()) % 10;
                // 取得音乐播放路径
                //				musicColumnIndex = musicCursor.getColumnIndex( MediaStore.Audio.AudioColumns.DATA );
                //				musicDataMap.musicPath = musicCursor.getString( musicColumnIndex );
                // 取得音乐的名字
                musicColumnIndex = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE);
                musicDataMap.musicName = musicCursor.getString(musicColumnIndex);
                // 取得音乐的专辑名称
                //				musicColumnIndex = musicCursor.getColumnIndex( MediaStore.Audio.AudioColumns.ALBUM );
                //				musicDataMap.musicAlbum = musicCursor.getString( musicColumnIndex );
                // 取得音乐的演唱者
                musicColumnIndex = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
                musicDataMap.musicArtist = musicCursor.getString(musicColumnIndex);
                // 取得歌曲对应的专辑对应的Key
                musicColumnIndex =
                    musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY);
                String musicAlbumKey = musicCursor.getString(musicColumnIndex);
                if (musicAlbumKey != null) {
                    String[] argArr = {musicAlbumKey};
//                ContentResolver albumResolver = this.getContentResolver();
                    Cursor
                        albumCursor =
                        musicResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                                            MediaStore.Audio.AudioColumns.ALBUM_KEY + " = ?",
                                            argArr,
                                            null);
                    if (null != albumCursor && albumCursor.getCount() > 0) {
                        albumCursor.moveToFirst();
                        int
                            albumArtIndex =
                            albumCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART);

                        String musicAlbumArtPath = albumCursor.getString(albumArtIndex);
                        musicDataMap.musicAlbumArtPath = musicAlbumArtPath;
                        albumCursor.close();
                    }
                }

                musicList.add(musicDataMap);
            }
            musicCursor.close();
        }
        //		Log.i( "musicList" , "hello world!!!!!!" );
        return musicList;
    }

    //下面是自定义的一个MusicInfo子类，实现了Parcelable，为的是可以将整个MusicInfo的ArrayList在Activity和Service中传送，=_=!!,但其实不用
    public static class MusicInfo {

        public int musicId;
        public int musicRating;
        public String musicPath;
        public String musicName;
        public String musicAlbum;
        public String musicArtist;
        public String musicAlbumArtPath;
    }
}
