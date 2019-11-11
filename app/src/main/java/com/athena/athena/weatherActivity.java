package com.athena.athena;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

public class weatherActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String TAG = "weather";
    ImageView iconview;
    TextView weatherview;

    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Object> weather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        prefs = getApplicationContext().getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;


        String weatherstring = prefs.getString("weather", "null");
        try{
            weather = parseJSON(weatherstring);
            Log.d(TAG, "onCreate: " + weather.toString());
        }catch (Exception e){
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
        String temperature = weather.get("temperature").toString();
        String windspeed = weather.get("windspeed").toString();
        String cloudpercentage = weather.get("cloudpercentage").toString();
        String rain = weather.get("rain").toString();
        String iconurl = weather.get("iconurl").toString();
        Log.d(TAG, "onCreate: " + iconurl);
        Picasso.get().load(iconurl).resize(width/2, width/2).into(iconview);
    }

    public HashMap<String, Object> parseJSON(String command){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        HashMap<String, Object> response = null;
        try {
            response = mapper.readValue(command, HashMap.class);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
            Log.d(TAG, "parseJSON: I tried to parse: " + command);
            response.put("hey", "there");
        }
        return response;
    }
}
