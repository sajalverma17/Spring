package com.rarecase.presenter.contracts;


import com.rarecase.model.Song;

/**
 * Methods performed for individual song objects in the List
 */

public interface ISongItemPresenter {
    /**
     * Performs Decrypt/Download for the Song object
     * @param song {@code Song} object on which action will be performed
     */
    void performSpringAction(Song song);

    /**
     * Triggers the album art download using Song object's albumArt field
     * @param song {@code Song} object on which action will be performed
     */
    void downloadAlbumArt(Song song);


}
