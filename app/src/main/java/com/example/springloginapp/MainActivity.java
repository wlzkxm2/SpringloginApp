package com.example.springloginapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SimpleSQLiteQuery;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.springloginapp.db.userAbs;
import com.example.springloginapp.db.userDao;
import com.example.springloginapp.db.userEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import ir.androidexception.roomdatabasebackupandrestore.Backup;
import ir.androidexception.roomdatabasebackupandrestore.Restore;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText id, pw;
    Button login_, register_, backup_, restore_;

    private userAbs database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(getApplicationContext(),
                userAbs.class, "user")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();


        database.userdao().insertAll(new userEntity("lee", "jiwon"));

        id = (EditText) findViewById(R.id.id_edit);
        pw = (EditText) findViewById(R.id.pw_edit);
        login_ = (Button) findViewById(R.id.login_btn);
        register_ = (Button) findViewById(R.id.register_btn);
        backup_ = (Button) findViewById(R.id.Backup_btn);
        restore_ = (Button) findViewById(R.id.Restore_btn);


        login_.setOnClickListener(this);
        register_.setOnClickListener(this);
        backup_.setOnClickListener(this);
        restore_.setOnClickListener(this);



    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.register_btn:
                Intent i = new Intent(MainActivity.this, Rigister.class);
                startActivity(i);
                finish();
                break;

            case R.id.Backup_btn:
                BackUpDB(this);
                break;

            case R.id.Restore_btn:
                RestoreDB();
                break;
        }
    }

     void login() {
        Log.v("login", "로그인 하는중");
        try{
            String userid = id.getText().toString();
            String userpw = pw.getText().toString();

            Log.v("login", "보낸값 : " + userid + " / " + userpw);

            CustomTask task = new CustomTask();
            String result = task.execute(userid, userpw).get();
            int FO = result.lastIndexOf("login");
            int LO = result.lastIndexOf("</title>");
            String findresults = result.substring(FO, LO);

            Log.w("받은 값", result);
            Log.w("찾은 값 : ", Integer.toString(FO));
            Log.w("찾은 값 : ", findresults);

            Intent intent2 = new Intent(MainActivity.this, login.class);
            startActivity(intent2);
            finish();


        }catch (Exception e){

        }
    }

    class CustomTask extends AsyncTask<String, Void, String>{

        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {

                String str;
                URL url = new URL("http://210.103.48.199:80/android");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");                              //데이터를 POST 방식으로 전송합니다.
                conn.setDoOutput(true);

                // 서버에 보낼 값 포함해 요청함.
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id="+strings[0]+"&pw="+strings[1]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
                osw.write(sendMsg);                           // OutputStreamWriter에 담아 전송
                osw.flush();

                if (conn.getResponseCode() == conn.HTTP_OK){
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();

                    while ((str = reader.readLine())!= null){
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                }else{
                    Log.i("통신 결과", conn.getResponseMessage() + "에러");
                }

            }catch (MalformedURLException e){
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            // 서버에서 보낸 값을 리턴합니다.
            return receiveMsg;
        }
    }

    public void BackUpDB(Context context){
        new Backup.Init()
                .database(database)
                .path(getFilesDir().getPath())
                .fileName("calDB.txt")
                .secretKey("SalehYarahmadi")
                .onWorkFinishListener((success, message) -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show())
                .execute();
    }

    public void RestoreDB(){
        new Restore.Init()
                .database(database)
                .backupFilePath(getFilesDir() + File.separator + "calDB.txt")
                .secretKey("SalehYarahmadi")
                .onWorkFinishListener((success, message) -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .execute();

    }
}