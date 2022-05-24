package com.example.springloginapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Rigister extends Activity implements View.OnClickListener {

    EditText inputID, inputPW;
    Button registernow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rigister);

        inputID = (EditText) findViewById(R.id.inputID_edit);
        inputPW = (EditText) findViewById(R.id.inputPW_edit);
        registernow = (Button) findViewById(R.id.registernow_btn);

        registernow.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registernow_btn:
                register();
                break;
        }
    }

    void register() {
        Log.v("register", "회원가입 하는중");

        try {
            String userID = inputID.getText().toString();
            String userPW = inputPW.getText().toString();

            Log.v("register", "보낸 값" + userID + " / " + userPW);

            Rigister.registerTask register_task = new Rigister.registerTask();

            String result = register_task.execute(userID, userPW).get();     // 서버가 전송한 값을 String 값으로

            Intent i = new Intent(Rigister.this, MainActivity.class);
            startActivity(i);
            finish();
        } catch (Exception e) {

        }
    }

    class registerTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://210.103.48.199:3306/andrigister");

//                URL url = new URL("http://118.235.12.28:80/andrigister");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id=" + strings[0] + "&pw=" + strings[1]; // GET방식으로 작성해 POST로 보냄 ex) "id=admin&pwd=1234";
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

    /*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                login();
        }
    }
    */
/*
    void login() {
        Log.v("login", "로그인 하는중");
        try{
            String userid = id.getText().toString();
            String userpw = pw.getText().toString();

            Log.v("login", "보낸값 : " + userid + " / " + userpw);

            MainActivity.CustomTask task = new MainActivity.CustomTask();
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
    */
/*
    class CustomTask extends AsyncTask<String, Void, String> {

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

}
*/
