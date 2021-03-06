package com.lalov.hitmonchance.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lalov.hitmonchance.DownloadImageTask;
import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;

import static com.lalov.hitmonchance.Globals.SERVICE_TAG;


public class StatsActivity extends AppCompatActivity {
    TextView txtName, txtDexNo, txtHPStat, txtAttackStat, txtDefenseStat
            , txtSpAtkStat, txtSpDefStat, txtSpeedStat, txtTotalStats;
    ImageView imgPokemon;

    private ServiceConnection pokemonServiceConnection;
    private PokemonService pokemonService;
    private boolean bound = false;
    private Pokemon usersSelectedPokemon;

    private String LOG = "StatsActivity: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Log.d(LOG,"created");

        SetupConnectionToPokemonService();
        BindToPokemonService();

        txtName = findViewById(R.id.txtName);
        txtDexNo = findViewById(R.id.txtDexNo);
        txtHPStat = findViewById(R.id.txtHPStat);
        txtAttackStat = findViewById(R.id.txtAttackStat);
        txtDefenseStat = findViewById(R.id.txtDefenseStat);
        txtSpAtkStat = findViewById(R.id.txtSpAtkStat);
        txtSpDefStat = findViewById(R.id.txtSpDefStat);
        txtSpeedStat = findViewById(R.id.txtSpeedStat);
        imgPokemon = findViewById(R.id.imageView);
        txtTotalStats = findViewById(R.id.txtTotalStats);

    }

    private void BindToPokemonService() {
        if (!bound) {
            bindService(new Intent(StatsActivity.this, PokemonService.class),
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

    private void setStats(Pokemon selectedPokemon){
        new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute(selectedPokemon.getImage());
        txtName.setText(getString(R.string.name) + " " + selectedPokemon.getName());
        txtDexNo.setText(getString(R.string.txtDexNo) + " " + selectedPokemon.getId());
        txtHPStat.setText(Long.toString(selectedPokemon.getHp()));
        txtAttackStat.setText(Long.toString(selectedPokemon.getAttack()));
        txtDefenseStat.setText(Long.toString(selectedPokemon.getDefense()));
        txtSpAtkStat.setText(Long.toString(selectedPokemon.getSpattack()));
        txtSpDefStat.setText(Long.toString(selectedPokemon.getSpdefense()));
        txtSpeedStat.setText(Long.toString(selectedPokemon.getSpeed()));
        txtTotalStats.setText(Long.toString(calculateTotalStats(selectedPokemon)));

    }

    private void SetupConnectionToPokemonService() {
        pokemonServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                pokemonService = ((PokemonService.PokemonServiceBinder)service).getService();
                Log.d(SERVICE_TAG, "StatsActivity connected to pokemon service");

                final Intent intent = getIntent(); 

                usersSelectedPokemon = pokemonService.GetPokemon(intent.getIntExtra("position",0));
                setStats(usersSelectedPokemon);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                pokemonService = null;
                Log.d(SERVICE_TAG, "StatsActivity disconnected from pokemon service");
            }
        };
    }

    protected void onDestroy() {
        super.onDestroy();

        UnBindFromPokemonService();
    }

    // Method is the same as in battle-activity
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
}
