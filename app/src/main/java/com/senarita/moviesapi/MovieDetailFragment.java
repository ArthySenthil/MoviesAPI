package com.senarita.moviesapi;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.senarita.moviesapi.movie.MovieContent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    public static final String ARG_MOVIE_DATA = "movie_data";

    MovieContent.MovieItem movieItem;

    @Bind(R.id.movie_image)
    ImageView mImageView;

    @Bind(R.id.movie_detail)
    TextView movie_detail;

    @Bind(R.id.user_rating)
    TextView user_rating;

    @Bind(R.id.release_date)
    TextView release_date;


    @Bind(R.id.star)
    CheckBox favoriteStar;

    public MovieDetailFragment() {
    }

    List<MovieContent.MovieItem> mMovieItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments().containsKey(ARG_MOVIE_DATA)) {

            movieItem = getArguments().getParcelable(ARG_MOVIE_DATA);

        }

    }


    String favMovieId = null;

    public void saveMovie() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("MovieItem");
        query.whereEqualTo("author", ParseUser.getCurrentUser());


        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    for (final ParseObject movie : objects) {

                        if (movieItem.getId().equals(movie.getString("movie_id"))) {

                            favMovieId = movie.getObjectId();

                            break;
                        }

                    }

                    if (favMovieId == null) {
                        addNewMovie();
                    }

                }
            }
        });

    }


    public void deleteMovie() {


        ParseQuery<ParseObject> query = ParseQuery.getQuery("MovieItem");
        query.whereEqualTo("author", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (final ParseObject movie : objects) {

                        if (movieItem.getId().equals(movie.getString("movie_id"))) {
                            final MovieContent.MovieItem delMovieItem = loadItems(movie);


                            movie.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (delMovieItem.getId().equals(movie.getString("movie_id"))) {

                                        mMovieItems.remove(movieItem);
                                    }

                                    Toast.makeText(getContext(), "The movie is not in your favorites anymore!", Toast.LENGTH_SHORT).show();

                                }
                            });


                            break;
                        }

                    }


                }
            }
        });


    }


    public void findMovieInBackground() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("MovieItem");
        query.whereEqualTo("author", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    mMovieItems.clear();

                    for (final ParseObject movie : objects) {
                        MovieContent.MovieItem favMovieItem = loadItems(movie);
                        mMovieItems.add(favMovieItem);


                        if (movieItem.getId().equals(movie.getString("movie_id"))) {

                            favoriteStar.setText(R.string.delete_favorites);
                            favoriteStar.setChecked(true);

                            break;
                        } else {

                            favoriteStar.setText(R.string.make_favorite);
                            favoriteStar.setChecked(false);


                        }


                    }

                }
            }
        });


    }

    public MovieContent.MovieItem loadItems(ParseObject movieObject) {
        MovieContent.MovieItem movieItems;

        String movieID = movieObject.getString("movie_id");
        String original_title = movieObject.getString("original_title");
        String overview = movieObject.getString("overview");
        String poster_path = movieObject.getString("poster_path");
        String popularity = movieObject.getString("popularity");
        String title = movieObject.getString("title");
        String vote_average = movieObject.getString("vote_average");
        String release_date = movieObject.getString("release_date");

        movieItems = new MovieContent.MovieItem(movieID, original_title, overview,
                poster_path, popularity, title, vote_average, release_date);

        return movieItems;
    }


    public void addNewMovie() {
        final ParseObject movieObject = new ParseObject("MovieItem");
        movieObject.put("movie_id", movieItem.getId());
        movieObject.put("original_title", movieItem.getOriginal_title());
        movieObject.put("overview", movieItem.getOverview());
        movieObject.put("poster_path", movieItem.getPoster_path());
        movieObject.put("popularity", movieItem.getPopularity());
        movieObject.put("title", movieItem.getTitle());
        movieObject.put("vote_average", movieItem.getVote_average());
        movieObject.put("release_date", movieItem.getRelease_date());
        movieObject.put("author", ParseUser.getCurrentUser());
        final MovieContent.MovieItem addMovieItem = loadItems(movieObject);

        movieObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //Saved successfully.
                if (e == null) {

                    if(addMovieItem.getId().equals(movieObject.getString("movie_id"))) {

                        mMovieItems.add(addMovieItem);
                    }

                    Toast.makeText(getContext(), "Movie details has been successfully saved to your favorites:)", Toast.LENGTH_SHORT).show();


                } else {
                    //The save failed.
                    Log.d(getClass().getSimpleName(), "User update error:" + e);
                }
            }
        });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        // Show the movie content in ImageView and TextViews.
        if (movieItem != null) {
            findMovieInBackground();
            Picasso.with(getContext())
                    .load("http://image.tmdb.org/t/p/w185/" + movieItem.getPoster_path())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(mImageView);
            Resources res = getResources();
            release_date.setText(res.getString(R.string.released_on, movieItem.getRelease_date()));
            user_rating.setText(res.getString(R.string.ratings, movieItem.getVote_average()));

            movie_detail.setText(movieItem.getOverview());

            favoriteStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //If user is not signed in prompt user to login.
                    if (ParseUser.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "Please login to add your favorites", Toast.LENGTH_SHORT).show();
                        favoriteStar.setChecked(false);
                        favoriteStar.setText(R.string.make_favorite);
                        loadLoginView();

                    } else {
                        //If user is signed in add movie to the users favorites

                        if (favoriteStar.isChecked()) {
                            saveMovie();
                            favoriteStar.setText(R.string.delete_favorites);
                            favoriteStar.setChecked(true);

                        } else {

                            //Delete movie from favorites.
                            deleteMovie();
                            favoriteStar.setText(R.string.make_favorite);
                            favoriteStar.setChecked(false);

                        }


                    }

                }

            });


        }


        return rootView;
    }

    private void loadLoginView() {
        Intent intent = new Intent(getContext(), UserLoginActivity.class);
        startActivityForResult(intent, Activity.RESULT_OK);
    }




}
