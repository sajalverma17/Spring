package com.rarecase.model.remote;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.rarecase.model.Song;
import com.rarecase.spring.R;
import com.rarecase.utils.HttpHelper;
import com.rarecase.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SongDownloadService extends IntentService {

    private static final String ACTION_DOWNLOAD = "com.rarecase.model.remote.action.DOWNLOAD_MEDIA";

    private static final String EXTRA_MEDIA_URL = "com.rarecase.model.remote.extra.MEDIA_URL";
    private static final String EXTRA_SONG = "com.rarecase.model.remote.extra.SONG";
    private static final String EXTRA_STORAGE_PATH = "com.rarecase.model.remote.extra.STORAGE_PATH";
    private static final String EXTRA_BITMAP = "com.rarecase.model.remote.extra.ALBUM_ART";

    private static final int NOTIFICATION_ID = 0;

    private static Handler uiHandler;

    NotificationCompat.Builder builder;

    NotificationManager notificationManager;

    public SongDownloadService() {
        super("SongDownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context c = getApplicationContext();
        builder = new NotificationCompat.Builder(c);
        builder.setContentTitle("Downloading");
        builder.setSmallIcon(R.drawable.notification_music_folder);
        builder.setColor(c.getResources().getColor(R.color.colorPrimary));
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * Starts this service to download a song. If this service is already performing
     * a download task, this new action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDownload(Context context, Song song, String url, @Nullable Bitmap albumArt, String storagePath) {
        Intent intent = new Intent(context, SongDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_MEDIA_URL, url);
        intent.putExtra(EXTRA_STORAGE_PATH, storagePath);
        //intent.putExtra(EXTRA_SONG, song);
        intent.putExtra(EXTRA_BITMAP, albumArt);
        uiHandler = new Handler();
        context.startService(intent);
    }

    public static void startActionDownload(Context context, Song song, String url, String storagePath){
        Intent intent = new Intent(context, SongDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_MEDIA_URL, url);
        intent.putExtra(EXTRA_STORAGE_PATH, storagePath);
        //intent.putExtra(EXTRA_SONG, song);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_DOWNLOAD)) {

                String media_url = intent.getStringExtra(EXTRA_MEDIA_URL);
                String storage_path = intent.getStringExtra(EXTRA_STORAGE_PATH);
                final Song song = intent.getParcelableExtra(EXTRA_SONG);
                Bitmap albumArt = intent.getParcelableExtra(EXTRA_BITMAP);

                File file = new File(storage_path + "/" + Utils.generateSpringActionFileName(song.getSong(),song.getId()));

                builder.setContentText(song.getSong());
                notificationManager.notify(NOTIFICATION_ID, builder.build());

                InputStream is = HttpHelper.downloadSong(media_url);
                if (is != null) {
                    OutputStream os;

                    byte[] byteBuffer = new byte[1024];
                    int value;
                    try {
                        os = new FileOutputStream(file);
                        while (((value = is.read(byteBuffer)) != -1)) {
                            os.write(byteBuffer, 0, value);
                        }
                        is.close();
                        os.close();
                    } catch (IOException e) {
                        Log.i("DownloadSongService", "IO exception: Download interrupted");
                        e.printStackTrace();
                        Utils.showToastFromService(uiHandler,getApplicationContext(),"Failed to download "+song.getSong()+" - Poor network");
                        return;
                    }
                    //Tag audio file only on successful download
                    Utils.tagAudioFile(song, albumArt, storage_path);
                    Utils.showToastFromService(uiHandler,getApplicationContext(),"Finished downloading "+song.getSong());

                } else {
                    Log.i("DownloadSongService", "Input stream null");
                    Utils.showToastFromService(uiHandler,getApplicationContext(),"Failed to start download of "+song.getSong());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
    }
}


