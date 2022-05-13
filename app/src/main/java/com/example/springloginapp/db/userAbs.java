package com.example.springloginapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {userEntity.class}, version = 1)
public abstract class userAbs extends RoomDatabase{
        public abstract userDao userdao();

        private static userAbs INSTANCE;

        private static final Object sLock = new Object();

        public static userAbs getInstance(Context context) {
                synchronized (sLock) {
                        if(INSTANCE==null) {
                                INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                                                userAbs.class, "user.db")
                                        .build();
                        }
                        return INSTANCE;
                }
        }

}
