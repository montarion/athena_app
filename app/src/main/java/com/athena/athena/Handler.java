package com.athena.athena;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.Notification.DEFAULT_VIBRATE;

/**
 * Created by Jamiro on 30/10/2019.
 */

public class Handler extends Activity{
    String TAG = "Handler";
    Context context;

    View base;

    TextView temperatureview;
    TextView eventview;
    TextView text_anime;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    //gps stuff
    LocationManager locationManager;

    //notifications
    String notititle = "testtitle";
    String notitext = "testtext";
    String category = "standard";
    NotificationManagerCompat notificationManager;
    Object notiservice;
    int notid = 0;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    public void onMessage(String command){
        Map<String, Object> response = parseJSON(command);
        Log.d(TAG, "call: " + response.toString());
        Log.d(TAG, "call: " + response.keySet().toString());
        Iterator keys = response.entrySet().iterator();
        while (keys.hasNext()) {
            Map.Entry entry = (Map.Entry) keys.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            Log.d(TAG, "call: Key = " + key + ", Value = " + value.toString());
            if (key.equals("motd")) {
                try {
                    Map<String, Object> motd = parseJSON(new ObjectMapper().writeValueAsString(response.get("motd")));
                    Log.d(TAG, "call: motd keys: " + motd.keySet().toString());
                    Iterator skeys = motd.entrySet().iterator();
                    Log.d(TAG, "call: " + skeys.toString());
                    while (skeys.hasNext()) {
                        Map.Entry sentry = (Map.Entry) skeys.next();
                        String skey = (String) sentry.getKey();
                        Log.d(TAG, "call: " + skey);
                        if (skey.equals("calendar")){
                            editor.putString("calendar", maptoJSON(motd.get("calendar")));
                            editor.commit();
                            final Map<String, Object> calendar = parseJSON(new ObjectMapper().writeValueAsString(motd.get("calendar")));
                            Log.d(TAG, "call: calendar" + calendar.toString());
                            Log.d(TAG, "call: calendar" + calendar.get("event"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    eventview.setText(calendar.get("event").toString());
                                    Log.d(TAG, "run: DONE UPDATING CALENDAR!");
                                }
                            });
                        }
                        if (skey.equals("calendar")){
                            editor.putString("weather", maptoJSON(motd.get("weather")));
                            editor.commit();
                            final Map<String, Object> weather = parseJSON(new ObjectMapper().writeValueAsString(motd.get("weather")));
                            final int temperature = Math.round(Float.valueOf(weather.get("temperature").toString()));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    temperatureview.setText(String.valueOf(temperature) + "°");
                                    Log.d(TAG, "run: DONE UPDATING WEATHER!");
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "motd error: " + e.getMessage() + e.getCause());
                }
            }
            if (key.equals("anime")){
                try{
                    final Map<String, Object> anime = parseJSON(new ObjectMapper().writeValueAsString(response.get("anime")));
                    Log.d(TAG, "call THING: " + anime.toString());

                    Log.d(TAG, "call: anime" + anime.toString());
                    editor.putString("title", anime.get("title").toString());
                    editor.putString("episode", anime.get("episode").toString());
                    editor.commit();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            TextView animetext = text_anime;
                            animetext.setText(anime.get("title").toString());
                            Log.d(TAG, "run: DONE UPDATING!");
                            SystemClock.sleep(1000);
                            notification(anime.get("title").toString(), "now watchable", "anime");
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "anime error: " + e.getMessage() + e.getCause());
                }
            }
            if (key.equals("location")) {
                try {
                    final Map<String, Object> location = parseJSON(new ObjectMapper().writeValueAsString(response.get(key)));
                    Log.d(TAG, "onMessage: " + location);
                    if (location.get("command").equals("request")) {
                        Log.i(TAG, "onMessage: Got location request!");
                        String coords = getLocation();
                        networking.send("gpscoords", coords);
                    }
                }catch (Exception e){
                    ;
                }



            }
            if (key.equals("notification")) {
                try{
                    final Map<String, Object> notification = parseJSON(new ObjectMapper().writeValueAsString(response.get(key)));
                    Log.d(TAG, "call THING: " + notification.toString());
                    final String title = notification.get("notititle").toString();
                    final String text = notification.get("notitext").toString();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            notification(title, text, "alert");
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "notification error: " + e.getMessage() + e.getCause());
                }
            }
        }

    }

    public Map<String, Object> parseJSON(String command){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Log.d(TAG, "parseJSON: string to parse: " + command);
        Map<String, Object> response = null;
        try {
            response = mapper.readValue(command, HashMap.class);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
            response.put("hey", "there");
        }
        return response;
    }

    public String maptoJSON(Object command){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        String response = "null";
        try {
            response = mapper.writeValueAsString(command);
            Log.d(TAG, "maptoJSON() returned: " + response);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
        }
        return response;
    }

    public void notification(String notititle, String notitext, String category) {
        Log.d(TAG, "notification: here!");
        NotificationChannel channel = new NotificationChannel(category, category, NotificationManager.IMPORTANCE_HIGH);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManagercheck = (NotificationManager)notiservice;
        notificationManagercheck.createNotificationChannel(channel);
        notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, category)
                //smallicon must be located in app/src/main/res/something(drawable)/nameofimage
                .setSmallIcon(R.drawable.rect_bottom)
                .setContentTitle(notititle)
                .setContentText(notitext)
                .setDefaults(DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setChannelId(category);
        Log.i("status", "firing notification");

        notid =+ 1;

        notificationManager.notify(notid, mBuilder.build());
    }

    public String getLocation() {

        Log.d(TAG, "getLocation: inside!");
        Criteria criteria = new Criteria();
        String provider;
        String coordinates = null;
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "getLocation: providers are: " + provider);
        if (provider != null) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                final Map<String, String> gpscoords = new HashMap<String, String>();
                gpscoords.put("lat", String.valueOf(location.getLatitude()));
                gpscoords.put("lon", String.valueOf(location.getLongitude()));
                coordinates = maptoJSON(gpscoords);
                Log.d(TAG, "getLocation: " + coordinates);

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "getLocation: provider must be null");
        }
        return coordinates;
    }
}

