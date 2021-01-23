package com.rarecase.model.json;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rarecase.model.Song;
import com.rarecase.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class SongJsonDeserializer implements JsonDeserializer<List<Song>> {

    List<String> pids;

    SongJsonDeserializer(List<String> pids){
        this.pids = pids;
    }
    @Override
    public List<Song> deserialize(JsonElement j, Type t, JsonDeserializationContext context){

        List<Song> songs = new ArrayList<Song>();
        try {
            if((pids != null) && (pids.size()!=0)){
                JsonObject song_details_wrapper = j.getAsJsonObject();
                for (String pid : pids) {
                    //Getting song details one by one using encrypted name/pid
                    //JSON Song object here
                    JsonObject jsong = song_details_wrapper.getAsJsonObject(pid);
                    if (jsong != null) {
                        JsonElement jid = jsong.get("id");
                        String id = jid.getAsString();

                        JsonElement jtitle = jsong.get("song");
                        String title = Utils.curateSongName(jtitle.getAsString());

                        JsonElement jalbum = jsong.get("album");
                        String album = jalbum.getAsString();

                        JsonElement jprimary_artits = jsong.get("primary_artists");
                        String primary_artists = jprimary_artits.getAsString();

                        JsonElement jfeatured_artists = jsong.get("featured_artists");
                        String featured_artists = jfeatured_artists.getAsString();

                        JsonElement jimage_url = jsong.get("image");
                        String image_url = jimage_url.getAsString();

                        JsonElement jperma_url = jsong.get("perma_url");
                        String perma_url = jperma_url.getAsString();

                        JsonElement jenc_media_url = jsong.get("encrypted_media_url");
                        String enc_media_url = jenc_media_url.getAsString();

                        JsonElement jduration = jsong.get("duration");
                        String duration = jduration.getAsString();

                        Song song = new Song(id, title, album, primary_artists, featured_artists, image_url, perma_url, enc_media_url, duration,null);
                        songs.add(song);
                    }
                }
            }else {
                JsonObject song_json = j.getAsJsonObject();
                JsonElement jsong_id = song_json.get("id");
                String song_id = jsong_id.getAsString();
                Song song = new Song(song_id);
                songs.add(song);
            }
        }catch(Exception e){
            Log.i("SongJsonDeserializer","Error deserializing JSON objects into Song objects");
        }


        return songs;
    }
}
