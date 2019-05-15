package com.lalov.hitmonchance.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;
import com.lalov.hitmonchance.UserAdaptor;

import java.util.ArrayList;

import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_USERS;
import static com.lalov.hitmonchance.Globals.SERVICE_TAG;

public class ChooseOpponent extends AppCompatActivity {

    private UserAdaptor userAdaptor;
    private ArrayList<String> userList;

    private ListView userListView;

    private ServiceConnection pokemonServiceConnection;
    private PokemonService pokemonService;
    private boolean bound = false;

    private BroadcastReceiver broadcastReceiverUsers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            InitUsers();
            userAdaptor.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_opponent);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int) (height*.8));

        SetupConnectionToPokemonService();
        BindToPokemonService();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverUsers,
                new IntentFilter(BROADCAST_RESULT_USERS));

        userListView = (ListView) findViewById(R.id.listViewUsers);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent pokemonIntent = getIntent();

                Intent userIntent = new Intent(ChooseOpponent.this, BattleActivity.class);
                userIntent.putExtra("positionPokemon", pokemonIntent.getIntExtra("position",0));
                userIntent.putExtra("positionOpponent", position);

                startActivity(userIntent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UnBindFromPokemonService();
    }

    private void InitUsers() {
        userList = pokemonService.GetAllUsers();
        userAdaptor = new UserAdaptor(this, userList);
        userListView.setAdapter(userAdaptor);
    }

    private void BindToPokemonService() {
        if (!bound) {
            bindService(new Intent(ChooseOpponent.this, PokemonService.class),
                    pokemonServiceConnection, Context.BIND_AUTO_CREATE);
            bound = true;
        }
    }

    private void UnBindFromPokemonService() {
        if (bound) {
            unbindService(pokemonServiceConnection);
            bound = false;
        }
    }

    private void SetupConnectionToPokemonService() {
        pokemonServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                pokemonService = ((PokemonService.PokemonServiceBinder)service).getService();
                Log.d(SERVICE_TAG, "ChooseOpponentActivity connected to pokemon service");

                pokemonService.GetAllUsersDatabase();
                //InitUsers();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                pokemonService = null;
                Log.d(SERVICE_TAG, "ChooseOpponentActivity disconnected from pokemon service");
            }
        };
    }
}
