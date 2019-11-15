package com.athena.athena;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.app.Notification.DEFAULT_VIBRATE;
import static org.xmlpull.v1.XmlPullParser.TYPES;

/**
 * Created by Jamiro on 30/10/2019.
 */

public class Handler extends Activity{
    String TAG = "Handler";
    Context context;

    boolean animerefresh;
    boolean weatherrefresh;
    boolean calendarrefresh;

    TextView temperatureview;
    TextView eventview;
    TextView text_anime;
    TextView text_weather;
    TextView text_calendar;

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

    //building UI
    CardView card_anime;
    CardView card_weather;
    CardView card_calendar;
    LinearLayout mainlayout;
    LinearLayout layout_anime;
    LinearLayout layout_weather;
    LinearLayout layout_calendar;

    FrameLayout.LayoutParams hideparams;
    FrameLayout.LayoutParams showparams;
    FrameLayout.LayoutParams invisparams;
    TextView anime_empty;
    TextView weather_empty;
    TextView calendar_empty;

    DisplayMetrics displayMetrics;

    int height;
    int width;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        final WindowManager w = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        final Display d = w.getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width  = displayMetrics.widthPixels;

        //invisview.setLayoutParams(invisparams);

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
                            final TextView textView = buildsubtext("Address is: " + calendar.get("location").toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                    if (calendarrefresh) {
                                        layout_calendar.removeAllViews();
                                        calendarrefresh = false;
                                    }
                                    text_calendar.setLayoutParams(showparams);
                                    layout_calendar.removeAllViews();
                                    layout_calendar.addView(calendar_empty);
                                    layout_calendar.addView(textView);
                                    text_calendar.setText(calendar.get("event").toString());
                                    calendarrefresh = true;
                                }
                            });
                        }
                        if (skey.equals("weather")){
                            editor.putString("weather", maptoJSON(motd.get("weather")));
                            editor.commit();
                            final Map<String, Object> weather = parseJSON(new ObjectMapper().writeValueAsString(motd.get("weather")));
                            final int temperature = Math.round(Float.valueOf(weather.get("temperature").toString()));
                            final TextView textView = buildsubtext("Windspeed is: " + weather.get("windspeed").toString() + "km/h");
                            final ImageView imageView = new ImageView(context);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                    if (weatherrefresh) {
                                        layout_weather.removeAllViews();
                                        weatherrefresh = false;
                                    }
                                    text_weather.setLayoutParams(showparams);
                                    Picasso.get().load(weather.get("iconurl").toString()).resize(500, 500).into(imageView);
                                    layout_weather.removeAllViews();
                                    layout_weather.addView(weather_empty);
                                    layout_weather.addView(imageView);
                                    layout_weather.addView(textView);
                                    text_weather.setText(temperature +" degrees");
                                    weatherrefresh = true;
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "motd error: " + e.getMessage());
                    e.printStackTrace();
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
                    final TextView textView = buildsubtext("It's episode: " + anime.get("episode").toString());
                    final ImageView imageView = new ImageView(context);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                            if (animerefresh){
                                layout_anime.removeAllViews();

                                Log.d(TAG, "run: ANIME: removed views" );
                                animerefresh = false;
                            }
                            Picasso.get().load(anime.get("imagelink").toString()).into(imageView);
                            text_anime.setLayoutParams(showparams);
                            layout_anime.removeAllViews();
                            layout_anime.addView(anime_empty);
                            layout_anime.addView(imageView);
                            layout_anime.addView(textView);
                            text_anime.setText(anime.get("title").toString());
                            animerefresh = true;
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

    public String parseTime(String time){
        Log.i(TAG, "parseTime: time: " + time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        Date convertedDate;
        String finalDateString = "";
        try {
            convertedDate = dateFormat.parse(time);
            SimpleDateFormat sdfnewformat = new SimpleDateFormat("EEE dd MMMM, HH:mm");
            finalDateString = sdfnewformat.format(convertedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return finalDateString;
    }

    public TextView buildsubtext(String text){
        final TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(20);
        textView.setGravity(17);
        textView.setFocusable(false);
        return textView;
    }


}

