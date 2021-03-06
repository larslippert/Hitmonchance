package com.lalov.hitmonchance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PokemonAdaptor extends BaseAdapter{

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
            new DownloadImageTask((ImageView) convertView.findViewById(R.id.imageViewPokemon)).execute(pokemon.getImage());

            TextView adaptorName = (TextView) convertView.findViewById(R.id.txtName);
            adaptorName.setText(context.getResources().getString(R.string.name)+ " " +pokemon.getName());

            TextView adaptorType = (TextView) convertView.findViewById(R.id.txtDexNo);
            if(!pokemon.getType2().equals("")){
                adaptorType.setText(context.getResources().getString(R.string.type)+ " "+pokemon.getType1()+", "+pokemon.getType2());
            }else{
                adaptorType.setText(context.getResources().getString(R.string.type)+ " "+pokemon.getType1());
            }

        }
        return convertView;
    }
}
