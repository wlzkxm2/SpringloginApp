package com.example.springloginapp.db;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.springloginapp.MainActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface userDao {

    @Query("SELECT * FROM userEntity")
    List<userEntity> getAll();

    @Query("SELECT * FROM userEntity WHERE uid IN (:userIds)")
    List<userEntity> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM userEntity WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    userEntity findByName(String first, String last);

    @Insert
    void insertAll(userEntity... users);

    @Delete
    void delete(userEntity user);

}
