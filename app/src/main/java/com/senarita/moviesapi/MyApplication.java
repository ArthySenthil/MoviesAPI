package com.senarita.moviesapi;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.parse.Parse;

/**
 * Created by Arthy on 12/4/2015.
 */
public class MyApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this,BuildConfig.PARSE_APP_ID,BuildConfig.PARSE_CLIENT_ID);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.edit().putBoolean(MovieListActivity.SORT_BY_POPULARITY, true).apply();
        sharedPreferences.edit().putBoolean(MovieListActivity.SORT_BY_FAVORITES, false).apply();
        sharedPreferences.edit().putBoolean(MovieListActivity.SORT_BY_VOTE_AVERAGE,false).apply();
        sharedPreferences.edit().putString(MovieListActivity.SORT_BY,MovieListActivity.DEFAULT_SORT).apply();

    }



}


