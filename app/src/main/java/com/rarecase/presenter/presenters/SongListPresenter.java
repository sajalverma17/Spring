package com.rarecase.presenter.presenters;


import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.rarecase.model.PidType;
import com.rarecase.model.Song;
import com.rarecase.model.remote.Scraper;
import com.rarecase.model.remote.SongListRepository;
import com.rarecase.presenter.contracts.ISongListPresenter;
import com.rarecase.spring.DownloaderActivity;
import com.rarecase.spring.ISongListView;
import com.rarecase.spring.R;
import com.rarecase.spring.TabActivity;
import java.net.URL;
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
    private String _retryUrl;
    private RetryOperation _retryOperation;
    private List<String> pids;

    public SongListPresenter(Context context,ISongListView view){
        //Get the view from TabActivity's FragmentManager
        if(context instanceof TabActivity)
        {
            TabActivity tabActivity = (TabActivity) context;
            List<Fragment> fragments = tabActivity.getSupportFragmentManager().getFragments();
            _view = (ISongListView) fragments.get(0);
        }
        else
        {
            _view = view;
        }
        _context = context; //Passed to Repository to access Cache
    }

    /**
     * Does nothing, as the SharedSongListPresenter loads the songs recently shared.
     */
    @Override
    public void loadOfflineSongs() {
    }

    @Override
    public void update(Observable o, Object arg) {
        _view.hideProgressBar();
        if (arg != null) {
            if (arg instanceof String)  {

                String error = (String) arg;

                Callable retryAction = new retryCall(o);

                if(error.equals("UIS")){
                    _view.showSnackbar(_context.getString(R.string.unsupported_item_shared));
                    _view.showErrorPopulatingSongs(_context.getString(R.string.unexpected_url_please_report));
                    return;
                }
                if(error.equals("PAR")){
                    // A parsing exception means that a song/album/playlist shared (things we support for download)
                    // but the json data was not what Spring expected, and some rules of parsing might need update, extenstion
                    _view.showSnackbar(_context.getString(R.string.parsing_exception));
                    _view.showErrorPopulatingSongs(_context.getString(R.string.unexpected_json_please_report));
                    return;
                }
                if(error.equals("GNE")){
                    _view.showSnackbarWithAction(_context.getString(R.string.generic_network_error), _context.getString(R.string.retry), retryAction);
                }
                if (error.equals("STE")) {
                    _view.showSnackbarWithAction(_context.getString(R.string.network_too_slow),_context.getString(R.string.retry),retryAction);
                }
                if (error.equals("UHE")) {
                    _view.showSnackbarWithAction(_context.getString(R.string.unable_to_connect),_context.getString(R.string.retry),retryAction);
                }
                if (error.equals("503")) {
                    _view.showSnackbarWithAction(_context.getString(R.string.rejected_by_server),_context.getString(R.string.retry),retryAction);
                }

                // This is for a "UNK" error code to handle, we minimized returning this error code to only when we are not sure
                _view.showErrorPopulatingSongs(_context.getString(R.string.generic_error));

            } else if (arg instanceof List) {
                if(o instanceof SongListRepository) {
                    List<Song> songList = (List<Song>) arg;
                    _view.setSongList(songList);
                }
                //Output from scraper - This call is for DownloadActivity.
                if(o instanceof Scraper){
                    List<String> pidList = (List<String>) arg;
                    pids = pidList;
                    loadSongDetails(pidList, PidType.Shared);
                }
            } else if (arg instanceof URL) {

                // Output from scraper's getRedirectURL asyc operation will be followed with scraping async operation
                if (o instanceof Scraper){
                    String url = arg.toString();
                    scrapeForPidsAsync(url);
                }
            }
        }
    }

    /**
     * Called by a view when user shares a string extra form hostapp to Spring. Is the entry point for a chain of async tasks:
     * 1. GetRedirectedUrl
     * 2. Scrape the webpage of the redirected URL for PIDs (and song details)
     * 3. Get song details of each PIDs from host app\s API.
     * @param stringExtra
     */
    public void processStringExtra(String stringExtra){
        Log.i("Processing shared item:", stringExtra);
            Pattern pattern = Pattern.compile(".*(http.*)");
            Matcher matcher = pattern.matcher(stringExtra);
            if(matcher.find()){
                String url = matcher.group(1);
                getRedirectedURL(url);
            } else {
                _view.showSnackbar(_context.getString(R.string.error_processing_link));
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

    /** The information we scrape for is in the source HTML of
     * the redirected page we reach when hitting this string Extra shared by host app. So we execute an http GET to get the redirect url
     */
    private void getRedirectedURL(String url)    {
        _view.showProgressBar();
        _retryUrl = url;
        _retryOperation = RetryOperation.Redirect;
        Scraper webScraper = new Scraper(url);
        webScraper.addObserver(this);
        webScraper.getRedirectURL();
    }

    /**
     * Scrapes the webpage to get PIDs.
     */
    private void scrapeForPidsAsync(String url){
        _view.showProgressBar();
        _retryUrl = url;
        _retryOperation = RetryOperation.Scraping;
        Scraper webScraper = new Scraper(url);
        webScraper.addObserver(this);
        webScraper.scrapeForPidsAsync();
    }

    private class retryCall implements Callable{

        private PidType _pidType;
        private Object _sourceInstance;

        retryCall(Object sourceInstance){

            if(_view instanceof DownloaderActivity)
                _pidType = PidType.Shared;
            _sourceInstance = sourceInstance;
        }

        @Override
        public Object call() throws Exception {

            if(_sourceInstance instanceof Scraper) {
                if(_retryOperation == RetryOperation.Scraping) {
                    scrapeForPidsAsync(_retryUrl);
                } else if(_retryOperation == RetryOperation.Redirect) {
                    getRedirectedURL(_retryUrl);
                }
            }
            if(_sourceInstance instanceof SongListRepository) {
                loadSongDetails(pids, _pidType);
            }
            return null;
        }
    }

    private enum RetryOperation {
        Scraping,
        Redirect
    }

}
