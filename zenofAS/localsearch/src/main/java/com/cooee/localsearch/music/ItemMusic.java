package com.cooee.localsearch.music;

import com.cooee.localsearch.base.ItemBase;
import com.cooee.t9search.model.PinyinUnit;

import java.util.List;

/*
 * Music
 */
public class ItemMusic extends ItemBase {

    public int audioId;
    public String musicArtist;
    public String musicAlbumArtPath;
    public String musicName;
    private String musicPath;
    public ItemMusic(
        int audioId,
        String musicName,
        String musicArtist,
        String musicAlbumArtPath,
        String musicPath) {
        this.audioId = audioId;
        this.musicName = musicName;
        this.musicArtist = musicArtist;
        this.musicAlbumArtPath = musicAlbumArtPath;
        this.musicPath = musicPath;
    }

    @Override
    public boolean match(
        String text,
        List<PinyinUnit> srcUnit) {
        if (T9Match(musicName, srcUnit) || musicName.toLowerCase().contains(text)) {
            return true;
        } else {
            return musicName.toLowerCase().contains(text.toLowerCase());
        }
    }
}
