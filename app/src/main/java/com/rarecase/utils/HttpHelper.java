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
import java.util.List;
import java.util.Map;


/**
 * Helper class for working with a remote server
 */
public class HttpHelper {

    /**Returns a Bitmap of image from a URL
     *
     * @param image_url URL of the image
     * @return {@code Bitmap object}
     */
    public static Bitmap downloadImage(String image_url){
        InputStream is = null;
        try {
            HttpURLConnection conn = setupConnection(image_url);
            if(conn != null) {
                is = conn.getInputStream();
            }
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
     * Gets the redirect URL by doing an HTTP GET on the given address.     *
     * @param address
     * @return Returns a URL in string or null if response doesn't contain the 2 required headers: status(should be 301) and location
     */
    public static String getRedirectURL(String address){

        try {
            HttpURLConnection conn = setupConnection(address);
            if(conn != null) {
                int responseCode = conn.getResponseCode();
                Map<String, List<String>> map = conn.getHeaderFields();

                if(responseCode == 301 || responseCode == 302){
                    return map.get("location").get(0);
                }
            }
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
        }
        return null;
    }

    /**
     * Sets up an HTTP connection
     * @param address
     * @return {@code HttpURLConnection} object tied to address parameter
     *
     */
    private static HttpURLConnection setupConnection(String address) throws IOException {
        HttpURLConnection conn = null;
        int responseCode = 404;
        try {
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            responseCode = conn.getResponseCode();
        }
        catch (IOException ioe){
            Log.i("HttpHelper:","IOException in setupConnection. Address:"+address);
        }
        if (responseCode != 200 && responseCode != 301 ) {
            throw new IOException("Got response code "+responseCode);
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