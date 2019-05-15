package com.lalov.hitmonchance.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonAdaptor;
import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;

import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_LOCATION;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_POKEMON;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_USERNAME;
import static com.lalov.hitmonchance.Globals.METERS_TO_WALK;
import static com.lalov.hitmonchance.Globals.PERMISSIONS_REQUEST_LOCATION;
import static com.lalov.hitmonchance.Globals.SERVICE_TAG;
import static com.lalov.hitmonchance.Globals.TIME_TO_PASS;

public class MainActivity extends AppCompatActivity {

    //TODO Error where pokemon is added to list twice

    private PokemonAdaptor pokemonAdaptor;
    private SwipeMenuListView pokemonListView;
    private ArrayList<Pokemon> pokemonList;
    Button btnAddPokemon;
    ImageButton imgBtnSettings, imgBtnMusic;
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
    private Date battleTime;
    MediaPlayer pokemonIntroSong;

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
            battleLocation = pokemonService.GetBattleLocation();
            battleTime = pokemonService.GetBattleTime();
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

        checkPermissions();

        pokemonListView = (SwipeMenuListView) findViewById(R.id.ListViewPokedex);
        btnAddPokemon = findViewById(R.id.btnAdd);
        txtSearchPokemon = findViewById(R.id.editTextAdd);
        imgBtnSettings = findViewById(R.id.imgBtnLogout);
        imgBtnMusic = findViewById(R.id.imgBtnMusic);
        txtUser = findViewById(R.id.textViewUser);

        docRef = db.collection("Users").document(currentUser.getUid());

        pokemonListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
                return true;
            }
        });

        pokemonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ReadyToBattleLocation() && ReadyToBattleTime()) {
                    if (pokemonIntroSong != null) {
                        pokemonIntroSong.stop();
                        pokemonIntroSong.release();
                        pokemonIntroSong = null;
                    }
                    
                    Intent intent = new Intent(getApplicationContext(), ChooseOpponent.class);
                    intent.putExtra("position", position);

                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "You need to walk 500 meters to battle!", Toast.LENGTH_LONG).show(); //TODO Externalize
                }
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

                if (!pokemonName.equals("")) {
                    pokemonService.AddPokemon(pokemonName);
                    txtSearchPokemon.getText().clear();
                }
                else {
                    txtSearchPokemon.setError("Please enter the name of a Pokemon"); //TODO Externalize
                }
            }
        });

        imgBtnMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pokemonIntroSong == null) {
                    pokemonIntroSong = MediaPlayer.create(getApplicationContext(), R.raw.pokemon_danish_introsong);
                    pokemonIntroSong.start();
                }
                else {
                    pokemonIntroSong.stop();
                    pokemonIntroSong.release();
                    pokemonIntroSong = null;
                }
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(R.color.colorRedPokedex);
                // set item width
                deleteItem.setWidth(250);
                // set a title
                deleteItem.setTitle("Delete");
                // set a title size
                deleteItem.setTitleSize(18);
                // set a title color
                deleteItem.setTitleColor(Color.BLACK);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        pokemonListView.setMenuCreator(creator);

        pokemonListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                pokemonService.DeletePokemonDatabase(pokemonList.get(position).getName());
                // false : close the menu; true : not close the menu
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UnBindFromMovieService();
    }

    /** ########################################################################################
     *  ######### Private methods ##############################################################
     *  ######################################################################################## */

    private boolean ReadyToBattleTime() {
        Date currentDate = Calendar.getInstance().getTime();

        if (battleTime != null) {
            long minutesPast = ((currentDate.getTime() - battleTime.getTime()) / 1000) / 60;

            if (minutesPast >= TIME_TO_PASS) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    private boolean ReadyToBattleLocation() {
        if (battleLocation != null) {
            userLocation = pokemonService.GetCurrentLocation();

            float distance = userLocation.distanceTo(battleLocation);

            if (distance < METERS_TO_WALK) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return true;
        }
    }

    /* Inspired by TheArnieExerciseFinder demo */
    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "You need to enable permission for Location to use the app", Toast.LENGTH_SHORT).show(); //TODO Externalize
                    finish();
                }
                return;
            }
        }
    }

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
