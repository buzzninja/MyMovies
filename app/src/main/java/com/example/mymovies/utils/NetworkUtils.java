package com.example.mymovies.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

public class NetworkUtils {


    private static final String Base_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String BASE_URL_VIDEOS = "https://api.themoviedb.org/3/movie/%s/videos";
    private static final String BASE_URL_REVIEWS = "https://api.themoviedb.org/3/movie/%s/reviews";
    private static final String BASE_YOU_TUBE_URL = "https://www.youtube.com/watch?v=";

    private static final String PARAMS_API_key = "api_key";
    private static final String PARAMS_LANGUAGE = "language";
    private static final String PARAMS_SORT_by = "sort_by";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_MIN_VOTE_COUNT = "vote_count.gte";


    private static final String VALUE_API_key = "6165d98394bb9f5862a6044565067e07";
    private static final String VALUE_SORT_by_POPULARITY = "popularity.desc";
    private static final String VALUE_SORT_by_TOP_VOTE = "vote_average.desc";
    private static final String VALUE_MIN_VOTE_COUNT = "1000";

    public static final int POPULARITY = 0;
    public static final int TOP_VOTE = 1;

    //Подгрузка обзоров, создание ЮРЛ
    public static URL buildURLForReviews(int id, String lang) {
        Uri uri = Uri.parse(String.format(BASE_URL_REVIEWS, id)).buildUpon().
                appendQueryParameter(PARAMS_LANGUAGE,lang).
                appendQueryParameter(PARAMS_API_key, VALUE_API_key).build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Загрузка ДЖИСОНА для отзывов
    public static JSONObject getJSONForReview(int id, String lang) {
        JSONObject result = null;
        URL url = buildURLForReviews(id,lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Подгрузка видео, создание ЮРЛ
    public static URL buildURLForVideo(int id, String lang) {
        Uri uri = Uri.parse(String.format(BASE_URL_VIDEOS, id)).buildUpon().
                appendQueryParameter(PARAMS_API_key, VALUE_API_key).
                appendQueryParameter(PARAMS_LANGUAGE, lang).build();
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Загрузка ДЖИСОНА для видео
    public static JSONObject getJSONForVideo(int id, String lang) {
        JSONObject result = null;
        URL url = buildURLForVideo(id, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Постройка строки для подгрузки данных фильма
    public static URL buildURL(int sort_by, int page, String lang) {
        //Выбор сортировки
        String method_of_sort;
        if (sort_by == POPULARITY) {
            method_of_sort = VALUE_SORT_by_POPULARITY;
        } else {
            method_of_sort = VALUE_SORT_by_TOP_VOTE;
        }

        //СБорка строки ссылки
        Uri uri = Uri.parse(Base_URL).buildUpon().
                appendQueryParameter(PARAMS_API_key, VALUE_API_key).
                appendQueryParameter(PARAMS_LANGUAGE, lang).
                appendQueryParameter(PARAMS_SORT_by, method_of_sort).
                appendQueryParameter(PARAMS_MIN_VOTE_COUNT, VALUE_MIN_VOTE_COUNT).
                appendQueryParameter(PARAMS_PAGE, Integer.toString(page)).
                build();

        //Создание и присвоение ЮРЛ с исключением
        URL result = null;
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //Если ссылка соберется - то она вернется. Если нет, то вернется НУЛЛ
        return result;
    }

    //Метод для загрузки джейсона с использование класса, описанного ниже
    public static JSONObject getJSONfromNet(int sort_by, int page, String lang) {
        JSONObject result = null;
        URL url = buildURL(sort_by, page, lang);
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //класс с АсинкТаск для загрузки джейсона
    private static class JSONLoadTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... urls) {
            if (urls == null || urls.length == 0) {
                return null;
            }
            JSONObject result = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                //Начинаем читать данные
                StringBuilder stringBuilder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
                //собранную строку присваеваем ранее собранному Джейсон объекту
                result = new JSONObject(stringBuilder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }

    //Улучшенный ЭсингТаск. В нем ЮРЛ передается на напрямую, а через объект БАНДЛ.
    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {
        //Для передачи УРЛ используется Бандл Объект
        private Bundle bundle;
        private  OnStartLoadingListner onStartLoadingListner;

        public void setOnStartLoadingListner(OnStartLoadingListner onStartLoadingListner)
        {this.onStartLoadingListner=onStartLoadingListner;}

        //Добавляем Листенер для определения начала загрузки
        public interface OnStartLoadingListner{
            void onStartLoading();
        }


        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (onStartLoadingListner!=null){
                onStartLoadingListner.onStartLoading();
            }
            forceLoad();
        }

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle=bundle;
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if (bundle ==null){
                return null;
            }
            String urlAsString=bundle.getString("url");
            URL url= null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (url == null) {
                return null;
            }
            JSONObject result = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                //Начинаем читать данные
                StringBuilder stringBuilder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
                //собранную строку присваеваем ранее собранному Джейсон объекту
                result = new JSONObject(stringBuilder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }


}
