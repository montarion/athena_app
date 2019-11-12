package com.athena.athena;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Jamiro on 10/11/2019.
 */

public class ExpendableListViewAdapter extends BaseExpandableListAdapter {
    String TAG = "adapter";
    String[] groupNames = {"Anime", "Weather", "Calendar"};
    String[][] childNames = {{null}, {null}, {null, null}};
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    HashMap<String, Object> calendar;

    Context context;

    public ExpendableListViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return groupNames.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childNames.length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupNames[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childNames[groupPosition][childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        CardView cardView = new CardView(context);
        String name = groupNames[groupPosition];
        cardView.setTag(name);
        cardView.setContentPadding(100, 100, 100, 100);
        cardView.setMinimumHeight(200);
        cardView.setMinimumWidth(400);
        cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.cardDark));
        TextView textView = new TextView(context);
        textView.setText(groupNames[groupPosition]);
        textView.setTextSize(30);
        textView.setGravity(17); //Center variable
        textView.setFocusable(false);
        cardView.addView(textView);
        return cardView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        CardView cardView = new CardView(context);
        prefs = context.getSharedPreferences("athenaPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        String name = groupNames[groupPosition];
        Log.d(TAG, "getgroupView: " + (groupNames[groupPosition]));
        if ((childPosition < childNames[groupPosition].length) && !(childNames[groupPosition][childPosition] == null)) {
            cardView.setContentPadding(100, 100, 100, 100);
            cardView.setMinimumHeight(200);
            cardView.setMinimumWidth(400);


            cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.cardDark));
            TextView textView = new TextView(context);
            Log.d(TAG, "getChildView: " + Arrays.toString(childNames[groupPosition]));

            textView.setText(childNames[groupPosition][childPosition]);
            textView.setTextSize(20);
            textView.setGravity(17);
            textView.setFocusable(false);
            cardView.addView(textView);
        }
        return cardView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }




}
