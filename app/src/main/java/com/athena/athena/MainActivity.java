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

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        float dX, dY;

        public boolean onTouch(View v, MotionEvent event) {
            String name = v.getTag().toString();
            int x = (int) event.getX();
            int y = (int) event.getY();
            Log.d(TAG, "onTouch: " + event.toString());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", String.format("moving in %s: v(%d, %d)", name, x, y));
                    v.animate()
                            .x(event.getRawX() + dX - (v.getWidth() / 100))
                            .setDuration(1)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    break;
            }

            return true;
        }
    };

}
