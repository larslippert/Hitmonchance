package com.lalov.hitmonchance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PokemonAdaptor pokemonAdaptor;
    private ListView pokemonListView;
    private ArrayList<Pokemon> pokemonsList;
    Button btnAddPokemon;
    EditText txtSearchPokemon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_main);

        btnAddPokemon = findViewById(R.id.btnAdd);
        txtSearchPokemon = findViewById(R.id.editTextAdd);

        pokemonListView = (ListView) findViewById(R.id.ListViewPokedex);
        pokemonsList = new ArrayList<Pokemon>();
        initializePokemons();
        //pokemonAdaptor = new PokemonAdaptor(this, pokemonsList);
        //pokemonListView.setAdapter(pokemonAdaptor);
    }
    private void initializePokemons() {
        //pokemonsList = new ArrayList<Pokemon>();
        Pokemon p1 = new Pokemon(1,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        pokemonsList.add(p1);
        pokemonAdaptor = new PokemonAdaptor(this, pokemonsList);
        pokemonListView.setAdapter(pokemonAdaptor);
    }
}
