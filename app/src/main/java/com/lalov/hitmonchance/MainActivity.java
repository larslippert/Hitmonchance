package com.lalov.hitmonchance;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PokemonAdaptor pokemonAdaptor;
    private ListView pokemonListView;
    private ArrayList<Pokemon> pokemonsList;
    Button btnAddPokemon;
    ImageButton imgBtnSettings;
    EditText txtSearchPokemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pokemonListView = (ListView) findViewById(R.id.ListViewPokedex);
        btnAddPokemon = findViewById(R.id.btnAdd);
        txtSearchPokemon = findViewById(R.id.editTextAdd);
        imgBtnSettings = findViewById(R.id.imgBtnLogout);

        Pokemon p1 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p2 = new Pokemon(2,"",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p3 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p4 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p5 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p6 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p7 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        pokemonsList = new ArrayList<>();
        pokemonsList.add(p1);
        pokemonsList.add(p2);
        pokemonsList.add(p3);
        pokemonsList.add(p4);
        pokemonsList.add(p5);
        pokemonsList.add(p6);
        pokemonsList.add(p7);
        pokemonAdaptor = new PokemonAdaptor(this, pokemonsList);

        pokemonListView.setAdapter(pokemonAdaptor);

        imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOut();
            }
        });

    }

    private void SignOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }


}
