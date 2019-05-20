package com.rarecase.presenter.presenters;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
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
import com.rarecase.utils.CachedPidsReader;
import com.rarecase.utils.SpringSharedPref;
import com.rarecase.utils.Utils;
import com.saavn.android.DRMManager;

import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class SongItemPresenter implements ISongItemPresenter,Observer{

    private Context _context; //To pass into shared prefs
    private ISongListView _listView;
    private ISongItemView _itemView;
    //TODO : Make imgCache static here instead of itemRepository.
    private static SongItemRepository _itemRepository;
    private static final int MIN_STORAGE_SPACE_REQUIRED = 20;

    public SongItemPresenter(Context context, ISongItemView itemView){
        _context = context;
        _itemView = itemView;

        //If presenter is created from TabActivity, it must be some Fragment
        if (context instanceof TabActivity) {
                TabActivity a =  (TabActivity) context;
                List<Fragment> listViews = a.getSupportFragmentManager().getFragments();
            if(_itemView.getViewPidType() == PidType.Offline)
                _listView = (ISongListView) listViews.get(0);
            else if (_itemView.getViewPidType() == PidType.Shared)
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

        if(CachedPidsReader.canWriteToStorage()) {
            if(CachedPidsReader.freeSpaceInMBs()>MIN_STORAGE_SPACE_REQUIRED) {

                final String storagePath = new SpringSharedPref(_context).getStoragePath();

                //All mp3s decrypted will be saved with this name. Later used to identify for ID3 tagging.
                //String springActionFileName = Utils.generateSpringActionFileName(song.getSong(),song.getId());

                if (_itemView.getViewPidType() == PidType.Offline) {

                    int result = DRMManager.decryptSongPartial(song.getId(),song.getSong(),storagePath);
                    //Not tagging files when decrypting. Tagging the file on UI thread introduces errors
                    //Utils.tagAudioFile(song,song.getAlbumArt(),storagePath + "/" +song.getSong() +".mp3");
                    if (result != -1) {
                        _listView.showSnackbar(_context.getString(R.string.decryption_successfull));
                    } else {
                        //TODO : Low Priority - Find the song which fails to decrypt with decryptSongPartial method
                        DRMManager.decryptSong(song.getId(),song.getSong(),storagePath);
                        _listView.showSnackbar(_context.getString(R.string.decryption_failed));
                    }

                } else if (_itemView.getViewPidType() == PidType.Shared || _itemView.getViewPidType() == PidType.Downloading) {

                    String mediaUrl = DRMManager.decryptMediaURL(song.getEnc_media_url());
                    Log.i("SongItemPresenter:", "Got media url :" + mediaUrl);
                    if ((mediaUrl != null)) {
                        if(!mediaUrl.isEmpty() || (mediaUrl.endsWith(".mp3") || mediaUrl.endsWith(".mp4")) ) {
                                if(Utils.isOnline(_context)) {

                                        SpringDownloadManager _downloadManager = new SpringDownloadManager(_context,song);
                                        if(_downloadManager.isDownloadInProgress()){
                                            if(_itemView.getViewPidType() == PidType.Shared)
                                            _listView.showSnackbar(_context.getString(R.string.download_queued));
                                        }
                                        else {
                                            _downloadManager.enqueueSongDownload(mediaUrl, song, storagePath);
                                            _listView.showSnackbar(_context.getString(R.string.download_started));
                                        }
                                }else {
                                    _listView.showSnackbar(_context.getString(R.string.you_are_offline));
                                }
                            }else{
                                _listView.showSnackbar(_context.getString(R.string.unavailable_song));
                            }
                    }
                }
            }else{
                _listView.showSnackbar(_context.getString(R.string.not_enough_space));
            }
        }else {
            _listView.requestStoragePermission();
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
                        _itemView.setAlbumArt((Bitmap) result.second);
                    }
                }
            }
        }
    }
}
