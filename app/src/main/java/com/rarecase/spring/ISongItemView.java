package com.rarecase.spring;

import android.graphics.Bitmap;

import com.rarecase.model.PidType;

/**
 * UI Methods performed on each song item in the Recyclerview Adapter
 */

public interface ISongItemView {
    String getSongItemId();
    String getSongItemAlbum();
    /*
    The type of list to which this view belongs to, i.e., PidType of the item
     */
    PidType getViewPidType();
    void setAlbumArt(Bitmap albumArt);

}
