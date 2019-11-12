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
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

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


    ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        setContentView(R.layout.activity_main);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListView.invalidateViews();
        Log.d(TAG, "onCreate: " + String.valueOf(expandableListView));
        final ExpendableListViewAdapter expListAdapter = new ExpendableListViewAdapter(this.context);
        //expListAdapter.childNames = new String[][]{{null, null},{"hey", "you"}};
        expandableListView.setAdapter(expListAdapter);

        n1 = new networking();
        n1.editor = editor;
        n1.prefs = prefs;
        n1.context = context;

        handler = new Handler();
        handler.context = this;

        handler.prefs = prefs;
        handler.editor = editor;
        handler.notiservice = getSystemService(NotificationManager.class);
        handler.expListAdapter = expListAdapter;
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

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousItem = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousItem )
                    expandableListView.collapseGroup(previousItem);
                previousItem = groupPosition;
            }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                String name = expListAdapter.groupNames[groupPosition];
                Log.d(TAG, "onGroupClick: got touch from: " + name);
                if (name.equals("Anime")){
                    networking.send("anime", "");
                }
                if(expandableListView.isGroupExpanded(groupPosition)){
                    expandableListView.collapseGroup(groupPosition);
                } else {
                    expandableListView.expandGroup(groupPosition, true);
                }
                return true;
            };


        });
        expListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                Log.d(TAG, "onChanged: CHANGE");
                super.onChanged();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        expandableListView.invalidateViews();
                    }
                });

            }
        });


    }






}
