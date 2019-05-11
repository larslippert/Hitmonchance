package com.lalov.hitmonchance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PokemonAdaptor extends BaseAdapter {

    // Inspiration from demo with listview
    Context context;
    List<Pokemon> pokemons;
    Pokemon pokemon;

    public PokemonAdaptor(Context c, List<Pokemon> pokemonlist ){
        this.context = c;
        this.pokemons = pokemonlist;
    }

    @Override
    public int getCount() {

        if(pokemons !=null) {
            return pokemons.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if(pokemons !=null) {
            return pokemons.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater pokemonInflator = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = pokemonInflator.inflate(R.layout.pokemonelements_listview, null);
        }

        pokemon = pokemons.get(position);
        if(pokemon !=null){
            ImageView adaptorImage = (ImageView) convertView.findViewById(R.id.imageViewPokemon);
            adaptorImage.setImageResource(R.drawable.pokeball_black);

            ImageView adaptorStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);
            adaptorStatus.setImageResource(R.drawable.pokeball_black);

            TextView adaptorName = (TextView) convertView.findViewById(R.id.txtName);
            adaptorName.setText(context.getResources().getString(R.string.name)+ " " +pokemon.getName());
        }
        return convertView;
    }

    public void UpdatePokemons(List<Pokemon> pokemonList){
           pokemons.clear();
           pokemons.addAll(pokemonList);
    }
}
