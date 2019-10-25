package com.athena.athena;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

public class animeActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String TAG = "anime";
    TextView aniview;
    TextView episode;
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Object> anime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime);
        prefs = getApplicationContext().getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        aniview = findViewById(R.id.aniview);
        episode = findViewById(R.id.episode);
        String episodename = prefs.getString("title", "null");
        String episodenumber = prefs.getString("episode", "null");

        aniview.setText(episodename);
        episode.setText(episodenumber);


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
