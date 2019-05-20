package com.rarecase.presenter.presenters

import android.content.Context
import com.rarecase.model.PidType
import com.rarecase.model.SongCacheManager
import com.rarecase.presenter.contracts.ISongListPresenter
import com.rarecase.spring.ISongListView
/*
    Presenter for Last shared song list view
 */
class SharedSongListPresenter(context : Context, view : ISongListView) : ISongListPresenter {

    private var _context: Context = context
    private var _view: ISongListView = view

    override fun loadOfflineSongs() {
        //This method will load the cached last shared song
        //and pass it to the _view.setSongList()
        val cacheManager = SongCacheManager(_context)
        val songList = cacheManager.getCachedSongs(PidType.Shared).values.toList()
        _view.setSongList(songList)
    }

}