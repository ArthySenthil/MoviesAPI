package com.senarita.moviesapi.movie;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;


public class MovieContent {

    /**
     * An array of movie items.
     */
    public static final List<MovieItem> ITEMS = new ArrayList<>();


    public static List<MovieItem> getMovieContents(String jsonString) {
        JsonParser jasonParser = new JsonParser();
        JsonObject jsonObject = jasonParser.parse(jsonString).getAsJsonObject();

        //data array of the results json object
        JsonElement resultElement = jsonObject.getAsJsonObject().get("results");
        JsonArray movieArray = resultElement.getAsJsonArray();
        ITEMS.clear();

        for (JsonElement object : movieArray) {
            Gson gson = new Gson();
            MovieItem movieItem = gson.fromJson(object, MovieItem.class);
            ITEMS.add(movieItem);

        }

        return ITEMS;
    }



    public static class MovieItem implements Parcelable {
        public String id;
        public String original_title;
        public String overview;
        public String poster_path;
        public String popularity;
        public String title;
        public String vote_average;
        public String release_date;

        public MovieItem(String id,
                         String original_title, String overview,
                         String poster_path, String popularity,
                         String title, String vote_average,
                         String release_date) {
            this.id = id;
            this.original_title = original_title;
            this.overview = overview;
            this.poster_path = poster_path;
            this.popularity = popularity;
            this.title = title;
            this.vote_average = vote_average;
            this.release_date = release_date;
        }



        public String getId() {
            return id;
        }

        public String getOriginal_title() {
            return original_title;
        }

        public String getOverview() {
            return overview;
        }

        public String getPoster_path() {
            return poster_path;
        }

        public String getPopularity() {
            return popularity;
        }

        public String getTitle() {
            return title;
        }

        public String getVote_average() {
            return vote_average;
        }

        public String getRelease_date() {
            return release_date;
        }

        @Override
        public String toString() {
            return "MovieItem{" +
                    "id='" + id + '\'' +
                    ", original_title='" + original_title + '\'' +
                    ", overview='" + overview + '\'' +
                    ", poster_path='" + poster_path + '\'' +
                    ", popularity='" + popularity + '\'' +
                    ", title='" + title + '\'' +
                    ", vote_average='" + vote_average + '\'' +
                    ", release_date='" + release_date + '\'' +
                    '}';
        }

        protected MovieItem(Parcel in) {
            id = in.readString();
            original_title = in.readString();
            overview = in.readString();
            poster_path = in.readString();
            popularity = in.readString();
            title = in.readString();
            vote_average = in.readString();
            release_date = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(original_title);
            dest.writeString(overview);
            dest.writeString(poster_path);
            dest.writeString(popularity);
            dest.writeString(title);
            dest.writeString(vote_average);
            dest.writeString(release_date);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
            @Override
            public MovieItem createFromParcel(Parcel in) {
                return new MovieItem(in);
            }

            @Override
            public MovieItem[] newArray(int size) {
                return new MovieItem[size];
            }
        };
    }


}
