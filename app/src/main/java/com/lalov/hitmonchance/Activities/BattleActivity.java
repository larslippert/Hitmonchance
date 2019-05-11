package com.lalov.hitmonchance.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;

import java.util.List;
import java.util.Random;

import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_RANDOM_POKEMON;

public class BattleActivity extends AppCompatActivity {

    TextView txtUsersPokemonName, txtEnemiesPokemonName, txtUsersPokemonStats, txtEnemiesPokemonStats, txtWinner;
    Button btnGoToPokedex, btnBattle;
    ImageView imgUser, imgEnemy;

    //for Pokemonservice
    private PokemonService pokemonService;
    private ServiceConnection pokemonServiceConnection;
    private boolean bound = false;

    private Pokemon usersSelectedPokemon;
    private Pokemon enemiesSelectedPokemon;

    private BroadcastReceiver broadcastReceiverPokemon = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            enemiesSelectedPokemon = pokemonService.GetOpponentPokemon();
            InitPokemon();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        setupConnectionToPokemonService();
        bindToPokemonService();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverPokemon,
                new IntentFilter(BROADCAST_RESULT_RANDOM_POKEMON));

        txtUsersPokemonName = findViewById(R.id.textViewUserName);
        txtEnemiesPokemonName = findViewById(R.id.textViewEnemyName);
        txtUsersPokemonStats = findViewById(R.id.textViewUserLevel);
        txtEnemiesPokemonStats = findViewById(R.id.textViewEnemyLevel);
        txtWinner = findViewById(R.id.textViewWinner);
        btnGoToPokedex = findViewById(R.id.buttonGoToPokedex);
        btnBattle = findViewById(R.id.btnBattle);
        imgUser = findViewById(R.id.imageViewUserPokemon);
        imgEnemy = findViewById(R.id.imageViewEnemyPokemon);

        btnGoToPokedex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                determinateWinner(calculateTotalStats(usersSelectedPokemon), calculateTotalStats(enemiesSelectedPokemon));
                btnBattle.setBackgroundColor(Color.GRAY);
                btnBattle.setText("Walk 500 meters to battle again"); //TODO Externalize
                btnBattle.setEnabled(false);
            }
        });
    }

    // Method for setting om connection to service
    // Inspired from ServiceDemo
    private void setupConnectionToPokemonService(){
        pokemonServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                pokemonService = ((PokemonService.PokemonServiceBinder)service).getService();
                //Log.d(LOG, "Movie service connected to Details");

                final Intent getIntent = getIntent();
                List<Pokemon> pokemonList = pokemonService.GetAllPokemon();

                usersSelectedPokemon = pokemonService.GetPokemon(getIntent.getIntExtra("positionPokemon",0));
                pokemonService.GetRandomPokemonDatabase(pokemonService.GetUserId(getIntent.getIntExtra("positionOpponent", 0)));

                /*
                getSelectedPokemon(usersSelectedPokemon,true);
                getSelectedPokemon(enemiesSelectedPokemon,false);
                determinateWinner(calculateTotalStats(usersSelectedPokemon),calculateTotalStats(enemiesSelectedPokemon));
                */
            }

            public void onServiceDisconnected(ComponentName className) {
                pokemonService = null;
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unBindFromPokemonService();
    }

    // Method for binding. Inspired by ServiceDemo
    private void bindToPokemonService() {
        bindService(new Intent(BattleActivity.this, PokemonService.class),
                pokemonServiceConnection, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    // Method for binding. Inspired by ServiceDemo
    private void unBindFromPokemonService() {
        if (bound) {
            // Detach our existing connection.
            unbindService(pokemonServiceConnection);
            bound = false;
        }
    }

    // Set up details with the correct pokemon
    private void getSelectedPokemon(Pokemon pokemon, boolean UserOrEnemy){
        Long totalStats = calculateTotalStats(pokemon);
        if(UserOrEnemy){
            txtUsersPokemonName.setText(pokemon.getName());
            txtUsersPokemonStats.setText(Long.toString(totalStats));
        }else{
            txtEnemiesPokemonName.setText(pokemon.getName());
            txtEnemiesPokemonStats.setText(Long.toString(totalStats));
        }
    }

    private void InitPokemon() {
        imgUser.setImageResource(R.drawable.pokeball_black);
        imgEnemy.setImageResource(R.drawable.pokeball_black);

        txtEnemiesPokemonName.setText(enemiesSelectedPokemon.getName());
        txtEnemiesPokemonStats.setText(Long.toString(calculateTotalStats(enemiesSelectedPokemon)));

        txtUsersPokemonName.setText(usersSelectedPokemon.getName());
        txtUsersPokemonStats.setText(Long.toString(calculateTotalStats(usersSelectedPokemon)));
    }

    private Long calculateTotalStats(Pokemon pokemon){
        long hp = pokemon.getHp();
        long attack = pokemon.getAttack();
        long defense = pokemon.getDefense();
        long spAttack = pokemon.getSpattack();
        long spDefense = pokemon.getSpdefense();
        long speed = pokemon.getSpeed();

        Long totalStat = hp+attack+defense+spAttack+spDefense+speed;
        return totalStat;
    }

    private void determinateWinner(long userStats, long enemyStats){
        Random rand = new Random();
        int totalStats = (int) (userStats + enemyStats);

        int bonus = 0;
        if (userStats > enemyStats)
            bonus = -70;
        else if (enemyStats > userStats)
            bonus = 70;

        int result = rand.nextInt(totalStats) + bonus;

        if(result < userStats){
            txtWinner.setTextColor(Color.GREEN);
            txtWinner.setText(getResources().getString(R.string.win));
        }else {
            txtWinner.setTextColor(Color.RED);
            txtWinner.setText(getResources().getString(R.string.lose));
        }
    }
}
