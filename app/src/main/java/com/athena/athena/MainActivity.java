package com.athena.athena;

import android.content.Context;

import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final String TAG = "main";
    private GestureDetector gestureDetector;
    public View vTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View backview = findViewById(R.id.rect_backgr);
        View baseview = findViewById(R.id.rect_base);
        View weatherview = findViewById(R.id.rect_weathe);
        View calview = (View) findViewById(R.id.rect_calend);
        View aniview = findViewById(R.id.rect_anime);
        View transitview = findViewById(R.id.rect_transi);
        View newsview = findViewById(R.id.rect_news);
        TextView event = findViewById(R.id.text_calend);
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        backview.setOnTouchListener(touchListener);
        baseview.setOnTouchListener(touchListener);
        weatherview.setOnTouchListener(touchListener);
        calview.setOnTouchListener(touchListener);
        aniview.setOnTouchListener(touchListener);
        transitview.setOnTouchListener(touchListener);
        newsview.setOnTouchListener(touchListener);

        networking n1 = new networking();
        n1.context = this;
        n1.base = this.findViewById(R.id.rect_base);
        n1.eventview = event;
        baseview.setBackgroundColor(getResources().getColor(R.color.green));
        n1.listen();


    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            vTouch = v;
            return gestureDetector.onTouchEvent(event);

        }
    };

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{

        public static final int SWIPE_THRESHOLD = 100;
        public static final int SWIPE_VELOCITY_THRESHOLD = 500;
        String direction = "";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");

            // don't return false here or else none of the other
            // gestures will work
            return true;
        }

        public boolean swipehandler(String direction, float velocity) {
            String name = vTouch.getTag().toString();
            int maxval = 0;
            int minval = 0;
            int rightlongmax = 600;
            int leftlongmin = -600;
            int rightshortmax = 800;
            int leftshortmin = -800;
            Log.d(TAG, "swipehandler: view " + name + " got an " + direction + "ward swipe with velocity: " + velocity + "!");
            if (direction.equals("right")){
                if (name.equals("weather")){
                    Log.d(TAG, "swipehandler: Expanding weather");
                    maxval = rightshortmax;
                } else if (name.equals("anime")){
                    Log.d(TAG, "swipehandler: Expanding anime");
                    maxval = rightlongmax;
                }

            }
            if (direction.equals("left")){
                if (name.equals("calendar")){
                    Log.d(TAG, "swipehandler: Expanding calendar");
                    minval = leftlongmin;
                } else if (name.equals("transit")){
                    Log.d(TAG, "swipehandler: Expanding transit");
                    minval = leftshortmin;
                }
            }
            if (direction.equals("up")){
                if (name.equals("news")){
                    Log.d(TAG, "swipehandler: Expanding news");
                }
                //TODO: implement gesture control for standard scrolling of base/background
            }
            FlingAnimation fling = null;
            vTouch.setElevation(8);
            if (name.equals("base") || name.equals("background") || name.equals("news")) {
                fling = new FlingAnimation(findViewById(vTouch.getId()), DynamicAnimation.TRANSLATION_Y);
                fling.setStartVelocity(velocity)
                        .setFriction(1.1f)
                        .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_SCALE)
                        .start();
            }else{
                fling = new FlingAnimation(findViewById(vTouch.getId()), DynamicAnimation.TRANSLATION_X);
                fling.setStartVelocity(velocity)
                        .setFriction(1.1f)
                        .setStartValue(0)
                        .setMinValue(minval)
                        .setMaxValue(maxval)
                        .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_SCALE)
                        .start();
            }



            Log.d(TAG, "swipehandler: " + fling.toString());

            return true;
        }
        @Override
        public boolean onFling(MotionEvent downEvent, MotionEvent moveEvent, float velocityX, float velocityY) {
            Log.d(TAG, "onFling: did it!");
            float diffX = moveEvent.getX() - downEvent.getX();
            float diffY = moveEvent.getY() - downEvent.getY();
            int id = vTouch.getId();
            // which one is greater? movement across x or y?
            if (Math.abs(diffX) > Math.abs(diffY)) {
                //left of right swipe
                if(Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD){
                    if (diffX > 0) {
                        direction = "right";
                        Boolean result = swipehandler(direction, velocityX);
                        return result;
                    } else {
                        direction = "left";
                        Boolean result = swipehandler(direction, velocityX);
                        return result;
                    }
                }
            } else{
                //up or down
                if(Math.abs(diffY) >SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD){
                    if (diffY > 0){
                        direction = "down";
                        Boolean result = swipehandler(direction, velocityY);
                        return result;
                    } else {
                        direction = "up";
                        Boolean result = swipehandler(direction, velocityX);
                        return result;
                    }
                }
            }

            return false;
        }
    }

}
