package com.lalov.hitmonchance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

// Inspiration from demo with listview

public class UserAdaptor extends BaseAdapter {
    private Context context;
    private ArrayList<String> users;
    private String user;

    public UserAdaptor(Context c, ArrayList<String> userList) {
        this.context = c;
        this.users = userList;
    }

    @Override
    public int getCount() {
        if (users != null)
            return users.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        if (users != null)
            return users.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater userInflater =(LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = userInflater.inflate(R.layout.users_listview, null);
        }

        user = users.get(position);

        if (user != null) {
            TextView adaptorUser = (TextView) convertView.findViewById(R.id.txtUser);
            adaptorUser.setText(user);
        }

        return convertView;
    }

    public void UpdateUsers(ArrayList<String> userList) {
        users.clear();
        users.addAll(userList);
    }
}
