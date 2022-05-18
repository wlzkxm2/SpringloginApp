package com.example.springloginapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.room.Room;

import com.example.springloginapp.db.userAbs;
import com.example.springloginapp.dbmanagement.FileUpload;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ir.androidexception.roomdatabasebackupandrestore.Backup;
import ir.androidexception.roomdatabasebackupandrestore.Restore;

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
        String url_address = "http://210.103.48.199:80/upload";
        String files = getFilesDir() + File.separator + (userId + "_DB.txt");
        try{
            FileInputStream fis =new FileInputStream(files);
            URL url=new URL(url_address);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            //웹서버를 통해 입출력 가능하도록 설정
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);//캐쉬 사용하지 않음
            conn.setRequestMethod("POST");
            //정해진 시간 내에 재접속할 경우 소켓을 새로 생성하지 않고 기존연결 사용
            //대소문자 주의
            conn.setRequestProperty("Connection", "Keep-Alive");
            //첨부파일에 대한 정보
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=files");

            //데이터 아웃풋 스트림
            DataOutputStream dos =new DataOutputStream(conn.getOutputStream());
            //form-data;name=파일변수명;filename="첨부파일이름"
            //작은따옴표를 사용할 수 없음
            dos.writeBytes("--files\r\n"); // --은 파일 시작 알림 표시
            dos.writeBytes("Content-Disposition: form-data; name=\"file1\"; filename=\""
                    +files+"\""+"\r\n");

            dos.writeBytes("\r\n");//줄바꿈 문자
            int bytes=fis.available();
            int maxBufferSize=1024;
            //Math.min(A, B)둘중 작은값;
            int bufferSize =Math.min(bytes, maxBufferSize);
            byte[] buffer=new byte[bufferSize];
            int read=fis.read(buffer, 0, bufferSize);
            while(read >0){
                //서버에 업로드
                dos.write(buffer,0, bufferSize);
                bytes=fis.available();
                bufferSize=Math.min(bytes, maxBufferSize);
                //읽은 바이트 수
                read=fis.read(buffer, 0, bufferSize);
            }
            dos.writeBytes("\r\n");//줄바꿈 문자

           /*boundary=경계문자 => 경계문자의 이름
        --경계문자 => 첨부파일 전송 시작부분
        --경계문자--      => 첨부파일 전송 끝부분*/

            dos.writeBytes("--files--\r\n");
            fis.close();//스트림 닫기
            dos.flush();//버퍼 클리어
            dos.close();//출력 스트림 닫기


            //서버의 응답을 처리
            int ch;
            InputStream is=conn.getInputStream(); //입력스트림
            StringBuffer sb=new StringBuffer();
            while( (ch=is.read()) != -1){ // 내용이 없을 때까지 반복
                sb.append((char)ch); // 문자를 읽어서 저장
            }
            // 스트링.trim() 스트링의 좌우 공백 제거
            str = sb.toString().trim();
            if(str.equals("success")){
                str = "파일이 업로드되었습니다.";
            }else if(str.equals("fail")){
                str = "파일 업로드 실패...";
            }
//안드로이드에서는 백그라운드 스레드에서 메인UI를 터치할 수 없음
// runOnUiThread()를 사용하면 백그라운드 스레드에서
//   메인UI를 직접 수정할 수 있음
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v("db", str);
                    Toast.makeText(login.this,
                            "업로드되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });

            is.close();
            conn.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }


    }


}
