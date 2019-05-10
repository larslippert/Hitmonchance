package com.lalov.hitmonchance;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lalov.hitmonchance.PokeAPI.PokeAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lalov.hitmonchance.Globals.API_CONNECTION_TAG;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT;
import static com.lalov.hitmonchance.Globals.FIRESTORE_TAG;
import static com.lalov.hitmonchance.Globals.POKE_API_CALL;

public class PokemonService extends Service {

    private Context mContext;
    private ArrayList<Pokemon> pokemonList;
    private String username;
    private RequestQueue requestQueue;

    MediaPlayer mediaPlayer;

    private final IBinder binder = new PokemonServiceBinder();
    private LocalBroadcastManager bm;

    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    public class PokemonServiceBinder extends Binder {
        public PokemonService getService() {
            return PokemonService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.Pokemon_danish_introsong);
        mediaPlayer.start();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        docRef = db.collection("Users").document(currentUser.getUid());

        bm = LocalBroadcastManager.getInstance(this);

        GetAllPokemonDatabase();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public Context getContext(){
        return mContext;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public Pokemon GetPokemon(int position) {return pokemonList.get(position); }

    public void CreateUser(String username) {
        AddUserDatabase(username);
    }


    public ArrayList<Pokemon> GetAllPokemon() {
        return pokemonList;
    }


    /*
    public String GetUsername() {
        GetUsernameDatabase();
        return username;
    }
    */

    /** ########################################################################################
     *  ######### API CALL #####################################################################
     *  ######################################################################################## */


    public void AddPokemon(String name) {
        String url = POKE_API_CALL + name;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(API_CONNECTION_TAG, "SUCCESS: API call succeeded");
                Pokemon pokemon = InterpretPokemonJSON(response);
                if (pokemon != null) {
                    AddPokemonDatabase(pokemon);
                    pokemonList.add(pokemon);

                    Intent broadcastIntent = new Intent(BROADCAST_RESULT);
                    bm.sendBroadcast(broadcastIntent);

                    Log.d(API_CONNECTION_TAG, "SUCCESS: Pokemon added");
                }
                else {
                    Log.d(API_CONNECTION_TAG, "ERROR: Pokemon is null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(API_CONNECTION_TAG, "ERROR: API call failed");
            }
        });

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }

        requestQueue.add(stringRequest);
    }


    private Pokemon InterpretPokemonJSON(String jsonResponse) {
        Gson gson = new GsonBuilder().create();
        PokeAPI pokeAPI = gson.fromJson(jsonResponse, PokeAPI.class);

        if (pokeAPI != null) {
            Pokemon pokemon = new Pokemon(
                    pokeAPI.getId(),
                    pokeAPI.getName(),
                    pokeAPI.getTypes().get(0).getType().getName(),
                    pokeAPI.getStats().get(0).getBaseStat(),
                    pokeAPI.getStats().get(1).getBaseStat(),
                    pokeAPI.getStats().get(2).getBaseStat(),
                    pokeAPI.getStats().get(3).getBaseStat(),
                    pokeAPI.getStats().get(4).getBaseStat(),
                    pokeAPI.getStats().get(5).getBaseStat()
            );

            return pokemon;
        }
        else {
            return null;
        }
    }

    /** ########################################################################################
     *  ######### DATABASE #####################################################################
     *  ######################################################################################## */

    private void AddUserDatabase(String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("Username", username);
        user.put("uid", currentUser.getUid());

        Log.d(FIRESTORE_TAG, currentUser.getUid());

        db.collection("Users").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(FIRESTORE_TAG, "User was added! Success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, "User was not added! Error!");
                    }
                });

    }

    private void AddPokemonDatabase(Pokemon pokemon) {
        Map<String, Object> _pokemon = new HashMap<>();
        _pokemon.put("#", pokemon.getId());
        _pokemon.put("Name", pokemon.getName());
        _pokemon.put("Type1", pokemon.getType1());
        _pokemon.put("Speed", pokemon.getSpeed());
        _pokemon.put("SpDefense", pokemon.getSpdefense());
        _pokemon.put("SpAttack", pokemon.getSpattack());
        _pokemon.put("Defense", pokemon.getDefense());
        _pokemon.put("Attack", pokemon.getAttack());
        _pokemon.put("HP", pokemon.getHp());

        db.collection("Users").document(currentUser.getUid()).collection("Pokemon").document()
                .set(_pokemon)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(FIRESTORE_TAG, "Pokemon was added! Success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, "Pokemon was not added! Error!");
                    }
                });
    }

    public void GetAllPokemonDatabase() {
        pokemonList = new ArrayList<Pokemon>();

        docRef.collection("Pokemon").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    pokemonList.add(new Pokemon(
                                            (long) document.getData().get("#"),
                                            (String) document.getData().get("Name"),
                                            (String) document.getData().get("Type1"),
                                            (long) document.getData().get("Speed"),
                                            (long) document.getData().get("SpDefense"),
                                            (long) document.getData().get("SpAttack"),
                                            (long) document.getData().get("Defense"),
                                            (long) document.getData().get("Attack"),
                                            (long) document.getData().get("HP")
                                    ));
                                    Intent intent = new Intent(BROADCAST_RESULT);
                                    intent.putExtra("pokemonList", pokemonList);

                                    bm.sendBroadcast(intent);
                                }
                            }
                        }
                    }
                });
    }

    public String GetUsernameDatabase() {
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(FIRESTORE_TAG, "SUCCESS: Got document snapshot");
                            username = documentSnapshot.getString("Username");
                        }
                        else {
                            Log.d(FIRESTORE_TAG, "ERROR: Could not get document snapshot");
                            username = currentUser.getEmail();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, "ERROR: Document was not found");
                        username = currentUser.getEmail();
                    }
                });

        return username;
    }
}
