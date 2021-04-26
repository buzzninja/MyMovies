package com.example.mymovies.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {Movie.class, FavoriteMovie.class}, version = 4,exportSchema = false)
public abstract class MovieDatabase extends RoomDatabase {

    private static final String DB_NAME = "movies.db";
    private static final String DB_VERSION = "1";

    private static MovieDatabase database;

    private static final Object LOCK = new Object();

    //SINGLETON
    public static MovieDatabase getInstance(Context context) {
        synchronized (LOCK) {
            if (database == null) {
                database = Room.databaseBuilder(context, MovieDatabase.class, DB_NAME)
                         .fallbackToDestructiveMigration()/*автоматически удаляет старые данные и добавляет новые при смене версии*/
                        .build();
            }
        }
        return database;
    }

    public abstract MovieDao movieDao();
}
