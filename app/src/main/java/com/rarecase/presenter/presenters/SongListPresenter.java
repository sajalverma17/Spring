package com.rarecase.presenter.presenters;


import android.content.Context;
import android.support.v4.app.Fragment;

import com.rarecase.model.PidType;
import com.rarecase.model.Song;
import com.rarecase.model.remote.Scraper;
import com.rarecase.model.remote.SongListRepository;
import com.rarecase.presenter.contracts.ISongListPresenter;
import com.rarecase.spring.DownloaderActivity;
import com.rarecase.spring.HomeActivity;
import com.rarecase.spring.ISongListView;
import com.rarecase.spring.R;
import com.rarecase.spring.TabActivity;
import com.rarecase.utils.CachedPidsReader;
import com.rarecase.utils.SortingHelper;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles a collection of song information
 */

public class SongListPresenter implements ISongListPresenter, Observer {

    private ISongListView _view;
    private Context _context;
    private String _stringExtra;
    private List<String> pids;

    public SongListPresenter(Context context,ISongListView view){
        //Get the view from TabActivity's FragmentManager
        if(context instanceof TabActivity)
        {
            TabActivity tabActivity = (TabActivity) context;
            List<Fragment> fragments = tabActivity.getSupportFragmentManager().getFragments();

            if(view instanceof HomeActivity)
                _view = (ISongListView) fragments.get(0);
            else
                _view = (ISongListView) fragments.get(1);
        }
        else
        {
            _view = view;
        }
        _context = context; //Passed to Repository to access Cache
    }

    private class retryCall implements Callable{

        private PidType _pidType;
        private Object _sourceInstance;

        retryCall(Object sourceInstance){

           if(_view instanceof HomeActivity)
               _pidType = PidType.Offline;
            if(_view instanceof DownloaderActivity)
                _pidType = PidType.Shared;
            _sourceInstance = sourceInstance;
        }

        @Override
        public Object call() throws Exception {

            if(_sourceInstance instanceof Scraper) {
                processStringExtra(_stringExtra);
            }
            if(_sourceInstance instanceof SongListRepository) {
                loadSongDetails(pids, _pidType);
            }
            return null;
        }
    }

    @Override
    public void loadOfflineSongs() {

        if(!CachedPidsReader.canReadInternalStorage()){
            _view.requestStoragePermission();
        }else if(!CachedPidsReader.pidsExist()){
            _view.showErrorPopulatingSongs(_context.getString(R.string.ghost_image_zero_songs_saved_offline));
            //_view.showSnackbar(_context.getString(R.string.zero_songs_saved_offline));
        }else {
            //Found cached encrypted song directory with one or more mp3 files.
            pids = CachedPidsReader.getCachedPids();
            //Offline downloaded songs by user found.  This call is from HomeActivity
            loadSongDetails(pids, PidType.Offline);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        _view.hideProgressBar();
        if (arg != null) {
            if (arg instanceof String)  {

                String error = (String) arg;

                Callable retryAction = new retryCall(o);

                _view.showErrorPopulatingSongs(_context.getString(R.string.generic_network_error));

                if (error.equals("STE")) {
                    _view.showSnackbarWithAction(_context.getString(R.string.network_too_slow),_context.getString(R.string.retry),retryAction);
                }
                if (error.equals("UHE")) {
                    _view.showSnackbarWithAction(_context.getString(R.string.unable_to_connect),_context.getString(R.string.retry),retryAction);
                }
                if (error.equals("503")) {
                    _view.showSnackbarWithAction(_context.getString(R.string.rejected_by_server),_context.getString(R.string.retry),retryAction);
                }
                if(error.equals("UNK")){
                    _view.showSnackbarWithAction(_context.getString(R.string.unkown_io_exception), _context.getString(R.string.retry), retryAction);
                }
            } else if (arg instanceof List) {
                if(o instanceof SongListRepository) {
                    List<Song> songList = (List<Song>) arg;
                    if(_view instanceof HomeActivity) {
                        //Sorting
                        songList = SortingHelper.quickSort(songList, 0, songList.size() - 1);
                    }
                    _view.setSongList(songList);
                }
                //Output from scraper - This call is for DownloadActivity.
                if(o instanceof Scraper){
                    List<String> pidList = (List<String>) arg;
                    pids = pidList;
                    loadSongDetails(pidList,PidType.Shared);
                }
            }
        }
    }

    /**
     * New implementation to include caching mechanism
     * @param pids
     * @param pidType Based on source of pid, use corresponding caching mechanism in SongCacheManager
     */
    private void loadSongDetails(List<String> pids,PidType pidType){
        _view.hideErrorPopulatingSongs();
        _view.showProgressBar();
        SongListRepository repository = new SongListRepository(_context);
        repository.addObserver(this);
        repository.loadSongDetails(pids,pidType);
    }

    /**
     * Method to extract webpage url and pass it to Scraper
     * @param stringExtra The intent data string shared to Spring
     */
    public void processStringExtra(String stringExtra){
        _view.showProgressBar();
        _stringExtra = stringExtra;

        String trimmedStringExtra = stringExtra.substring(14, stringExtra.length());
        if( trimmedStringExtra.startsWith(_context.getString(R.string.albumString))
                || trimmedStringExtra.startsWith(_context.getString(R.string.songString))
                || trimmedStringExtra.startsWith(_context.getString(R.string.playlistString)))
        {
            Pattern pattern = Pattern.compile(".*(http.*)");
            Matcher matcher = pattern.matcher(stringExtra);
            if(matcher.find()){
                String url = matcher.group(1);
                Scraper webScraper = new Scraper(url);
                webScraper.addObserver(this);
                webScraper.scrapeForPidsAsync();
            }else{
                _view.hideProgressBar();
                _view.showSnackbar(_context.getString(R.string.error_processing_link));
            }
        }else {
            _view.hideProgressBar();
            _view.showSnackbar(_context.getString(R.string.only_songs_albums_playlists));
        }
    }

}
