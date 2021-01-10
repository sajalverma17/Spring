package com.rarecase.model.json;

import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rarecase.model.Song;

import java.lang.reflect.Type;
import java.util.List;

public class SongJsonParser {

    private Type songListType;

    public SongJsonParser(){
        songListType = new TypeToken<List<Song>>(){}.getType();
    }
    /**
     *Parses API's response JSON string containing song details into full-fledged song objects.
     *@param json_string JSON String having song details
     *@param pids  pids for matching songs with their corresponding song details.
     *@return  {@code List} of {@code Song}s
     */
    public List<Song> getSongList(String json_string, @Nullable List<String> pids){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(songListType, new SongDeserializer(pids));
        Gson g = builder.create();
        return g.fromJson(json_string,songListType);
    }

    /**Parses {@code json_string} to read the pid from HTML source code into a {@code Song} object.
     * This method is used to convert song details fetched from HTML source to a song object
     * with only pid field in it, which is later passed to the API call to get all details, most importantly:
     * the "encrypted_media_url"
     *@param json_string JSON String having song details from HTML source
     *@return  Returns song object {@code Song} with Pid field in it
     */
    public Song getSong(String json_string){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(songListType,new SongDeserializer(null));
        Gson g = builder.create();
        List<Song> songs = g.fromJson(json_string,songListType);
        return songs.get(0);
    }






}
