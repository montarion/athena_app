package com.athena.athena;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;




import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Jamiro on 21/10/2019.
 */

public class networking extends Activity{
    Context context;
    static String TAG = "Networking";
    static String status = "disconnected";
    static Socket socket;
    Context apcontext;

    View base;

    TextView temperatureview;
    TextView eventview;
    TextView text_anime;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_main);
        apcontext = getApplicationContext();


        Log.d(TAG, "onCreate: calling scheduler");



    }
    public static void connect(){
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            socket = IO.socket("http://83.163.109.161:7777");


            socket.connect();
            Log.d(TAG, "connect: " + socket.connected());
        } catch (URISyntaxException e ){
            Log.d(TAG, "instance initializer: error.");
        }
    }
    public void listen(){

        if (!status.equals("connected")) {
            Log.d(TAG, "listen: moving to connect");
            connect();
        }
        socket.on("connectmsg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "listen: got connect msg");
                send("socketACK", "greylynx");
            }
        }).on("socketSUCC", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: Got connected!");
                status = "connected";
                base.setBackgroundColor(ContextCompat.getColor(context, R.color._light_green));
                String[] opts = {"calendar", "weather"};
                send("motd", Arrays.toString(opts));




            }
        }).on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG, "call: " + Arrays.toString(args));
                int i;
                for (i=0; i<args.length; ++i){
                    Log.d(TAG, "call: " + args[i].toString());
                    String command = args[i].toString();
                    Map<String, Object> response = parseJSON(command);
                    Log.d(TAG, "call: " + response.toString());
                    Log.d(TAG, "call: " + response.keySet().toString());
                    Iterator keys = response.entrySet().iterator();
                    while (keys.hasNext()) {
                        Map.Entry entry = (Map.Entry) keys.next();
                        String key = (String) entry.getKey();
                        Object value = entry.getValue();
                        Log.d(TAG, "call: Key = " + key + ", Value = " + value.toString());
                        if (key.equals("motd")) {
                            try {
                                Map<String, Object> motd = parseJSON(new ObjectMapper().writeValueAsString(response.get("motd")));
                                Log.d(TAG, "call: motd keys: " + motd.keySet().toString());
                                Iterator skeys = motd.entrySet().iterator();
                                Log.d(TAG, "call: " + skeys.toString());
                                while (skeys.hasNext()) {
                                    Map.Entry sentry = (Map.Entry) skeys.next();
                                    String skey = (String) sentry.getKey();
                                    Log.d(TAG, "call: " + skey);
                                    if (skey.equals("calendar")){
                                        editor.putString("calendar", maptoJSON(motd.get("calendar")));
                                        editor.commit();
                                        final Map<String, Object> calendar = parseJSON(new ObjectMapper().writeValueAsString(motd.get("calendar")));
                                        Log.d(TAG, "call: calendar" + calendar.toString());
                                        Log.d(TAG, "call: calendar" + calendar.get("event"));
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                eventview.setText(calendar.get("event").toString());
                                                Log.d(TAG, "run: DONE UPDATING CALENDAR!");
                                            }
                                        });
                                    }
                                    if (skey.equals("calendar")){
                                        editor.putString("weather", maptoJSON(motd.get("weather")));
                                        editor.commit();
                                        final Map<String, Object> weather = parseJSON(new ObjectMapper().writeValueAsString(motd.get("weather")));
                                        final int temperature = Math.round(Float.valueOf(weather.get("temperature").toString()));
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                temperatureview.setText(String.valueOf(temperature) + "°");
                                                Log.d(TAG, "run: DONE UPDATING WEATHER!");
                                            }
                                        });
                                    }
                                }







                            } catch (Exception e) {
                                Log.e(TAG, "motd error: " + e.getMessage() + e.getCause());
                            }
                        }
                        if (key.equals("anime")){
                            try{
                                final Map<String, Object> anime = parseJSON(new ObjectMapper().writeValueAsString(response.get("anime")));
                                Log.d(TAG, "call THING: " + anime.toString());

                                Log.d(TAG, "call: anime" + anime.toString());
                                editor.putString("title", anime.get("title").toString());
                                editor.putString("episode", anime.get("episode").toString());
                                editor.commit();
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            TextView animetext = text_anime;
                                            animetext.setText(anime.get("title").toString());
                                            Log.d(TAG, "run: DONE UPDATING!");
                                        }
                                    });
                            } catch (Exception e) {
                                Log.e(TAG, "anime error: " + e.getMessage() + e.getCause());
                            }
                        }
                    }

                }
            }
        });
    }
    public void send(String key, Object value) {
            JSONObject obj = new JSONObject();
            try {
                obj.put(key, value);
            }catch (JSONException e){
                Log.e(TAG, e.getMessage() );
            }
            socket.emit("message", obj.toString());
    }

    public Map<String, Object> parseJSON(String command){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Log.d(TAG, "parseJSON: string to parse: " + command);
        Map<String, Object> response = null;
        try {
            response = mapper.readValue(command, HashMap.class);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
            response.put("hey", "there");
            }
        return response;
    }

    public String maptoJSON(Object command){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        String response = "null";
        try {
            response = mapper.writeValueAsString(command);
            Log.d(TAG, "maptoJSON() returned: " + response);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
        }
        return response;
    }

}
