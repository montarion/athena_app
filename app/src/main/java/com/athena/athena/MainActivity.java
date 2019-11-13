package com.athena.athena;
// for dragging things nicely: https://medium.com/over-engineering/hands-on-with-material-components-for-android-cards-311b00a5ea3
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

import static android.app.Notification.DEFAULT_VIBRATE;
//TODO: figure out how to cut long text short gracefully(with trailing elipse)
//TODO: figure out how to get location


public class MainActivity extends AppCompatActivity {
    final String TAG = "main";
    private GestureDetector gestureDetector;
    public View vTouch;
    calendarActivity calendar;
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


    ExpandableListView expandableListView;

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

        hideparams = new FrameLayout.LayoutParams(anime_head.getLayoutParams());
        hideparams.gravity = Gravity.CENTER;

        showparams = new FrameLayout.LayoutParams(anime_head.getLayoutParams());
        showparams.gravity = Gravity.TOP;

        Log.d(TAG, "onCreate: " + String.valueOf(expandableListView));
        final ExpendableListViewAdapter expListAdapter = new ExpendableListViewAdapter(this.context);
        //expListAdapter.childNames = new String[][]{{null, null},{"hey", "you"}};
        //expandableListView.setAdapter(expListAdapter);

        n1 = new networking();
        n1.editor = editor;
        n1.prefs = prefs;
        n1.context = context;

        handler = new Handler();
        handler.context = this;

        handler.prefs = prefs;
        handler.editor = editor;
        handler.notiservice = getSystemService(NotificationManager.class);
        handler.displayMetrics = new DisplayMetrics();
        handler.mainlayout = mainlayout;
        handler.showparams = showparams;
        handler.hideparams = hideparams;
        handler.layout_anime = findViewById(R.id.anime_layout);
        handler.layout_weather = findViewById(R.id.weather_layout);
        handler.layout_calendar = findViewById(R.id.calendar_layout);
        handler.card_anime = anime;
        handler.text_anime = anime_head;
        handler.card_weather = weather;
        handler.text_weather = weather_head;
        handler.card_calendar = calendar;
        handler.text_calendar = calendar_head;

        handler.animerefresh = animerefresh;
        handler.weatherrefresh = weatherrefresh;
        handler.calendarrefresh = calendarrefresh;
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

        NotificationManager notificationManagercheck = getSystemService(NotificationManager.class);
        notificationManagercheck.createNotificationChannel(channel);

        //expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
        //    int previousItem = -1;
//
        //    @Override
        //    public void onGroupExpand(int groupPosition) {
        //        if(groupPosition != previousItem )
        //            expandableListView.collapseGroup(previousItem);
        //        previousItem = groupPosition;
        //    }
        //});
//
        //expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//
        //    @Override
        //    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        //        String name = expListAdapter.groupNames[groupPosition];
        //        Log.d(TAG, "onGroupClick: got touch from: " + name);
        //        if (name.equals("Anime")){
        //            networking.send("anime", "");
        //        }
        //        if(expandableListView.isGroupExpanded(groupPosition)){
        //            expandableListView.collapseGroup(groupPosition);
        //        } else {
        //            expandableListView.expandGroup(groupPosition, true);
        //        }
        //        return true;
        //    };
        //});

        anime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView topview = anime;

                View childview = topview.getChildAt(1);
                TransitionManager.beginDelayedTransition(mainlayout, new AutoTransition());
                if (childview.getVisibility() == View.GONE){
                    anime_head.setLayoutParams(showparams);
                    childview.setVisibility(View.VISIBLE);
                } else {
                    anime_head.setLayoutParams(hideparams);
                    childview.setVisibility(View.GONE);
                }
            }
        });
        anime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onClick: " + v.getTag().toString());
                networking.send("anime", "");
                return true;
            }
        });

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
