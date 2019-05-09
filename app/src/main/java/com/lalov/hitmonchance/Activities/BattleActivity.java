package com.lalov.hitmonchance.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonAdaptor;
import com.lalov.hitmonchance.PokemonService;
import com.lalov.hitmonchance.R;

import java.util.List;

public class BattleActivity extends AppCompatActivity {

    TextView txtUsersPokemonName, txtEnemiesPokemonName, txtUsersPokemonStats, txtEnemiesPokemonStats, txtWinner;
    Button btnGoToPokedex;
    ImageView imgUser, imgEnemy;

    //for Pokemonservice
    private PokemonService pokemonService;
    private ServiceConnection pokemonServiceConnection;
    private boolean bound = false;
    String LOG = "Bound";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        txtUsersPokemonName = findViewById(R.id.textViewUserName);
        txtEnemiesPokemonName = findViewById(R.id.textViewEnemyName);
        txtUsersPokemonStats = findViewById(R.id.textViewUserLevel);
        txtEnemiesPokemonStats = findViewById(R.id.textViewEnemyLevel);
        txtWinner = findViewById(R.id.textViewWinner);
        btnGoToPokedex = findViewById(R.id.buttonGoToPokedex);
        imgUser = findViewById(R.id.imageViewUserPokemon);
        imgEnemy = findViewById(R.id.imageViewEnemyPokemon);
        btnGoToPokedex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BattleActivity.this,MainActivity.class);
                startActivity(intent);
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

                PokemonAdaptor adaptor = new PokemonAdaptor(BattleActivity.this,pokemonList);
                imgUser.setImageResource(R.drawable.pokeball_black);
                imgEnemy.setImageResource(R.drawable.pokeball_black);
                //int icon = adaptor.findIconFormovie(movieService.getMovie(getIntent.getIntExtra("Position",0)));
                Pokemon usersSelectedPokemon = pokemonService.GetPokemon(getIntent.getIntExtra("Position",0));
                Pokemon enemiesSelectedPokemon = pokemonService.GetPokemon(getIntent.getIntExtra("Position",0));
                getselectedPokemon(usersSelectedPokemon,true);
                getselectedPokemon(enemiesSelectedPokemon,false);
                determinateWinner(calculateTotalStats(usersSelectedPokemon),calculateTotalStats(enemiesSelectedPokemon));
            }

            public void onServiceDisconnected(ComponentName className) {
                pokemonService = null;
                //Log.d(LOG, "Movie service disconnected from Details");
            }
        };
    }
    // Bind when onStart is called
    @Override
    protected void onStart() {
        super.onStart();
        bindToPokemonService();
        //Log.d(LOG, "Movie service has binded to Details");
    }

    // UnBind when onStop is called
    @Override
    protected void onStop() {
        super.onStop();
        unBindFromPokemonService();
        //Log.d(LOG, "Movie service has unbinded to Details");
    }

    // Method for binding. Inspired by ServiceDemo
    void bindToPokemonService() {
        bindService(new Intent(BattleActivity.this,
                PokemonService.class), pokemonServiceConnection, Context.BIND_AUTO_CREATE);
        bound = true;
    }
    // Method for binding. Inspired by ServiceDemo
    void unBindFromPokemonService() {
        if (bound) {
            // Detach our existing connection.
            unbindService(pokemonServiceConnection);
            bound = false;
        }
    }

    // Set up details with the correct movie
    void getselectedPokemon(Pokemon pokemon, boolean UserOrEnemy){
        Long totalStats = calculateTotalStats(pokemon);
        if(UserOrEnemy){
            txtUsersPokemonName.setText(pokemon.getName());
            txtUsersPokemonStats.setText(Long.toString(totalStats));
        }else{
            txtEnemiesPokemonName.setText(pokemon.getName());
            txtEnemiesPokemonStats.setText(Long.toString(totalStats));
        }
    }
    Long calculateTotalStats(Pokemon pokemon){
        long hp = pokemon.getHp();
        long attack = pokemon.getAttack();
        long defense = pokemon.getDefense();
        long spAttack = pokemon.getSpattack();
        long spDefense = pokemon.getSpdefense();
        long speed = pokemon.getSpeed();
        Long totalStat = hp+attack+defense+spAttack+spDefense+speed;
        return totalStat;
    }

    void determinateWinner(long UserStats, long EnemyStats){
        if(UserStats>EnemyStats){
            txtWinner.setTextColor(Color.GREEN);
            txtWinner.setText(getResources().getString(R.string.win));
        }else if(UserStats<EnemyStats){
            txtWinner.setTextColor(Color.RED);
            txtWinner.setText(getResources().getString(R.string.loose));
        }else{
            txtWinner.setTextColor(Color.BLUE);
            txtWinner.setText(getResources().getString(R.string.tie));
        }

    }

}
