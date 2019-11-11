package com.athena.athena;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import static android.app.Notification.DEFAULT_VIBRATE;

/**
 * Created by Jamiro on 21/10/2019.
 */


public class networking extends Service{
    Context context;
    static String TAG = "Networking";
    static String status = "disconnected";
    static Socket socket;
    Context apcontext;

    View base;


    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    Handler handler;

    NotificationManagerCompat notificationManager;
    Object notiservice;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: in startcommand");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification realnotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.rect_base)
                .setContentTitle("Athena")
                .setContentText("Background Service is running..")
                .setContentIntent(pendingIntent)
                .setChannelId("foreground service")
                .build();
        startForeground(9999, realnotification);
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        apcontext = getApplicationContext();






    }
    public static void connect(){
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            socket = IO.socket("http://1.1.1.1:11111");


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
                    handler.onMessage(command);
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

    public void notification(String notititle, String notitext, String category) {
        Log.d(TAG, "notification: here!");
        NotificationChannel channel = new NotificationChannel(category, category, NotificationManager.IMPORTANCE_HIGH);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManagercheck = (NotificationManager)notiservice;
        notificationManagercheck.createNotificationChannel(channel);
        notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, category)
                //smallicon must be located in app/src/main/res/something(drawable)/nameofimage
                .setSmallIcon(R.drawable.rect_bottom)
                .setContentTitle(notititle)
                .setContentText(notitext)
                .setDefaults(DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setChannelId(category);
        Log.i("status", "firing notification");
        int notid = 0;
        notid =+ 1;

        notificationManager.notify(notid, mBuilder.build());
    }


}
