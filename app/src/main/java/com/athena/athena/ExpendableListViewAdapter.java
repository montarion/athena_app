package com.athena.athena;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Created by Jamiro on 10/11/2019.
 */

public class ExpendableListViewAdapter extends BaseExpandableListAdapter {

    String[] groupNames = {"test1", "test2"};
    String[][] childNames = {{"child1.1", "child1.2"}, {"child2.1", "child2.2"}};

    Context context;
    public ExpendableListViewAdapter(Context context){
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        textView.setText(groupNames[groupPosition]);
        textView.setTextSize(30);
        return textView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
