package com.example.mymovies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymovies.adapters.ReviewAdapter;
import com.example.mymovies.adapters.TrailerAdapter;
import com.example.mymovies.data.FavoriteMovie;
import com.example.mymovies.data.MainViewModel;
import com.example.mymovies.data.Movie;
import com.example.mymovies.data.Review;
import com.example.mymovies.data.Trailer;
import com.example.mymovies.utils.JSOButils;
import com.example.mymovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageViewBigPosterDetail;
    private TextView textViewTitle;
    private TextView textViewOriginalTitle;
    private TextView textViewRating;
    private TextView textViewRealiseDate;
    private TextView textViewOverview;
    private ImageView imageViewAddToFavorite;
    private ScrollView scrollViewInfo;

    private RecyclerView recyclerViewTrailers;
    private RecyclerView recyclerViewReviews;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;

    private int id;

    private Movie movie;
    private FavoriteMovie favoriteMovie;

    private MainViewModel viewModel;

    private static String lang;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //Достаем айди фильма, если не передало, то возвращаемся в Мейн
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }
        //получаем Муви через ВьюМодел
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        movie = viewModel.getMovieById(id);

        imageViewAddToFavorite = findViewById(R.id.imageViewAddToFavorite);
        imageViewBigPosterDetail = findViewById(R.id.imageViewBigPosterDetail);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewRating = findViewById(R.id.textViewRating);
        textViewRealiseDate = findViewById(R.id.textViewRealiseDate);
        textViewOverview = findViewById(R.id.textViewOverview);
        scrollViewInfo=findViewById(R.id.ScrollViewInfo);

        //устанавливаем значения из полученного Муви
        Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.staron).into(imageViewBigPosterDetail);
        textViewTitle.setText(movie.getTitle());
        textViewOriginalTitle.setText(movie.getOriginalTitle());
        textViewRating.setText(Double.toString(movie.getVoteAverage()));
        textViewRealiseDate.setText(movie.getReleaseDate());
        textViewOverview.setText(movie.getOverview());

        setFavorite();

        lang= Locale.getDefault().getLanguage();

        recyclerViewTrailers=findViewById(R.id.recyclerViewTrailers);
        recyclerViewReviews=findViewById(R.id.recyclerViewReviews);
        reviewAdapter=new ReviewAdapter();
        trailerAdapter=new TrailerAdapter();
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);
        recyclerViewTrailers.setAdapter(trailerAdapter);
        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideo(movie.getId(),lang);
        JSONObject jsonObjectReview = NetworkUtils.getJSONForReview(movie.getId(),lang);
        ArrayList<Trailer> trailers= JSOButils.getTrailersFromJson(jsonObjectTrailers);
        ArrayList<Review> reviews=JSOButils.getReviewsFromJson(jsonObjectReview);
        reviewAdapter.setReviews(reviews);
        trailerAdapter.setTrailers(trailers);
        //Слушалтель трейлеров
        trailerAdapter.setOnTrailerClickListener(new TrailerAdapter.OnTrailerClickListener() {
            @Override
            public void OnTrailerClick(String url) {
                Intent intentToTrailer=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intentToTrailer);
            }
        });
        scrollViewInfo.smoothScrollTo(0,0);
    }
    //Звездочка
    public void onClickChangeFavorite(View view) {
        if (favoriteMovie == null) {
            viewModel.insertFavoriteMovie(new FavoriteMovie(movie));
            Toast.makeText(this, R.string.add_to_favorite, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteFavoriteMovie(favoriteMovie);
            Toast.makeText(this, R.string.delete_from_favorite, Toast.LENGTH_SHORT).show();
        }
        setFavorite();
    }

    private void setFavorite() {
        favoriteMovie = viewModel.getFavoriteMovieById(id);
        if (favoriteMovie == null){
            imageViewAddToFavorite.setImageResource(R.drawable.staroff);
        } else{
            imageViewAddToFavorite.setImageResource(R.drawable.staron);
        }
    }

    //Менюшка
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.ItemMain:
                Intent intent=new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.ItemFavorite:
                Intent intentToFavorite=new Intent(this, FavoriteActivity.class);
                startActivity(intentToFavorite);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}