package com.example.springloginapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.room.Room;

import com.example.springloginapp.db.userAbs;
import com.example.springloginapp.dbmanagement.FileUploadUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ir.androidexception.roomdatabasebackupandrestore.Backup;
import ir.androidexception.roomdatabasebackupandrestore.Restore;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class login extends Activity implements View.OnClickListener {

    Button backupdb, restoredb, uploaddb, loaddb;
    File tempSelectFile;

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
        switch (v.getId()) {
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

    public void BackUpDB(Context context) {
        new Backup.Init()
                .database(database)
                .path(getFilesDir().getPath())
                .fileName(userId + "_DB.txt")
                .secretKey("probono")
                .onWorkFinishListener((success, message) -> Toast.makeText(login.this, message, Toast.LENGTH_SHORT).show())
                .execute();
    }

    public void RestoreDB() {
        new Restore.Init()
                .database(database)
                .backupFilePath(getFilesDir() + File.separator + (userId + "_DB.txt"))
                .secretKey("probono")
                .onWorkFinishListener((success, message) -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .execute();

    }

    public void UploadServerFiles() {
        try {
            tempSelectFile = new File(getFilesDir() + File.separator + (userId + "_DB.txt"));
            CustomTasks task = new CustomTasks();
            String result = task.execute(userId).get();
            FileUploadUtils.send2Server(tempSelectFile);

        } catch (Exception e) {

        }
    }

    class CustomTasks extends AsyncTask<String, Void, String> {

        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {

                String str;
                URL url = new URL("http://210.103.48.199:3306/upload");
//                URL url = new URL("http://118.235.12.28:80/android");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //데이터를 POST 방식으로 전송합니다.
                conn.setDoOutput(true);

                // 서버에 보낼 값 포함해 요청함.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id=" + strings[0]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter에 담아 전송
                osw.flush();

                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();

                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                } else {
                    Log.i("통신 결과", conn.getResponseMessage() + "에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            // 서버에서 보낸 값을 리턴합니다.
            return receiveMsg;
        }


    }
}