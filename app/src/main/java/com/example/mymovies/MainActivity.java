package com.example.mymovies;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymovies.adapters.MovieAdapter;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.example.mymovies.utils.JSOButils;
import com.example.mymovies.utils.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private Switch switchSort;
    private ProgressBar progressBarLoading;


    //DB
    private MainViewModel viewModel;
    //Уникальынй параметр для загрузки
    private static final int LOADER_ID = 1;
    private LoaderManager loaderManager;
    private static int page = 1;
    private static boolean isLoading = false;
    private int sortMethod;
    private static String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Для улучшенного загрузчика. Использует Синглтон
        loaderManager = LoaderManager.getInstance(this);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);


        recyclerView = findViewById(R.id.RecyclerViewPosters);
        movieAdapter = new MovieAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        recyclerView.setAdapter(movieAdapter);
        lang = Locale.getDefault().getLanguage();

        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (page == 1) {
                    movieAdapter.setMovies(movies);
                }
            }
        });
        progressBarLoading = findViewById(R.id.progressBarLoading);
        switchSort = findViewById(R.id.switch_sort);
        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                page = 1;

                if (isChecked) {
                    sortMethod = NetworkUtils.TOP_VOTE;
                } else {
                    sortMethod = NetworkUtils.POPULARITY;
                }
                downLoadData(sortMethod, page);

            }
        });

        switchSort.setChecked(false);
        //Listener click
        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void OnPosterClick(int position) {
                //получаем выбранный Муви
                Movie movie = movieAdapter.getMovies().get(position);

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                startActivity(intent);
            }
        });
        //Listener on end reach
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void OnReachEnd() {
                if (!isLoading) {
                    downLoadData(sortMethod, page);
                }

            }
        });
    }

    //Метод расчитывает сколько делать колонок с фильмами
    private int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        //тернарная операция, если да - возвращаем деление, если нет - 2
        return width / 185 > 2 ? width / 185 : 2;
    }

    private void downLoadData(int methodOfSort, int page) {
        URL url = NetworkUtils.buildURL(methodOfSort, page, lang);
        Log.i("url1", url.toString());
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }


    //Менюшка
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.ItemMain:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.ItemFavorite:
                Intent intentToFavorite = new Intent(this, FavoriteActivity.class);
                startActivity(intentToFavorite);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    //LoadCall методы для определения статуса загрузки
    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListner(new NetworkUtils.JSONLoader.OnStartLoadingListner() {
            @Override
            public void onStartLoading() {
                progressBarLoading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        ArrayList<Movie> movies = JSOButils.getMoviesFromJson(data);
        if (movies != null && !movies.isEmpty()) {
            if (page == 1) {
                viewModel.deleteAllMovies();
                movieAdapter.ClearMovies();
            }
            for (Movie movie : movies) {
                viewModel.insertMovie(movie);
            }
            //Здесь завершение загрузки
            movieAdapter.AddMovies(movies);
            page++;


        }

        isLoading = false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
}