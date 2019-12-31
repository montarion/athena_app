package com.athena.athena;
// for dragging things nicely: https://medium.com/over-engineering/hands-on-with-material-components-for-android-cards-311b00a5ea3
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Gravity;

import android.view.View;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

import static android.app.Notification.DEFAULT_VIBRATE;
//TODO: figure out how to cut long text short gracefully(with trailing elipse)


public class MainActivity extends AppCompatActivity {
    final String TAG = "main";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Handler handler;
    networking n1;
    Context context = this;

    boolean animerefresh = false;
    boolean weatherrefresh = false;
    boolean calendarrefresh = false;

    FrameLayout.LayoutParams hideparams;
    FrameLayout.LayoutParams showparams;
    FrameLayout.LayoutParams fullscreenparams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        setContentView(R.layout.activity_main);
        final CardView anime = findViewById(R.id.card_anime);
        final TextView anime_head = findViewById(R.id.anime_head);
        final CardView weather = findViewById(R.id.card_weather);
        final TextView weather_head = findViewById(R.id.weather_head);
        final CardView calendar = findViewById(R.id.card_calendar);
        final TextView calendar_head = findViewById(R.id.calendar_head);
        final LinearLayout mainlayout = findViewById(R.id.mainlayout);

        FrameLayout.LayoutParams invisparams;
        invisparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        invisparams.gravity = Gravity.TOP;
        ViewGroup v = (ViewGroup) mainlayout;


        //final TextView anime_empty;
        //anime_empty = new TextView(context);
        //anime_empty.setTextSize(30);
        //anime_empty.setText("");
        //anime_empty.setVisibility(View.INVISIBLE);

        final TextView weather_empty;
        weather_empty = new TextView(context);
        weather_empty.setTextSize(30);
        weather_empty.setText("");
        weather_empty.setVisibility(View.INVISIBLE);

        final TextView calendar_empty;
        calendar_empty = new TextView(context);
        calendar_empty.setTextSize(30);
        calendar_empty.setText("");
        calendar_empty.setVisibility(View.INVISIBLE);

        hideparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hideparams.gravity = Gravity.CENTER;

        showparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        showparams.gravity = Gravity.TOP;

        fullscreenparams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        n1 = new networking();
        n1.editor = editor;
        n1.prefs = prefs;
        n1.context = context;

        handler = new Handler();
        handler.context = this;
        handler.v = v;
        handler.notid = 0;
        handler.prefs = prefs;
        handler.editor = editor;
        handler.notiservice = getSystemService(NotificationManager.class);
        handler.displayMetrics = new DisplayMetrics();
        handler.mainlayout = mainlayout;
        handler.showparams = showparams;
        handler.hideparams = hideparams;
        handler.fullscreenparams = fullscreenparams;

        handler.layout_weather = findViewById(R.id.weather_layout);
        handler.layout_calendar = findViewById(R.id.calendar_layout);
        //handler.card_anime = anime;
        //handler.text_anime = anime_head;
        handler.card_weather = weather;
        handler.text_weather = weather_head;
        handler.card_calendar = calendar;
        handler.text_calendar = calendar_head;
        handler.weather_empty = weather_empty;
        handler.calendar_empty = calendar_empty;
        handler.invisparams = invisparams;

        handler.animerefresh = animerefresh;
        handler.weatherrefresh = weatherrefresh;
        handler.calendarrefresh = calendarrefresh;
        handler.n1 = n1;
        n1.handler = handler;
        Intent intent = new Intent(this, networking.class);

        Log.d(TAG, "onCreate: trying to start networking");
        startService(intent);
        Log.d(TAG, "onCreate: started networking");
        n1.listen();

        //ask for permissions
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 1);

        NotificationChannel channel = new NotificationChannel("foreground service", "foreground service", NotificationManager.IMPORTANCE_MIN);

        //NotificationManager notificationManagercheck = getSystemService(NotificationManager.class);
        //notificationManagercheck.createNotificationChannel(channel);
//
        //anime.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        CardView topview = anime;
//
        //        View childview = topview.getChildAt(1);
        //        TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
        //        if (childview.getVisibility() == View.GONE){
        //            anime_head.setLayoutParams(showparams);
        //            childview.setVisibility(View.VISIBLE);
        //        } else {
        //            anime_head.setLayoutParams(hideparams);
        //            childview.setVisibility(View.GONE);
        //        }
        //    }
        //});
        //anime.setOnLongClickListener(new View.OnLongClickListener() {
        //    @Override
        //    public boolean onLongClick(View v) {
        //        Log.d(TAG, "onClick: " + v.getTag().toString());
        //        networking.send("anime", "");
        //        return true;
        //    }
        //});

        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView topview = weather;
                Log.d(TAG, "onClick: " + v.getTag().toString());
                View childview = topview.getChildAt(1);
                TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                if (childview.getVisibility() == View.GONE){
                    weather_head.setLayoutParams(showparams);
                    childview.setVisibility(View.VISIBLE);
                } else {
                    weather_head.setLayoutParams(hideparams);
                    childview.setVisibility(View.GONE);
                }
            }
        });
        weather.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String[] opts = {"weather"};
                networking.send("motd", Arrays.toString(opts));
                return true;
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView topview = calendar;
                Log.d(TAG, "onClick: " + v.getTag().toString());
                View childview = topview.getChildAt(1);
                int i = 0;
                for (i = 0;i < calendar.getChildCount(); i++) {
                    Log.i(TAG, "onMessage: calendar tags: " + calendar.getChildAt(i).getTag());
                };
                i=0;
                LinearLayout calendar_layout = findViewById(R.id.calendar_layout);
                for (i = 0;i < calendar_layout.getChildCount(); i++) {
                    Log.i(TAG, "onMessage: calendar_layout tags: " + calendar_layout.getChildAt(i).getTag());
                };
                Log.d(TAG, "onClick: CALENDAR ONCLICK VIEW: " + childview.getTag());
                Log.d(TAG, "onClick: " + String.valueOf(childview.getVisibility()));
                TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                if (childview.getVisibility() == View.GONE){
                    calendar_head.setLayoutParams(showparams);
                    childview.setVisibility(View.VISIBLE);
                } else {
                    calendar_head.setLayoutParams(hideparams);
                    childview.setVisibility(View.GONE);
                }
            }
        });

        calendar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String[] opts = {"calendar"};
                networking.send("motd", Arrays.toString(opts));
                return true;
            }
        });



    }






}
