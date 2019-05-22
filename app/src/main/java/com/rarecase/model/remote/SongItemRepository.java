package com.rarecase.model.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;

import com.rarecase.model.Song;
import com.rarecase.model.SongCacheManager;
import com.rarecase.presenter.presenters.SongItemPresenter;
import com.rarecase.utils.HttpHelper;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Worker class to fetch details of individual song items.
 */

public class SongItemRepository extends Observable {

    private static Map<String,Bitmap> imgCache;
    private Context _context;

    @Override
    public synchronized void addObserver(Observer o) {
        if(o instanceof SongItemPresenter) {
            super.addObserver(o);
        }
    }

    //Initialized once in SongItemPresenter
    public SongItemRepository(Context context){
        _context = context;
        if(imgCache == null)
            imgCache = new SongCacheManager(context).getImageCache();
    }

    public Map<String,Bitmap> getImgCache(){
        return imgCache;
    }

    public void downloadAlbumArtAsync(Song song){

        new AsyncTask<String,Void,Pair<String,Bitmap>>(){
            @Override
            protected Pair<String,Bitmap> doInBackground(String... params) {
                String albumKey = params[0];
                String url = params[1];
                return new Pair<>(albumKey, HttpHelper.downloadImage(url));
            }
            @Override
            protected void onPostExecute(Pair<String,Bitmap> result){
                if(result != null){
                    if(result.second != null) {
                        //Cache image only if download successfully
                        imgCache.put(result.first, result.second);                          //Cache to app memory.
                        SongCacheManager songCacheManager = new SongCacheManager(_context);
                        songCacheManager.cacheImage(result.first, result.second);           //Cache to disk
                    }
                    setChanged();
                    notifyObservers(result);
                }
            }
        }.execute(song.getAlbum(),song.getImage_url());
    }
}
