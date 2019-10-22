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


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
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
                    Map<String, String> outmap = new HashMap<String, String>();
                    Log.d(TAG, "call: " + args[i].toString());
                    String command = args[i].toString();
                    JSONObject object = parseJSON(command);
                    Iterator<String> keys = object.keySet().iterator();
                    for (Iterator iterator = object.keySet().iterator(); iterator.hasNext();){
                        String key = (String)iterator.next();
                        Log.d(TAG, "call: currently working on: " + key);
                        if (key.equals("motd")){
                            Log.d(TAG, object.get(key).toString());
                            JSONObject contents = parseJSON(object.get(key).toString());
                            Log.d(TAG, "call: weather key");
                            Log.d(TAG, contents.get("weather").toString());
                            Log.d(TAG, contents.get("agenda").toString());
                        }
                    }
                }
            }
        });
    }
    public static void send(String key, Object value) {
            JSONObject obj = new JSONObject();
            obj.put(key, value);
            socket.emit("message", obj.toString());
    }

    public JSONObject parseJSON(String command){
        JSONObject object = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            object = (JSONObject) parser.parse(command);
            return object;
        }catch (ParseException e){
            Log.e(TAG, "parseJSON: "+e.getMessage().toString() );
            object.put("Failure", e.getMessage());

        }
        return object;
    }
}
