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
            /*
            TextView customTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
            customTitle.setText(movie.getTitle());

            ImageView customImage = (ImageView) convertView.findViewById(R.id.imageView);
            int movieIcon = findIconFormovie(movie);
            customImage.setImageResource(movieIcon);

            TextView customRating = (TextView) convertView.findViewById(R.id.textViewIMDB);
            customRating.setText(context.getResources().getString(R.string.overviewimdb)+movie.getRating());

            TextView customUserrating = (TextView) convertView.findViewById(R.id.textViewRating);
            customUserrating.setText(movie.getUserRating());

            TextView customWatched = (TextView) convertView.findViewById(R.id.textViewWatched);
            customWatched.setText(movie.getWatched());
            */
        }
        return convertView;
    }
    /*
    // Method for finding the genre of the movie
    private String findGenre(String genres){
        String genre = genres.substring(0,genres.indexOf(','));
        return genre;
    }
    // Method for finding the right icon for the movie based og genre.
    // Icons found on https://emojiisland.com/pages/free-download-emoji-icons-png
    public int findIconFormovie(Movie movie){
        String genre = findGenre(movie.getGenres());
        int genreIcon;
        switch (genre){
            case "Drama":
                genreIcon= R.drawable.drama;
                break;
            case "Animation":
                genreIcon = R.drawable.animation;
                break;
            case "Action":
                genreIcon = R.drawable.action;
                break;
            case "Biography":
                genreIcon = R.drawable.biography;
                break;
            case "Horror":
                genreIcon = R.drawable.horror;
                break;
            default:
                 genreIcon = R.drawable.unknown;
        }
        return genreIcon;
    }
    */
    public void UpdateMovis(List<Pokemon> movieList){
           pokemons.clear();
           pokemons.addAll(movieList);
    }
}
