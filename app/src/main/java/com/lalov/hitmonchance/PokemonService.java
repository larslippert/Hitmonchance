package com.lalov.hitmonchance;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class PokemonService extends Service {

    private static Context mContext;
    private List<Pokemon> pokemonList;

    public class PokemonServiceBinder extends Binder {
        //return ref to service (or at least an interface) that activity can call public methods on
        PokemonService getService() {
            return PokemonService.this;
        }
    }
    private final IBinder binder = new PokemonServiceBinder();
    public PokemonService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

    }
    public static Context getContext(){
        return mContext;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
