package com.lalov.hitmonchance;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.lalov.hitmonchance.Globals.API_CONNECTION_TAG;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_LOCATION;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_POKEMON;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_RANDOM_POKEMON;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_USERNAME;
import static com.lalov.hitmonchance.Globals.BROADCAST_RESULT_USERS;
import static com.lalov.hitmonchance.Globals.FIRESTORE_TAG;
import static com.lalov.hitmonchance.Globals.LOCATION_TAG;
import static com.lalov.hitmonchance.Globals.POKE_API_CALL;

public class PokemonService extends Service {

    private Context mContext;
    private ArrayList<Pokemon> pokemonList;
    private Pokemon opponentPokemon;
    private String username;
    private ArrayList<String> userList;
    private ArrayList<String> uidList;
    private RequestQueue requestQueue;

    private final IBinder binder = new PokemonServiceBinder();

    private LocalBroadcastManager bmPokemon;
    private LocalBroadcastManager bmUsername;
    private LocalBroadcastManager bmUsers;
    private LocalBroadcastManager bmRandomPokemon;
    private LocalBroadcastManager bmLocation;

    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    private Location userLocation;
    private Location battleLocation;
    private Date battleTime;

    private LocationManager locationManager;

    public class PokemonServiceBinder extends Binder {
        public PokemonService getService() {
            return PokemonService.this;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            userLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        userLocation = GetLastKnownLocation();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        docRef = db.collection("Users").document(currentUser.getUid());

        bmPokemon = LocalBroadcastManager.getInstance(this);
        bmUsername = LocalBroadcastManager.getInstance(this);
        bmUsers = LocalBroadcastManager.getInstance(this);
        bmRandomPokemon = LocalBroadcastManager.getInstance(this);
        bmLocation = LocalBroadcastManager.getInstance(this);

        TrackLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //TODO Many of these should probably have null point handlers...

    public Pokemon GetPokemon(int position) {
        return pokemonList.get(position);
    }

    public void CreateUser(String username) {
        AddUserDatabase(username);
    } //TODO Maybe not needed, just go straight to database call

    public ArrayList<Pokemon> GetAllPokemon() {
        return pokemonList;
    }

    public String GetUsername() {
        return username;
    }

    public ArrayList<String> GetAllUsers() {
        return userList;
    }

    public String GetUserId(int position) {
        return uidList.get(position);
    }

    public Pokemon GetOpponentPokemon() {
        return opponentPokemon;
    }

    public Location GetCurrentLocation() {
        return userLocation;
    }

    public Location GetBattleLocation() {
        return battleLocation;
    }

    public Date GetBattleTime() {
        return battleTime;
    }

    private void TrackLocation() {
        long minTime = 5000;
        float minDistance = 5;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(minTime, minDistance, criteria, locationListener, null);
            } catch (SecurityException e) {
                Log.d(LOCATION_TAG, "ERROR: " + e);
            }
        }
    }

    private Location GetLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        else
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
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

                    Intent broadcastIntent = new Intent(BROADCAST_RESULT_POKEMON);
                    bmPokemon.sendBroadcast(broadcastIntent);

                    Log.d(API_CONNECTION_TAG, "SUCCESS: Pokemon added");
                }
                else {
                    Log.d(API_CONNECTION_TAG, "ERROR: Pokemon is null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(API_CONNECTION_TAG, "ERROR: API call failed with error: " + error);
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

        if (pokeAPI.getId() != null) {
            String type1 = pokeAPI.getTypes().get(0).getType().getName();
            String type2 = "";

            if (pokeAPI.getTypes().size() == 2) {
                type1 = pokeAPI.getTypes().get(1).getType().getName();
                type2 = pokeAPI.getTypes().get(0).getType().getName();
            }

            String name = pokeAPI.getName().substring(0,1).toUpperCase() + pokeAPI.getName().substring(1);

            Pokemon pokemon = new Pokemon(
                    pokeAPI.getId(),
                    name,
                    type1,
                    type2,
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
        _pokemon.put("Type2", pokemon.getType2());
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

    public void GetAllPokemonDatabase() { //TODO Should be called in StatsActivity along with a broadcastManager
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
                                            (String) document.getData().get("Type2"),
                                            (long) document.getData().get("Speed"),
                                            (long) document.getData().get("SpDefense"),
                                            (long) document.getData().get("SpAttack"),
                                            (long) document.getData().get("Defense"),
                                            (long) document.getData().get("Attack"),
                                            (long) document.getData().get("HP")
                                    ));
                                }

                                Intent intent = new Intent(BROADCAST_RESULT_POKEMON);
                                intent.putExtra("pokemonList", pokemonList);

                                bmPokemon.sendBroadcast(intent);

                            }
                        }
                    }
                });
    }

    public void GetRandomPokemonDatabase(String uid) {
        db.collection("Users").document(uid).collection("Pokemon").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                int numberOfPokemon = task.getResult().size();
                                Random rand = new Random();
                                int chosenPokemon = 0;

                                if (numberOfPokemon > 1) {
                                    chosenPokemon = rand.nextInt(numberOfPokemon);
                                }

                                int counter = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (counter == chosenPokemon) {
                                        opponentPokemon = new Pokemon(
                                                (long) document.getData().get("#"),
                                                (String) document.getData().get("Name"),
                                                (String) document.getData().get("Type1"),
                                                (String) document.getData().get("Type2"),
                                                (long) document.getData().get("Speed"),
                                                (long) document.getData().get("SpDefense"),
                                                (long) document.getData().get("SpAttack"),
                                                (long) document.getData().get("Defense"),
                                                (long) document.getData().get("Attack"),
                                                (long) document.getData().get("HP")
                                        );

                                        break;
                                    }

                                    counter++;
                                }

                                if (opponentPokemon != null) {
                                    Intent intent = new Intent(BROADCAST_RESULT_RANDOM_POKEMON);
                                    intent.putExtra("pokemon", opponentPokemon); //TODO Might not make sense to put this here

                                    bmRandomPokemon.sendBroadcast(intent);
                                }
                            }
                        }
                    }
                });
    }

    public void GetAllUsersDatabase() {
        userList = new ArrayList<String>();
        uidList = new ArrayList<String>();

        db.collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (!document.getData().get("uid").equals(currentUser.getUid())) {
                                        userList.add((String) document.getData().get("Username"));
                                        uidList.add((String) document.getData().get("uid"));
                                    }
                                }
                                Intent intent = new Intent(BROADCAST_RESULT_USERS);
                                intent.putExtra("userList", userList);

                                bmUsers.sendBroadcast(intent);
                            }
                        }
                    }
                });
    }

    public void GetUsernameDatabase() {
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(FIRESTORE_TAG, "SUCCESS: Got document snapshot");
                            username = documentSnapshot.getString("Username");

                            Intent intent = new Intent(BROADCAST_RESULT_USERNAME);
                            intent.putExtra("Username", username);

                            bmUsername.sendBroadcast(intent);
                        }
                        else {
                            Log.d(FIRESTORE_TAG, "ERROR: Could not get document snapshot");
                            username = currentUser.getEmail();

                            Intent intent = new Intent(BROADCAST_RESULT_USERNAME);
                            intent.putExtra("Username", username);

                            bmUsername.sendBroadcast(intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, "ERROR: Document was not found");
                        username = currentUser.getEmail();

                        Intent intent = new Intent(BROADCAST_RESULT_USERNAME);
                        intent.putExtra("Username", username);

                        bmUsername.sendBroadcast(intent);
                    }
                });
    }

    /** ########################################################################################
     *  ######### LOCATION #####################################################################
     *  ######################################################################################## */

    public void AddBattleLocationAndTime() {
        Map<String, Object> battle = new HashMap<>();
        battle.put("Latitude", String.valueOf(userLocation.getLatitude()));
        battle.put("Longitude", String.valueOf(userLocation.getLongitude()));
        battle.put("Time", Calendar.getInstance().getTime());

        db.collection("Users").document(currentUser.getUid()).collection("Battle").document("LatestBattle")
                .set(battle)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(FIRESTORE_TAG, "SUCCESS: Battle was added!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, "ERROR: Battle was not added with exception: " + e);
                    }
                });
    }

    public void GetBattleLocationAndTime() {
        battleLocation = new Location("");
        battleTime = new Date();

        docRef.collection("Battle").document("LatestBattle").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(FIRESTORE_TAG, "SUCCESS: Got document snapshot");
                            battleLocation.setLatitude(Double.parseDouble(documentSnapshot.getString("Latitude")));
                            battleLocation.setLongitude(Double.parseDouble(documentSnapshot.getString("Longitude")));
                            battleTime = documentSnapshot.getDate("Time");

                            Intent intent = new Intent(BROADCAST_RESULT_LOCATION);

                            bmLocation.sendBroadcast(intent);
                        }
                        else {
                            Log.d(FIRESTORE_TAG, "ERROR: Could not get document snapshot");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, "ERROR: Document was not found with exception: " + e);
                    }
                });
    }

}
