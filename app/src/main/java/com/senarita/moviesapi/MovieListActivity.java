package com.senarita.moviesapi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.senarita.moviesapi.movie.MovieContent;
import com.senarita.moviesapi.movie.MovieContent.MovieItem;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static final String SORT_BY_POPULARITY = "sort_by_popularity";
    public static final String SORT_BY_VOTE_AVERAGE = "sort_by_vote_average";
    public static final String VOTE_AVERAGE = "vote_average";
    public static final String SORT_BY = "sortBy";
    public static final String DEFAULT_SORT = "popularity";
    public static final String SORT_BY_FAVORITES = "sortByFavorites";
    public static final String LOG_TAG = MovieListActivity.class.getSimpleName();

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public boolean mTwoPane;

    /**
     * To determine which sorting preference the activity should choose.
     */

    private boolean mPopularity = true;
    private boolean mVoteAverage = false;
    private boolean mFavorites = false;

    /**
     * The actual sorting preference that is chosen.
     */
    private String sortByValue;

    String query;
    RecyclerView.LayoutManager mLayoutManager;
    private OkHttpClient mOkHttpClient;
    List<MovieItem> movieItemList = new ArrayList<>();

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.movie_list)
    RecyclerView recyclerView;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            toolbar.setSubtitle("Welcome, Guest");

        } else {
            String name = currentUser.getUsername();
            name = capitalize(name);
            toolbar.setSubtitle("Welcome, " + name);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.networkInterceptors().add(new StethoInterceptor());

        mLayoutManager = new GridLayoutManager(MovieListActivity.this, 2);
        recyclerView.setLayoutManager(mLayoutManager);

        assert recyclerView != null;
        setupRecyclerView(recyclerView);


        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        String prefsList;

        prefsList = prefsData();

        //To check if the Popular,Best rated or Favorites Movie List has to be loaded.

        if (prefsList.equals(SORT_BY_FAVORITES)) {
            loadFavorites();

        } else {
            sortByValue = prefsList;
            loadMovieList();

        }


    }

    public void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this,movieItemList));
    }

    public void loadMovieList() {
        try {
            run(sortByValue);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing data " + e.toString());
        }

    }


    public String prefsData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean favValue = sharedPreferences.getBoolean(SORT_BY_FAVORITES, false);
        Boolean voteAverage = sharedPreferences.getBoolean(SORT_BY_VOTE_AVERAGE, false);
        Boolean popularity = sharedPreferences.getBoolean(SORT_BY_POPULARITY, false);


        if (popularity) {
            query = sharedPreferences.getString(SORT_BY, DEFAULT_SORT);

        } else if (voteAverage) {
            query = sharedPreferences.getString(SORT_BY, VOTE_AVERAGE);
        } else if (favValue) {
            if (ParseUser.getCurrentUser() != null) {
                query = SORT_BY_FAVORITES;
            } else {
                query = sharedPreferences.getString(SORT_BY, DEFAULT_SORT);
            }

        } else {
            query = sharedPreferences.getString(SORT_BY, DEFAULT_SORT);
        }

        Log.d(LOG_TAG, SORT_BY + ": " + query);


        return query;
    }

    public static String capitalize(String s) {
        if (s == null) return null;
        if (s.length() == 1) {
            return s.toUpperCase();
        }
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return "";
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit().putString(SORT_BY, sortByValue).apply();
        sharedPreferences.edit().putBoolean(SORT_BY_VOTE_AVERAGE, mVoteAverage).apply();
        sharedPreferences.edit().putBoolean(SORT_BY_POPULARITY, mPopularity).apply();
        sharedPreferences.edit().putBoolean(SORT_BY_FAVORITES, mFavorites).apply();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);

        //Change the title of the menu as Login or Logout.
        setLoginLogoutMenuTitle();

        return true;
    }




    private void setOptionTitle(int id, String title) {
        MenuItem item = menu.findItem(id);
        item.setTitle(title);
    }

    private void setLoginLogoutMenuTitle() {
        if (ParseUser.getCurrentUser() == null) {
            setOptionTitle(R.id.action_logout, getString(R.string.login_button_label));
        } else {
            setOptionTitle(R.id.action_logout, getString(R.string.logout_label));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_logout) {

            ParseUser.logOut();
           // loadLoginView();
            if(item.getTitle()==getString(R.string.login_button_label)){
                loadLoginView();

            }else {
                Toast.makeText(this,R.string.signed_out,Toast.LENGTH_SHORT).show();
                loadMovieList();
                toolbar.setSubtitle("Welcome, Guest");
                setOptionTitle(R.id.action_logout, getString(R.string.login_button_label));
            }

            return true;
        }
        if(id==R.id.action_refresh){
           if(mFavorites){
               if(ParseUser.getCurrentUser()!=null){
                   loadFavorites();
               }

           }else {
               loadMovieList();
           }

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.popularity) {
            mPopularity = true;
            mVoteAverage = false;
            mFavorites = false;
            sortByValue = DEFAULT_SORT;

            loadMovieList();


        } else if (id == R.id.user_ratings) {
            mPopularity = false;
            mVoteAverage = true;
            mFavorites = false;
            sortByValue = VOTE_AVERAGE;

            loadMovieList();


        } else if (id == R.id.favorites) {

            mFavorites = true;
            mPopularity = false;
            mVoteAverage = false;

            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                loadLoginView();
            } else {
                loadFavorites();
            }


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    private void loadLoginView() {
        Intent intent = new Intent(this, UserLoginActivity.class);
        startActivity(intent);
    }


    public void run(String sortBy) throws Exception {

        String apiKey = BuildConfig.MOVIE_API_KEY;
        Request request = new Request.Builder()
                .url("http://api.themoviedb.org/3/discover/movie?sort_by=" + sortBy + ".desc&" + "api_key=" + apiKey)
                .header("Content-Type", "application/json")
                .build();


        setProgressBarIndeterminateVisibility(true);
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                final String jsonString = response.body().string();

                movieItemList = MovieContent.getMovieContents(jsonString);

                MovieListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (movieItemList.size() > 0) {
                            updateDisplay(movieItemList);
                            setProgressBarIndeterminateVisibility(false);

                        }

                    }
                });
            }
        });
    }

    public void updateDisplay(List<MovieItem> movieItems) {
        assert recyclerView != null;
        SimpleItemRecyclerViewAdapter simpleItemRecyclerViewAdapter
                = new SimpleItemRecyclerViewAdapter(MovieListActivity.this, movieItems);
        recyclerView.setAdapter(simpleItemRecyclerViewAdapter);
        simpleItemRecyclerViewAdapter.loadNewData(movieItems);
    }

    public void loadFavorites() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("MovieItem");
        query.whereEqualTo("author", ParseUser.getCurrentUser());


        setProgressBarIndeterminateVisibility(true);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    // If there are results, update the list of movies
                    // and notify the adapter
                    movieItemList.clear();
                    for (ParseObject movie : objects) {
                        MovieItem favMovieItem = loadItems(movie);
                        movieItemList.add(favMovieItem);

                    }
                    if (movieItemList.size() > 0) {
                        updateDisplay(movieItemList);

                    } else {
                        loadMovieList();
                        Toast.makeText(getBaseContext(), "No movie in favorites yet!", Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Log.d(LOG_TAG, "Error: " + e.getMessage());
                }
            }

        });
        mFavorites = true;
        mPopularity = false;
        mVoteAverage = false;
    }

    public MovieItem loadItems(ParseObject movieObject) {
        MovieItem movieItems;

        String movieID = movieObject.getString("movie_id");
        String original_title = movieObject.getString("original_title");
        String overview = movieObject.getString("overview");
        String poster_path = movieObject.getString("poster_path");
        String popularity = movieObject.getString("popularity");
        String title = movieObject.getString("title");
        String vote_average = movieObject.getString("vote_average");
        String release_date = movieObject.getString("release_date");

        movieItems = new MovieItem(movieID, original_title, overview,
                poster_path, popularity, title, vote_average, release_date);

        return movieItems;
    }


    public  class SimpleItemRecyclerViewAdapter

            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private List<MovieItem> mValues;
        Context mContext;

        public SimpleItemRecyclerViewAdapter(Context context, List<MovieItem> items) {
            mContext = context;
            mValues = items;
        }

        public SimpleItemRecyclerViewAdapter( List<MovieItem> items) {

            mValues = items;
        }



        public void loadNewData(List<MovieItem> newMovies) {

            mValues = newMovies;
            notifyDataSetChanged();

        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
            final MovieItem movieData = mValues.get(position);

            Picasso.with(mContext)
                    .load("http://image.tmdb.org/t/p/w185/" + mValues.get(position).poster_path)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mImageView);


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        // Pass on details to DetailFragment;
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(MovieDetailFragment.ARG_MOVIE_DATA, movieData);

                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        // Pass on details to DetailActivity;
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra(MovieDetailFragment.ARG_MOVIE_DATA, movieData);
                        context.startActivity(intent);
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {


            public final View mView;
            public final ImageView mImageView;
            public MovieItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.image);
            }

        }
    }

}
