package com.athena.athena;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    int notid;

    //building UI
    ViewGroup v;
    CardView card_weather;
    CardView card_calendar;
    LinearLayout mainlayout;
    LinearLayout layout_weather;
    LinearLayout layout_calendar;

    FrameLayout.LayoutParams hideparams;
    FrameLayout.LayoutParams showparams;
    FrameLayout.LayoutParams invisparams;
    FrameLayout.LayoutParams fullscreenparams;
    //TextView anime_empty;
    TextView weather_empty;
    TextView calendar_empty;

    DisplayMetrics displayMetrics;

    int height;
    int width;

    networking n1;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        final WindowManager w = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        final Display d = w.getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width  = displayMetrics.widthPixels;
        setContentView(R.layout.activity_main);


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
            //debug
            Log.i(TAG, "RUN: mainlayout kids : " + mainlayout.getChildCount());

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
                            if (v.findViewWithTag(skey+"_cardview") == null){
                                Log.i(TAG, "run: mainlayout kids before: " + mainlayout.getChildCount());
                                final CardView cardView = buildcard(skey);
                                Log.i(TAG, "onMessage: cardview right before: " + cardView.getTag());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                        Log.i(TAG, "run: cardview: " + cardView.getTag());
                                        mainlayout.addView(cardView);
                                        Log.i(TAG, "run: mainlayout is updated");

                                        //v = (ViewGroup) cardView;
                                    }});

                                Log.i(TAG, "run: mainlayout kids after: " + mainlayout.getChildCount());
                                Log.i(TAG, "run: mainlayout kids child 3: " + mainlayout.getChildAt(3));
                                Log.d(TAG, "onMessage: CURRENTLY MAINLAYOUT HAS THIS NUMBER OF CHILDREN: " + mainlayout.getChildCount());
                                int currentchildren =mainlayout.getChildCount();
                                while (mainlayout.getChildCount() < currentchildren+1){
                                }
                            }
                            editor.putString("calendar", maptoJSON(motd.get("calendar")));
                            editor.commit();

                            final CardView card = v.findViewWithTag(skey+"_cardview");
                            ViewGroup v1 = (ViewGroup) card;
                            final TextView headtext = v1.findViewWithTag(skey+"_headtext");
                            final LinearLayout layout = v1.findViewWithTag(skey+"_layout");
                            final TextView empty = v1.findViewWithTag(skey+"_empty");
                            final Map<String, Object> calendar = parseJSON(new ObjectMapper().writeValueAsString(motd.get("calendar")));
                            Log.d(TAG, "onMessage: calendar: " + calendar);
                            final String title = calendar.get("event").toString();
                            final String cat = skey;

                            Log.d(TAG, "call: calendar" + calendar.toString());
                            Log.d(TAG, "call: calendar" + calendar.get("event"));
                            calendar_empty.setText(calendar.get("event").toString());


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                    if (calendarrefresh) {
                                        layout.removeAllViews();
                                        calendarrefresh = false;
                                    }
                                    headtext.setLayoutParams(showparams);
                                    layout.removeAllViews();
                                    card.removeAllViews();
                                    card.addView(headtext);
                                    empty.setVisibility(View.INVISIBLE);
                                    empty.setText(title);
                                    layout.addView(empty);
                                    if (calendar.containsKey("location")) {
                                        TextView subText = buildtext("Address is: " + calendar.get("location").toString(), 20);
                                        subText.setTag(cat+"+subtext");
                                        layout.addView(subText);
                                    }
                                    headtext.setText(title);
                                    card.addView(layout);
                                    calendarrefresh = true;
                                }
                            });
                        }
                        if (skey.equals("weather")){

                            if (v.findViewWithTag(skey+"_cardview") == null){
                                Log.i(TAG, "run: mainlayout kids before: " + mainlayout.getChildCount());
                                final CardView cardView = buildcard(skey);
                                Log.i(TAG, "onMessage: cardview right before: " + cardView.getTag());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                        Log.i(TAG, "run: cardview: " + cardView.getTag());
                                        mainlayout.addView(cardView);
                                        Log.i(TAG, "run: mainlayout is updated");

                                        //v = (ViewGroup) cardView;
                                    }});

                                Log.i(TAG, "run: mainlayout kids after: " + mainlayout.getChildCount());
                                Log.i(TAG, "run: mainlayout kids child 3: " + mainlayout.getChildAt(3));
                                Log.d(TAG, "onMessage: CURRENTLY MAINLAYOUT HAS THIS NUMBER OF CHILDREN: " + mainlayout.getChildCount());
                                int currentchildren =mainlayout.getChildCount();
                                while (mainlayout.getChildCount() < currentchildren+1){
                                }
                            }


                            editor.putString("weather", maptoJSON(motd.get("weather")));
                            editor.commit();

                            final CardView card = v.findViewWithTag(skey+"_cardview");

                            Log.i(TAG, "onMessage: cardview card is: " + card);
                            ViewGroup v1 = (ViewGroup) card;
                            final TextView headtext = v1.findViewWithTag(skey+"_headtext");

                            final LinearLayout layout = v1.findViewWithTag(skey+"_layout");
                            final TextView empty = v1.findViewWithTag(skey+"_empty");
                            final Map<String, Object> weather = parseJSON(new ObjectMapper().writeValueAsString(motd.get("weather")));
                            final int temperature = Math.round(Float.valueOf(weather.get("temperature").toString()));
                            final String title = temperature + " degrees";

                            final TextView subText = buildtext("Windspeed is: " + weather.get("windspeed").toString() + "km/h", 20);
                            subText.setTag(skey+"_subtext");

                            final ImageView imageView = new ImageView(context);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                    if (weatherrefresh) {
                                        layout.removeAllViews();
                                        weatherrefresh = false;
                                    }
                                    headtext.setLayoutParams(showparams);
                                    Picasso.get().load(weather.get("iconurl").toString()).resize(500, 500).into(imageView);
                                    layout.removeAllViews();
                                    card.removeAllViews();
                                    card.addView(headtext);
                                    empty.setVisibility(View.INVISIBLE);
                                    empty.setText(title);
                                    layout.addView(empty);
                                    layout.addView(imageView);
                                    layout.addView(subText);
                                    headtext.setText(title);
                                    card.addView(layout);
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
                    // first check if there's a card
                    if (v.findViewWithTag(key+"_cardview") == null){
                        Log.i(TAG, "run: mainlayout kids before: " + mainlayout.getChildCount());
                        final CardView cardView = buildcard(key);
                        Log.i(TAG, "onMessage: cardview right before: " + cardView.getTag());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                                Log.i(TAG, "run: cardview: " + cardView.getTag());
                                mainlayout.addView(cardView);
                                Log.i(TAG, "run: mainlayout is updated");

                                //v = (ViewGroup) cardView;
                            }});

                        Log.i(TAG, "run: mainlayout kids after: " + mainlayout.getChildCount());
                        Log.i(TAG, "run: mainlayout kids child 3: " + mainlayout.getChildAt(3));
                        Log.d(TAG, "onMessage: CURRENTLY MAINLAYOUT HAS THIS NUMBER OF CHILDREN: " + mainlayout.getChildCount());
                        int currentchildren =mainlayout.getChildCount();
                        while (mainlayout.getChildCount() < currentchildren+1){
                        }
                    }
                    // now there definitely is!
                    // get vars
                    //ViewGroup v = (ViewGroup) mainlayout;


                    final CardView card = v.findViewWithTag(key+"_cardview");

                    Log.i(TAG, "onMessage: cardview card is: " + card);
                    ViewGroup v1 = (ViewGroup) card;
                    final TextView headtext = v1.findViewWithTag(key+"_headtext");

                    final LinearLayout layout = v1.findViewWithTag(key+"_layout");
                    final TextView empty = v1.findViewWithTag(key+"_empty");
                    final Map<String, Object> anime = parseJSON(new ObjectMapper().writeValueAsString(response.get("anime")));
                    final String title = anime.get("title").toString();
                    final String text = "You can watch episode " + anime.get("episode").toString() + " right now!";
                    editor.putString("title", anime.get("title").toString());
                    editor.putString("episode", anime.get("episode").toString());
                    editor.commit();
                    final String cat = key;
                    final TextView subText = buildtext("It's episode: " + anime.get("episode").toString(), 20);
                    subText.setTag(cat+"_subtext");

                    final Bitmap image = Picasso.get().load(anime.get("imagelink").toString()).get();
                    final int imageHeight = image.getHeight();
                    final int imageWidth = image.getWidth();
                    final float ratio = (float)imageWidth/imageHeight;
                    final int newHeight = (int)(layout.getWidth() * (ratio*1.2));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                            if (animerefresh){
                                layout.removeAllViews();
                                animerefresh = false;
                            }

                            Log.d(TAG, "run: height: " + imageHeight);
                            Log.d(TAG, "run: width: " + imageWidth);
                            Log.d(TAG, "run: thingwidth: " + layout.getWidth());
                            Log.d(TAG, "run: ratio: " + ratio);
                            Log.d(TAG, "run: newheight: " + newHeight);
                            layout.removeAllViews();
                            card.removeAllViews();
                            ImageView imageView = new ImageView(context);
                            imageView.setTag(cat+"_image");
                            Log.i(TAG, "run: SET IMAGE VIEW: " + imageView.getTag());
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setBackgroundResource(R.drawable.fade_image_background);
                            Picasso.get().load(anime.get("imagelink").toString()).resize(layout.getWidth(), newHeight).into(imageView);


                            headtext.setLayoutParams(showparams);
                            FrameLayout.LayoutParams testparams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,600);


                            headtext.setText(title);
                            card.addView(headtext);
                            empty.setVisibility(View.INVISIBLE);
                            empty.setText(title);
                            layout.addView(empty);
                            layout.addView(imageView);
                            layout.addView(subText);
                            card.addView(layout);
                            //layout.addView(imageView);


                            animerefresh = true;
                        }
                    });
                    int currentchildren = layout.getChildCount();
                    while (layout.getChildCount() < currentchildren+1){
                    }

                } catch (Exception e) {
                    Log.e(TAG, "anime error: " + e.getMessage());
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

    public TextView buildtext(String text, int textsize){
        final TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(textsize);
        textView.setGravity(17);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setFocusable(false);
        return textView;
    }

    public LinearLayout buildlinearlayout(){
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        // do fix animation
        LayoutTransition lt = new LayoutTransition();
        lt.enableTransitionType(LayoutTransition.CHANGING);
        linearLayout.setLayoutTransition(lt);
        return linearLayout;
    }
    public CardView buildcard(final String title){
        String oldTAG = TAG;
        TAG = "buildcard";
        Log.i(TAG, "buildcard: started building: " + title);
        // create main card
        final CardView cardView = new CardView(context);
        cardView.setTag(title+"_cardview");
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardDark));
        cardView.setMinimumHeight(70);
        LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardView.setLayoutParams(cardViewParams);
        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        cardViewMarginParams.setMargins(0, 5, 0, 5);
        cardView.requestLayout();  //Dont forget this line

        cardView.setCardElevation(40);
        cardView.invalidate();
        // add card to mainlayout
        //mainlayout.addView(cardView);
        Log.i(TAG, "buildcard: created card with tag: " + cardView.getTag());

        // add head text
        final TextView headtext = buildtext(title, 30);
        headtext.setTag(title+"_headtext");
        headtext.setGravity(Gravity.CENTER);
        headtext.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headtext.setVisibility(View.VISIBLE);
        // add headtext to card
        cardView.addView(headtext);
        Log.i(TAG, "buildcard: added head text with tag: " + headtext.getTag());

        // add linearview
        final LinearLayout linearLayout = buildlinearlayout();
        linearLayout.setTag(title+"_layout");


        // add layout to card
        cardView.addView(linearLayout);
        Log.i(TAG, "buildcard: created linear layout with tag: " + linearLayout.getTag());
        //add empty text
        TextView emptytext = buildtext("hey there!", 30);
        emptytext.setTag(title+"_empty");
        emptytext.setGravity(Gravity.CENTER_VERTICAL);
        emptytext.setVisibility(View.VISIBLE);
        Log.i(TAG, "buildcard: created empty text with tag: " + emptytext.getTag());
        // add empty text to layout
        linearLayout.addView(emptytext);
        linearLayout.setElevation(4);

        // do the click listeners
        Log.i(TAG, "buildcard: adding click listeners");
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                //View childview = linearLayout;
                View childview = cardView.getChildAt(1);
                Log.i(TAG, "onClick: CHILDVIEW TAG: " + childview.getTag());
                String currenttarget = v.getTag().toString().split("_")[0];



                if (childview.getVisibility() == View.GONE){
                    Log.i(TAG, "onClick: showing: " + currenttarget);
                    headtext.setLayoutParams(showparams);
                    headtext.setGravity(Gravity.TOP);
                    childview.setVisibility(View.VISIBLE);

                } else {
                    Log.i(TAG, "onClick: hiding: " + currenttarget);
                    headtext.setLayoutParams(hideparams);
                    childview.setVisibility(View.GONE);
                }
            }
        });
        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onClick: " + v.getTag().toString());
                networking.send(title, "");
                return true;
            }
        });
        Log.i(TAG, "buildcard: added click listeners.");
    TAG = oldTAG;
        Log.i(TAG, "buildcard: cardview: " + cardView.getTag());
    return cardView;
    }

}

