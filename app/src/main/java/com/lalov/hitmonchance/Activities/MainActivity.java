package com.lalov.hitmonchance.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonAdaptor;
import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;

import java.util.ArrayList;
import java.util.Calendar;

import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_LOCATION;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_POKEMON;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_USERNAME;
import static com.lalov.hitmonchance.Globals.SERVICE_TAG;

public class MainActivity extends AppCompatActivity {

    //TODO Error where pokemon is added to list twice

    private static final String TAG = "Tag123";
    private PokemonAdaptor pokemonAdaptor;
    private ListView pokemonListView;
    private ArrayList<Pokemon> pokemonList;
    Button btnAddPokemon;
    ImageButton imgBtnSettings;
    EditText txtSearchPokemon;
    TextView txtUser;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    private ServiceConnection pokemonServiceConnection;
    private PokemonService pokemonService;
    private boolean bound = false;
    private boolean newUser = true;

    private Location userLocation;
    private Location battleLocation;
    private String battleTime;

    private BroadcastReceiver broadcastReceiverPokemon = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            InitPokemon();
            pokemonAdaptor.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver broadcastReceiverUsername = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            txtUser.setText(pokemonService.GetUsername());
        }
    };

    private BroadcastReceiver broadcastReceiverLocation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            userLocation = pokemonService.GetCurrentLocation();
            battleLocation = pokemonService.GetBattleLocation();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SetupConnectionToPokemonService();
        BindToMovieService();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverPokemon,
                new IntentFilter(BROADCAST_RESULT_POKEMON));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverUsername,
                new IntentFilter(BROADCAST_RESULT_USERNAME));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverLocation,
                new IntentFilter(BROADCAST_RESULT_LOCATION));

        pokemonListView = (ListView) findViewById(R.id.ListViewPokedex);
        btnAddPokemon = findViewById(R.id.btnAdd);
        txtSearchPokemon = findViewById(R.id.editTextAdd);
        imgBtnSettings = findViewById(R.id.imgBtnLogout);
        txtUser = findViewById(R.id.textViewUser);

        docRef = db.collection("Users").document(currentUser.getUid());

        pokemonListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                startActivity(intent);
                return true;
            }
        });

        pokemonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ChooseOpponent.class);
                intent.putExtra("position", position);

                startActivity(intent);
            }
        });

        imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOut();
            }
        });

        btnAddPokemon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pokemonName = txtSearchPokemon.getText().toString().toLowerCase();
                pokemonService.AddPokemon(pokemonName);
                txtSearchPokemon.getText().clear();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UnBindFromMovieService();
    }

    /** ########################################################################################
     *  ######### Private methods ##############################################################
     *  ######################################################################################## */

    private void BindToMovieService() {
        if (!bound) {
            bindService(new Intent(MainActivity.this, PokemonService.class),
                    pokemonServiceConnection, Context.BIND_AUTO_CREATE);
            bound = true;
        }
    }

    private void UnBindFromMovieService() {
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
                Log.d(SERVICE_TAG, "MainActivity connected to pokemon service");

                final Intent intent = getIntent(); //TODO This might cause problems later, should be changed
                if (intent.hasExtra("Username") && newUser) {
                    pokemonService.CreateUser(intent.getStringExtra("Username"));
                    pokemonService.AddPokemon(intent.getStringExtra("PokemonName"));
                }

                newUser = false;

                pokemonService.GetUsernameDatabase();
                pokemonService.GetAllPokemonDatabase();
                pokemonService.GetBattleLocationAndTime();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                pokemonService = null;
                Log.d(SERVICE_TAG, "MainActivity disconnected from pokemon service");
            }
        };
    }

    private void InitPokemon() {
        pokemonList = pokemonService.GetAllPokemon();
        pokemonAdaptor = new PokemonAdaptor(this, pokemonList);
        pokemonListView.setAdapter(pokemonAdaptor);
    }

    private void SignOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
