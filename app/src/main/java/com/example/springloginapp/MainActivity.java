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
import com.example.springloginapp.dbmanagement.CSVWriter;

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


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText id, pw;
    Button login_, register_, backup_, restore_;

    public userDao userdao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        userAbs db = Room.databaseBuilder(getApplicationContext(),
                userAbs.class, "user")
                .setJournalMode(userAbs.JournalMode.TRUNCATE)       // DB 백업을 위해 저널모드로 생성
                                                                    // https://androidexplained.github.io/android/room/2020/10/03/room-backup-restore.html
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        userdao = db.userdao();

        userdao.checkpoint((new SimpleSQLiteQuery("pragma wal_checkpoint(full)")));

        List<userEntity> users = userdao.getAll();


        if(users.isEmpty() == false) {
            Log.v("db", "비어있음");
        }else{
            userEntity userentity = new userEntity();
            userentity.setUid(1);
            userentity.setFirstName("Lee");
            userentity.setLastName("jiwon");
            userdao.insertAll(userentity);
        }

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
//        String inputPath = "/data/user/0/com.example.springloginapp/databases/";     // 원본 파일 경로
        String inputFile = "user";          // 월본 파일이름
//        String outputPath = "/storage/emulated/0/backup";       // 옮길 파일 경로

//        InputStream in = null;
//        OutputStream out = null;

        try {
            String appName = context.getResources().getString(R.string.app_name);
            File file = new File(Environment.getExternalStorageDirectory() + "/" + appName + "/");
            if(!file.exists())
            {
                file.mkdirs();
            }

            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite())
            {
                File data = Environment.getDataDirectory();
                String currentDBPath= "//data//" + context.getPackageName() + "//databases//" + inputFile;
                String backupDBPath  = "/" + appName + "/" + inputFile;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileInputStream src = new FileInputStream(currentDB);
                FileOutputStream dst = new FileOutputStream(backupDB);

                // KEY Length 16 byte
                SecretKeySpec sks = new SecretKeySpec("1234567890123456".getBytes(), "AES");
                // Create cipher
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, sks);

                CipherOutputStream cos = new CipherOutputStream(dst, cipher);
                byte[] b = new byte[8];
                int i = src.read(b);
                while (i != -1) {
                    cos.write(b, 0, i);
                    i = src.read(b);
                }
                src.close();
                dst.close();
                cos.flush();
                cos.close();

                Log.e("db", "Database has been exported to\n" + backupDB.toString());
            }
            else
            {
                Log.e("db", "No storage permission.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("db", "Error exporting database!");
        }
    }

    public static void RestoreDB(){

    }
}