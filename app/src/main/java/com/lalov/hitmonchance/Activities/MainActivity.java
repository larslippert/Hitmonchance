package com.lalov.hitmonchance.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lalov.hitmonchance.Pokemon;
import com.lalov.hitmonchance.PokemonAdaptor;
import com.lalov.hitmonchance.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pokemonListView = (ListView) findViewById(R.id.ListViewPokedex);
        btnAddPokemon = findViewById(R.id.btnAdd);
        txtSearchPokemon = findViewById(R.id.editTextAdd);
        imgBtnSettings = findViewById(R.id.imgBtnLogout);
        txtUser = findViewById(R.id.textViewUser);

        docRef = db.collection("Users").document(currentUser.getUid());
        SetUsername();

        Pokemon p1 = new Pokemon(1,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        Pokemon p2 = new Pokemon(2,"Shit",1,"atk","atk2",20,1,1,1,1,1,1,true);
        pokemonList = new ArrayList<>();
        pokemonList.add(p1);
        pokemonList.add(p2);
        pokemonAdaptor = new PokemonAdaptor(this, pokemonList);

        pokemonListView.setAdapter(pokemonAdaptor);
        pokemonListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                startActivity(intent);
                return true;
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
                AddPokemon();
            }
        });

    }

    private void SignOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void AddPokemon() {
        Map<String, Object> pokemon = new HashMap<>();
        pokemon.put("#", 151);
        pokemon.put("Name", txtSearchPokemon.getText().toString());

        db.collection("Users").document(currentUser.getUid()).collection("Pokemon").document()
                .set(pokemon)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error");
                    }
                });

        txtSearchPokemon.getText().clear();
    }

    private void SetUsername() {
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            txtUser.setText(documentSnapshot.getString("Username"));
                            Log.d(TAG, "SUCCESS: Got document snapshot");
                        }
                        else {
                            txtUser.setText(currentUser.getEmail());
                            Log.d(TAG, "ERROR: Could not get document snapshot");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "ERROR: Document was not found");
                    }
                });
    }

}
