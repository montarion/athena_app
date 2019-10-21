package com.athena.athena;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String TAG = "main";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View calview = (View) findViewById(R.id.rect_calend);
        

    }
}
