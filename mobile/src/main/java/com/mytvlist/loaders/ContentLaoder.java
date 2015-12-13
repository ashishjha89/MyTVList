package com.mytvlist.loaders;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class ContentLaoder {

    private final static String TAG = "ContentLaoder";

    public static String getContent(String urlPath) {
        HttpURLConnection connection;
        try {
            URL url = new URL(urlPath);
            URLConnection urlConn = url.openConnection();
            connection = (HttpURLConnection) urlConn;
            connection.setRequestMethod("GET");
            // Add headers
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("trakt-api-version", "2");
            connection.setRequestProperty("trakt-api-key", "ff9f7b7caf6bea28e46c51f783934c01f3b7a907674818621c4cf2148784c46a");
            // Log.d(TAG, "sendRequest Before connection.connect() urlPath=" + urlPath);
            connection.connect();
        } catch (MalformedURLException e) {
            //Log.d(TAG, "sendRequest MalformedURLException");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // Log.d(TAG, "sendRequest IOException " + (e.getMessage()));
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // Log.d(TAG, "sendRequest Exception " + (e.getMessage()));
            e.printStackTrace();
            return null;
        }
        if (connection == null) {
            return null;
        }
        String response = null;

        try {
            int retCode = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();


            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                // Log.d(TAG, "sendRequest IOException");
                e.printStackTrace();
                return null;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            response = sb.toString();
            // Log.d(TAG, "code: " + retCode + " " + connection.getResponseMessage() + "URL: " + urlPath + " response=" + response);

        } catch (MalformedURLException e) {
            // Log.d(TAG, "sendRequest() MalformedURLException return code");
            e.printStackTrace();
        } catch (IOException e) {
            // Log.d(TAG, "sendRequest IOException return" + (e.getMessage()));
            e.printStackTrace();
        } catch (Exception e) {
            // Log.d(TAG, "sendRequest Exception return " + (e.getMessage()));
            e.printStackTrace();
            return null;
        }
        return response;
    }

    public static String getIMDBDetails(String urlPath) {
        HttpURLConnection connection;
        try {
            URL url = new URL(urlPath);
            URLConnection urlConn = url.openConnection();
            connection = (HttpURLConnection) urlConn;
            connection.connect();
        } catch (MalformedURLException e) {
            //Log.d(TAG, "getIMDBDetails() sendRequest MalformedURLException");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            //Log.d(TAG, "getIMDBDetails() sendRequest IOException " + (e.getMessage()));
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            // Log.d(TAG, "getIMDBDetails() sendRequest Exception " + (e.getMessage()));
            e.printStackTrace();
            return null;
        }
        if (connection == null) {
            return null;
        }
        String response = null;

        try {
            int retCode = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();


            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                // Log.d(TAG, "getIMDBDetails() sendRequest IOException");
                e.printStackTrace();
                return null;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            response = sb.toString();
            // Log.d(TAG, "getIMDBDetails() code: " + retCode + " " + connection.getResponseMessage() + "URL: " + urlPath /*+ " response=" + response*/);
        } catch (MalformedURLException e) {
            // Log.d(TAG, "getIMDBDetails() sendRequest() MalformedURLException return code");
            e.printStackTrace();
        } catch (IOException e) {
            // Log.d(TAG, "getIMDBDetails() sendRequest IOException return" + (e.getMessage()));
            e.printStackTrace();
        } catch (Exception e) {
            // Log.d(TAG, "getIMDBDetails() sendRequest Exception return " + (e.getMessage()));
            e.printStackTrace();
            return null;
        }
        return response;
    }
}
