package com.athena.athena;

import android.app.Activity;
import android.content.Context;
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

    TextView eventview;

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
                base.setBackgroundColor(ContextCompat.getColor(context, R.color._light_green));
                String[] opts = {"agenda", "weather"};
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
                                Log.d(TAG, "call: motd" + motd.toString());

                                final Map<String, Object> agenda = parseJSON(new ObjectMapper().writeValueAsString(motd.get("agenda")));
                                String weather = motd.get("weather").toString();
                                Log.d(TAG, "call: agenda" + agenda.toString());
                                Log.d(TAG, "call: agenda" + agenda.get("event"));
                                Log.d(TAG, "call: weather " + weather);
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        eventview.setText(agenda.get("event").toString());
                                        Log.d(TAG, "run: DONE UPDATING!");
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "call: " + e.getMessage() + e.getCause());
                            }
                        }
                    }

                }
            }
        });
    }
    public static void send(String key, Object value) {
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

        Map<String, Object> response = null;
        try {
            response = mapper.readValue(command, HashMap.class);
        }catch (IOException e ){
            Log.d(TAG, "parseJSON: " + e.getMessage());
            response.put("hey", "there");
            }
        return response;
    }
}
