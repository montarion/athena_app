package com.athena.athena;

import android.app.Application;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class calendarActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, Object> agenda;
    TextView calview;
    TextView dateview;
    TextView addressview;
    String TAG = "calendarActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        prefs = getApplicationContext().getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        calview = findViewById(R.id.calview);
        dateview = findViewById(R.id.date);
        addressview = findViewById(R.id.address);
        String test = "{\"agenda\":{\"start\":\"2019-10-25 13:20:00+02:00\",\"event\":\"Keyboard\",\"end\":\"2019-10-25 13:40:00+02:00\",\"ongoing\":false,\"location\":\"Library Idea Zeist, Markt 1, 3701 JZ Zeist, Netherlands\"},\"weather\":\"it's warm\"}";
        String agendastring = prefs.getString("agenda", test);
        try{
            agenda = parseJSON(agendastring);
            Log.d(TAG, "onCreate: " + agenda.toString());
        }catch (Exception e){
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
        Log.d(TAG, "onCreate: " + agenda.keySet().toString());
        Log.d(TAG, "onCreate: " + agenda.get("event").toString());
        String event = agenda.get("event").toString();
        String date = parseTime(agenda.get("start").toString());
        String address = agenda.get("location").toString();
        calview.setText(event);
        dateview.setText(date);
        addressview.setText(address);
    }

    public void display(){
        //calview.setText(agenda.get("event").toString());

    }

    public HashMap<String, Object> parseJSON(String command){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        HashMap<String, Object> response = null;
        try {
            response = mapper.readValue(command, HashMap.class);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
            response.put("hey", "there");
        }
        return response;
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

}
