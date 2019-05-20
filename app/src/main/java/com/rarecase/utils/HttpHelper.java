package com.rarecase.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;


/**
 * Helper class for working with a remote server
 */
public class HttpHelper {

    /**Connects to the the url of the media file
     * @param url URL of the audio file
     * @return An input stream pointing to the media file. Null if an IO exception occurs.
     */
    public static InputStream downloadSong(String url){

        HttpURLConnection conn = setupConnection(url);
        InputStream inputStream;
        try {
            inputStream = conn.getInputStream();
        } catch (IOException e) {
            Log.i("HttpHelper: ","downloadSong() failed");
            return null;
        }
        if (inputStream != null) {
            return inputStream;
        }else{
            return  null;
        }
    }

    /**Returns a Bitmap of image from a URL
     *
     * @param image_url URL of the image
     * @return {@code Bitmap object}
     */
    public static Bitmap downloadImage(String image_url){

        HttpURLConnection conn = setupConnection(image_url);
        InputStream is ;
        try {
            is = conn.getInputStream();
        }catch (IOException ioe){
            Log.i("HttpHelper: ","downloadImage() failed");
            return null;
        }
        if(is != null) {
            return BitmapFactory.decodeStream(is);
        }else {
            return null;
        }
    }


    /**
     * Returns JSON text from a URL on a web server
     *
     * @param address API
     * @return JSON as string
     * */
    public static String downloadUrl(String address) {

        InputStream is = null;
        try {
            HttpURLConnection conn = setupConnection(address);
            if(conn != null) {
                is = conn.getInputStream();
                return readStream(is);
            }else{
                return null;
            }
            //This is a work around as cant think of a way to bubble up these three exception all the way to presenter
            //since the presenter gets the result of downloadUrl() as a string in the Overridden update method.
            //How to catch network exceptions in presenter???
        } catch (SocketTimeoutException ste){
            return "STE";
        } catch (UnknownHostException uhe){
            return "UHE";
        } catch (IOException e) {
            if(e.getMessage().contains("Got response code 503")) {
                return "503";
            }else {
                return "UNK";
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i("HttpHelper: ","DownloadUrl : IOException while closing Input stream");
                }
            }
        }
    }

    /**
     * Sets up an HTTP connection
     * @param address
     * @return {@code HttpURLConnection} object tied to address parameter
     *
     */
    private static HttpURLConnection setupConnection(String address){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200 ) {
                throw new IOException("Got response code "+responseCode);
            }
        }
        catch (IOException ioe){
            Log.i("HttpHelper:","IOException in setupConnection");
        }
        return conn;
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private static String readStream(InputStream stream) throws IOException {

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        BufferedOutputStream out = null;
        try {
            int length = 0;
            out = new BufferedOutputStream(byteArray);
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return byteArray.toString();
        } catch (IOException e) {
            Log.i("HttpHelper","Socket Timeout while reading stream");
            return "STE";
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


}