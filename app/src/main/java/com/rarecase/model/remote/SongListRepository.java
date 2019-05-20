package com.rarecase.model.remote;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rarecase.model.PidType;
import com.rarecase.model.Song;
import com.rarecase.model.SongCacheManager;
import com.rarecase.model.SongJsonParser;
import com.rarecase.presenter.presenters.SongListPresenter;
import com.rarecase.utils.HttpHelper;
import com.rarecase.utils.SortingHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;

/**
 * Worker class to fetch song details by making API calls
 */

public class SongListRepository extends java.util.Observable{


    private List<Song> songs = new ArrayList<>();
    private Context context;

    @Override
    public synchronized void addObserver(Observer o) {
        if(o instanceof SongListPresenter) {
            super.addObserver(o);
        }
    }

    public SongListRepository(Context context){
        this.context = context;
    }

    public void loadSongDetails(List<String> pids, PidType pidType){
        SongCacheManager songCacheManager = new SongCacheManager(context);
        //Try to get songs from cache
        if(pidType == PidType.Offline || pidType == PidType.Shared) {
            HashMap<String, Song> dictionary = songCacheManager.getCachedSongs(pidType);
            List<String> notInCache = new ArrayList<>();
            for (String pid : pids) {
                Song s = dictionary.get(pid);
                if (s != null)
                    songs.add(s);
                else
                    notInCache.add(pid);
            }
            if (notInCache.size() > 0) {
                downloadSongDetailsAsync(notInCache, pidType);
            }
            else {
                setChanged();
                notifyObservers(songs);
            }
        }
        else {
            downloadSongDetailsAsync(pids,pidType);
        }
    }

    /**
     * Downloads song details from the API asynchronously and notifies the observers with the data.
     */
    private void downloadSongDetailsAsync(final List<String> pids, final PidType pidType) {

        final String DETAILS_URL =  "http://www.saavn.com/api.php?__call=song.getDetails&pids="+makeQueryString(pids)+"&ctx=android&_format=json&_marker=0&network_type=mobile&network_subtype=UMTS&network_operator=Android&cc=us&v=23&readable_version=2.6%3A&manufacturer=unknown&model=google_sdk&build=google_sdk-eng+2.3.4+GINGERBREAD+12";

        new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {

                    String resultJson;
                    resultJson = HttpHelper.downloadUrl(DETAILS_URL);
                    //When either a known exception is thrown by HttpHelper, or success
                    if(resultJson != null) {
                        if (resultJson.contains("&#039;")) {
                            resultJson = resultJson.replace("&#039;", "'");
                        }
                        if(resultJson.contains("&quot;")){
                            resultJson = resultJson.replace("&quot;","'");
                        }
                        if(resultJson.contains("&amp;")){
                            resultJson = resultJson.replace("&amp;","&");
                        }
                    }
                    return resultJson;
                }

                @Override
                protected void onPostExecute(String resultJson) {
                    if(resultJson != null) {
                        if(resultJson.length() == 3){
                            setChanged();
                            if(songs.size() > 0)    //Some songs were loaded from cache and added to songs List
                                                    // Display them despite the network error
                                                    //The songs not loaded from cache would have the details
                                                    //downloaded from API next time the app is opened with good network
                                notifyObservers(songs);
                            else
                                notifyObservers(resultJson);
                        }
                        else {
                            SongJsonParser parser = new SongJsonParser();
                            songs.addAll(parser.getSongList(resultJson, pids));
                            //Cache only offline and shared as of now.
                                if (pidType == PidType.Offline || pidType == PidType.Shared) {
                                    SongCacheManager cacheManager = new SongCacheManager(context);
                                    cacheManager.cacheSongs(songs, pidType);
                                }
                            setChanged();
                            notifyObservers(songs);
                        }
                    }
                }
            }.execute();
    }

    private String makeQueryString(List<String> pids) {

        String query_string = "";
        for (String pid: pids) {
            query_string = query_string+pid+",";
        }
        return query_string;
    }




}
