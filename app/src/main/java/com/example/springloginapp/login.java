package com.example.springloginapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.room.Room;

import com.example.springloginapp.db.userAbs;

import java.io.File;

import ir.androidexception.roomdatabasebackupandrestore.Backup;
import ir.androidexception.roomdatabasebackupandrestore.Restore;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class login extends Activity implements View.OnClickListener{

    Button backupdb, restoredb, uploaddb, loaddb;

    private userAbs database;

    String userId = "";
    String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        database = Room.databaseBuilder(getApplicationContext(),
                        userAbs.class, "user")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        backupdb = (Button) findViewById(R.id.Backup_btn);
        restoredb = (Button) findViewById(R.id.Restore_btn);
        uploaddb = (Button) findViewById(R.id.upload_btn);
        loaddb = (Button) findViewById(R.id.load_btn);

        Intent i = getIntent();
        userId = i.getStringExtra("idOutPut");

        backupdb.setOnClickListener(this);
        restoredb.setOnClickListener(this);
        uploaddb.setOnClickListener(this);
        loaddb.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.Backup_btn:
                BackUpDB(this);
                break;

            case R.id.Restore_btn:
                RestoreDB();
                break;

            case R.id.upload_btn:
                UploadServerFiles();
                break;

        }
    }

    public void BackUpDB(Context context){
        new Backup.Init()
                .database(database)
                .path(getFilesDir().getPath())
                .fileName(userId + "_DB.txt")
                .secretKey("probono")
                .onWorkFinishListener((success, message) -> Toast.makeText(login.this, message, Toast.LENGTH_SHORT).show())
                .execute();
    }

    public void RestoreDB(){
        new Restore.Init()
                .database(database)
                .backupFilePath(getFilesDir() + File.separator + (userId + "_DB.txt"))
                .secretKey("probono")
                .onWorkFinishListener((success, message) -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .execute();

    }

    public void UploadServerFiles(){
    }


}
