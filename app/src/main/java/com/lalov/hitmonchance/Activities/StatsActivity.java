package com.lalov.hitmonchance.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.lalov.hitmonchance.R;

public class StatsActivity extends AppCompatActivity {

    private String LOG = "StatsActivity: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Log.d(LOG,"created");
    }
}
