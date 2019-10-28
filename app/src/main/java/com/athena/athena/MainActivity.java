package com.athena.athena;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
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
    networking n1;
    Context context = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        setContentView(R.layout.activity_main);
        View backview = findViewById(R.id.rect_backgr);
        View baseview = findViewById(R.id.rect_base);
        View weatherview = findViewById(R.id.rect_weathe);
        View calview = (View) findViewById(R.id.rect_calend);
        View aniview = findViewById(R.id.rect_anime);
        View transitview = findViewById(R.id.rect_transi);
        View newsview = findViewById(R.id.rect_news);
        TextView text_weather = findViewById(R.id.text_weather);
        TextView text_calendar = findViewById(R.id.text_calend);
        TextView text_anime = findViewById(R.id.text_anime);
        backview.setOnTouchListener(touchListener);
        baseview.setOnTouchListener(touchListener);
        weatherview.setOnTouchListener(touchListener);
        calview.setOnTouchListener(touchListener);
        aniview.setOnTouchListener(touchListener);
        transitview.setOnTouchListener(touchListener);
        newsview.setOnTouchListener(touchListener);

        n1 = new networking();
        n1.context = this;
        n1.base = this.findViewById(R.id.rect_base);
        n1.temperatureview = text_weather;
        n1.eventview = text_calendar;
        n1.text_anime = text_anime;
        n1.prefs = prefs;
        n1.editor = editor;
        n1.notiservice = getSystemService(NotificationManager.class);
        baseview.setBackgroundColor(getResources().getColor(R.color.green));
        n1.listen();

        //ask for permissions
        //String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO};
        //ActivityCompat.requestPermissions(this, permissions, 1);








    }
    public void clickget(View v){
        String name = v.getTag().toString();
        if (name.equals("text_anime")) {
            n1.send("anime", "");
        }
    }
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        float x1, x2, y1, y2;
        int SWIPE_THRESHOLD = 300;
        public boolean onTouch(View v, MotionEvent event) {
            String name = v.getTag().toString();
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:

                    //Log.i(TAG, "onTouch: dX" + String.valueOf(v.getX() - event.getRawX()));
                    Log.i(TAG, "onTouch: dY" + String.valueOf(v.getY() - event.getRawY()));
                    Log.i(TAG, "onTouch: finger position: " + String.valueOf(v.getY()));
                    //ViewGroup.LayoutParams mainlp = v.getLayoutParams();
                    //mainlp.height += v.getY() - event.getRawY();
                    //v.setLayoutParams(mainlp);


                    //v.requestLayout();
                    //Log.i("TAG", String.format("moving in %s: (%e, %e)", name, event.getRawX(), event.getRawY()));
                    //v.animate()
                      //      .x(event.getRawX() + dX - (v.getWidth() / 100))
                      //      .setDuration(1)
                      //      .start();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    x2 = event.getX();
                    y2 = event.getY();
                    Log.d(TAG, "onTouch: " + String.valueOf(x1 - x2));
                    if (name.equals("calendar")) {
                        if (x1 - SWIPE_THRESHOLD > x2) {
                            Log.d(TAG, "onTouch: got leftward swipe!");
                            Intent intent = new Intent(getBaseContext(), calendarActivity.class);
                            startActivity(intent);
                        } else {
                            n1.send("calendar", "");
                        }
                    } if (name.equals("news")){
                        if (y1 - SWIPE_THRESHOLD > y2){
                            Log.d(TAG, "onTouch: upward swipe");
                        } else{
                            n1.notification("test", "test text", "whatevs");
                        }
                    } if (name.equals("anime")){
                        if (x1 + SWIPE_THRESHOLD < x2) {
                            Log.d(TAG, "onTouch: got rightward swipe!");
                            Intent intent = new Intent(getBaseContext(), animeActivity.class);
                            startActivity(intent);
                        } else {
                            n1.send("anime", "");
                        }
                    } if (name.equals("weather")){
                        if (x1 + SWIPE_THRESHOLD/2 < x2){
                            Log.d(TAG, "onTouch: got rightward swipe!");
                            Intent intent = new Intent(getBaseContext(), weatherActivity.class);
                            startActivity(intent);
                        } else {
                            n1.send("weather", "");
                        }
                    } else {
                            Log.d(TAG, "onTouch: name is: " + name);
                        }

                    break;
            }

            return true;
        }
    };



}
