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
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lalov.hitmonchance.DownloadImageTask;
import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;

import java.util.List;
import java.util.Random;

import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_RANDOM_POKEMON;
import static com.lalov.hitmonchance.Globals.PERMISSIONS_REQUEST_LOCATION;

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

    boolean battle = false;
    boolean opponentChosen = true;

    private BroadcastReceiver broadcastReceiverPokemon = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             if(opponentChosen){
                 enemiesSelectedPokemon = pokemonService.GetOpponentPokemon();
             }
             opponentChosen = false;
             InitPokemon();
        }
    };
    MediaPlayer pokemonBattleSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        setupConnectionToPokemonService();
        bindToPokemonService();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverPokemon,
                new IntentFilter(BROADCAST_RESULT_RANDOM_POKEMON));

        checkPermissions();

        txtUsersPokemonName = findViewById(R.id.textViewUserName);
        txtEnemiesPokemonName = findViewById(R.id.textViewEnemyName);
        txtUsersPokemonStats = findViewById(R.id.textViewUserLevel);
        txtEnemiesPokemonStats = findViewById(R.id.textViewEnemyLevel);
        txtWinner = findViewById(R.id.textViewWinner);
        btnGoToPokedex = findViewById(R.id.buttonGoToPokedex);
        btnBattle = findViewById(R.id.btnBattle);
        imgUser = findViewById(R.id.imageViewUserPokemon);
        imgEnemy = findViewById(R.id.imageViewEnemyPokemon);
        pokemonBattleSong = MediaPlayer.create(getApplicationContext(), R.raw.battle_music);
        pokemonBattleSong.start();

        if(savedInstanceState != null){
            txtWinner.setText((savedInstanceState.getString("Winner")));
            battle = savedInstanceState.getBoolean("Battle");
            enemiesSelectedPokemon = (Pokemon) savedInstanceState.getSerializable("Pokemon");
            opponentChosen = savedInstanceState.getBoolean("Opponent");
            if(battle){
                btnBattle.setBackgroundColor(Color.GRAY);
                btnBattle.setText(getResources().getString(R.string.walk));
                btnBattle.setEnabled(false);

                if(txtWinner.getText().equals(getResources().getString(R.string.win))){
                    txtWinner.setTextColor(Color.GREEN);
                }
                else{
                    txtWinner.setTextColor(Color.RED);
                }
            }

        }

        btnGoToPokedex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnBattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pokemonBattleSong.stop();
                pokemonBattleSong.release();
                determinateWinner(calculateTotalStats(usersSelectedPokemon), calculateTotalStats(enemiesSelectedPokemon));

                pokemonService.AddBattleLocationAndTime();

                btnBattle.setBackgroundColor(Color.GRAY);
                btnBattle.setText(getResources().getString(R.string.walk));
                btnBattle.setEnabled(false);
                battle = true;
            }
        });
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
                    Toast.makeText(this, getResources().getString(R.string.location), Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    // Method for setting up connection to service
    // Inspired from ServiceDemo
    private void setupConnectionToPokemonService(){
        pokemonServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                pokemonService = ((PokemonService.PokemonServiceBinder)service).getService();

                final Intent getIntent = getIntent();

                usersSelectedPokemon = pokemonService.GetPokemon(getIntent.getIntExtra("positionPokemon",0));
                pokemonService.GetRandomPokemonDatabase(pokemonService.GetUserId(getIntent.getIntExtra("positionOpponent", 0)));
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

    private void InitPokemon() {
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewUserPokemon)).execute(usersSelectedPokemon.getImage());
        new DownloadImageTask((ImageView) findViewById(R.id.imageViewEnemyPokemon)).execute(enemiesSelectedPokemon.getImage());

        txtEnemiesPokemonName.setText(enemiesSelectedPokemon.getName());
        txtEnemiesPokemonStats.setText(getApplicationContext().getResources().getString(R.string.totalstats)+" "+Long.toString(calculateTotalStats(enemiesSelectedPokemon)));

        txtUsersPokemonName.setText(usersSelectedPokemon.getName());
        txtUsersPokemonStats.setText(getApplicationContext().getResources().getString(R.string.totalstats)+" "+Long.toString(calculateTotalStats(usersSelectedPokemon)));
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
            pokemonBattleSong = MediaPlayer.create(this,R.raw.winner_music);
            pokemonBattleSong.start();
        }else {
            txtWinner.setTextColor(Color.RED);
            txtWinner.setText(getResources().getString(R.string.lose));
            pokemonBattleSong = MediaPlayer.create(this,R.raw.loser_music);
            pokemonBattleSong.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pokemonBattleSong.stop();
        pokemonBattleSong.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Winner", String.valueOf(txtWinner.getText()));
        outState.putBoolean("Battle",battle);
        outState.putSerializable("Pokemon",enemiesSelectedPokemon);
        outState.putBoolean("Opponent",opponentChosen);
    }
}
