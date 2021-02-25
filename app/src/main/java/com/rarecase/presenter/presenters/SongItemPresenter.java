package com.rarecase.presenter.presenters;

import androidx.fragment.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Pair;

import com.rarecase.model.PidType;
import com.rarecase.model.Song;
import com.rarecase.model.remote.SongItemRepository;
import com.rarecase.model.remote.SpringDownloadManager;
import com.rarecase.presenter.contracts.ISongItemPresenter;
import com.rarecase.spring.DownloaderActivity;
import com.rarecase.spring.ISongItemView;
import com.rarecase.spring.ISongListView;
import com.rarecase.spring.R;
import com.rarecase.spring.TabActivity;
import com.rarecase.utils.Utils;
import com.saavn.android.DRMManager;

import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class SongItemPresenter implements ISongItemPresenter,Observer{

    private final Context _context;
    private ISongListView _listView;
    private final ISongItemView _itemView;
    private static SongItemRepository _itemRepository;

    public SongItemPresenter(Context context, ISongItemView itemView){
        _context = context;
        _itemView = itemView;

        //If presenter is created from TabActivity, it must be some Fragment
        if (context instanceof TabActivity) {
                TabActivity a =  (TabActivity) context;
                List<Fragment> listViews = a.getSupportFragmentManager().getFragments();
            if(_itemView.getViewPidType() == PidType.Shared)
                _listView = (ISongListView) listViews.get(0);
            else if (_itemView.getViewPidType() == PidType.Offline)
                _listView = (ISongListView) listViews.get(1);
        }
        //Else it is an Activity
        else if(_context instanceof DownloaderActivity){
            _listView = (DownloaderActivity) context;
        }
        //Initialize the static Repository.
        if(_itemRepository == null)
            _itemRepository = new SongItemRepository(context);     //One static repository for all itemPresenters created for each item
    }


    @Override
    public void performSpringAction(final Song song) {

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            if (!Utils.hasWritePermission(_context)) {
                _listView.requestStoragePermission();
                return;
            }
        }

        if (_itemView.getViewPidType() == PidType.Shared || _itemView.getViewPidType() == PidType.Downloading) {

            String mediaUrl = DRMManager.decryptMediaURL(song.getEnc_media_url());
            if ((mediaUrl != null)) {
                if (!mediaUrl.isEmpty() || (mediaUrl.endsWith(".mp3") || mediaUrl.endsWith(".mp4"))) {
                    if (Utils.isOnline(_context)) {

                        SpringDownloadManager _downloadManager = new SpringDownloadManager(_context, song);
                        if (_downloadManager.isDownloadInProgress()) {
                            if (_itemView.getViewPidType() == PidType.Shared)
                                _listView.showSnackbar(_context.getString(R.string.download_queued));
                        } else {
                            _downloadManager.enqueueSongDownload(mediaUrl, song);
                            _listView.showSnackbar(_context.getString(R.string.download_started));
                        }
                    } else {
                        _listView.showSnackbar(_context.getString(R.string.you_are_offline));
                    }
                } else {
                    _listView.showSnackbar(_context.getString(R.string.unavailable_song));
                }
            }
        }
    }

    @Override
    public void downloadAlbumArt(Song song) {

        if (_itemRepository.getImgCache().containsKey(song.getAlbum())) {
                _itemView.setAlbumArt(_itemRepository.getImgCache().get(song.getAlbum()));
        } else {
                _itemRepository.addObserver(this);
                _itemRepository.downloadAlbumArtAsync(song);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof SongItemRepository){
            if(arg!= null){
                if(arg instanceof Pair){
                    Pair result = (Pair)arg;
                    if(result.first.equals(_itemView.getSongItemAlbum())) {
                        if(result.second != null) {
                            _itemView.setAlbumArt((Bitmap) result.second);
                        }
                    }
                }
            }
        }
    }
}
