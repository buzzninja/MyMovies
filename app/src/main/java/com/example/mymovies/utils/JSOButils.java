package com.example.mymovies.utils;

import android.util.Log;

import com.example.mymovies.data.Movie;
import com.example.mymovies.data.Review;
import com.example.mymovies.data.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSOButils {

    //Ключи для других ДЖИСОНОВ, отзывы
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_CONTENT = "content";
    //Ключи для других ДЖИСОНОВ, видео
    private static final String KEY_VIDEO_KEY = "key";
    private static final String KEY_VIDEO_NAME = "name";
    private static final String BASE_YOU_TUBE_URL = "https://www.youtube.com/watch?v=";
    //разделы JSONа для удобства
    private static final String KEY_RESULTS = "results";
    private static final String KEY_VOTECOUNT = "vote_count";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ORIGINALTITLE = "original_title";
    private static final String KEY_OVERVIEW = "overview";
    private static final String KEY_POSTERPATH = "poster_path";
    private static final String KEY_BACKDROP = "backdrop_path";
    private static final String KEY_VOTEAVERAGE = "vote_average";
    private static final String KEY_RELEASEDATE = "release_date";

    //Posters
    public static final String Base_Poster_URL = "https://image.tmdb.org/t/p/";
    public static final String SMALL_POSTER_SIZE = "w185";
    public static final String BIG_POSTER_SIZE = "w780";

    //Метод получения ДЖИСОНА данных о фильме
    public static ArrayList<Movie> getMoviesFromJson(JSONObject jsonObject) {
        ArrayList<Movie> result = new ArrayList<>();
        if (jsonObject == null) {

            return result;
        }
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(KEY_RESULTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objectMovie = jsonArray.getJSONObject(i);

                int id = objectMovie.getInt(KEY_ID);
                int voteCount = objectMovie.getInt(KEY_VOTECOUNT);
                String title = objectMovie.getString(KEY_TITLE);
                String orginalTitle = objectMovie.getString(KEY_ORIGINALTITLE);
                String overview = objectMovie.getString(KEY_OVERVIEW);
                String posterPath = Base_Poster_URL+SMALL_POSTER_SIZE+ objectMovie.getString(KEY_POSTERPATH);
                String BigPosterPath = Base_Poster_URL+BIG_POSTER_SIZE+ objectMovie.getString(KEY_POSTERPATH);
                String backDrop = objectMovie.getString(KEY_BACKDROP);
                double voteAverage = objectMovie.getDouble(KEY_VOTEAVERAGE);
                String releaseDate = objectMovie.getString(KEY_RELEASEDATE);
                Log.i("orig",orginalTitle);
                Movie movie = new Movie(id, voteCount, title, orginalTitle, overview, posterPath,BigPosterPath, backDrop, voteAverage, releaseDate);
                result.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
//Метод получения ДЖИСОНА отзывов
public static ArrayList<Review> getReviewsFromJson(JSONObject jsonObject){
    ArrayList<Review> result = new ArrayList<>();
    if (jsonObject == null) {

        return result;    }
    try {
        JSONArray jsonArray=jsonObject.getJSONArray(KEY_RESULTS);
        for (int i=0;i<jsonArray.length();i++){
            JSONObject jsonObjectReview=jsonArray.getJSONObject(i);
            String author=jsonObjectReview.getString(KEY_AUTHOR);
            String content = jsonObjectReview.getString(KEY_CONTENT);
            result.add(new Review(author,content));
                    }
    } catch (JSONException e) {
        e.printStackTrace();
    }
    return result;
}
    //Метод получения ДЖИСОНА трейлера
    public static ArrayList<Trailer> getTrailersFromJson(JSONObject jsonObject){
        ArrayList<Trailer> result = new ArrayList<>();
        if (jsonObject == null) {
            return result;    }
        try {
            JSONArray jsonArray=jsonObject.getJSONArray(KEY_RESULTS);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObjectTrailer=jsonArray.getJSONObject(i);
                String name=jsonObjectTrailer.getString(KEY_VIDEO_NAME);
                String key = BASE_YOU_TUBE_URL+jsonObjectTrailer.getString(KEY_VIDEO_KEY);
                result.add(new Trailer(name,key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
