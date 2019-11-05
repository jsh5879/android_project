package com.example.iyeeun.qr_termproject;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class RegisterActivity extends AppCompatActivity {

    EditText idText;
    EditText passwordText;
    EditText passwordCheckText;
    EditText nameText;
    EditText phoneNumber;
    String checkedID; //중복확인이 완료된 아이디를 저장하는 곳
    boolean idchk = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        passwordCheckText = (EditText) findViewById(R.id.passwordCheckText);
        nameText = (EditText) findViewById(R.id.nameText);
        phoneNumber = (EditText) findViewById(R.id.phoneNumber);

        final Button idCheckButton = (Button) findViewById(R.id.idCheckButton);
        final Button registerButton = (Button) findViewById(R.id.registerButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelButton);
        final Button comfortable_number_Button = (Button) findViewById(R.id.button_comfortable_number);


        //안심번호 생성 URL로 이동
        comfortable_number_Button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://callmix.co.kr/html/index.html"));
                startActivity(intent);
            }

        });

        //아이디 중복확인
        idCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v){
                new RegisterActivity.JSONidchk().execute(StaticStringUtil.SERVER_URL + "/idCheck");

                try {
                    checkedID = new JSONidchk().execute(StaticStringUtil.SERVER_URL + "/idCheck").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
//                Log.e("----------checkedID (JSON get test)", checkedID);
                if(idText.getText().toString().equals(checkedID)){
                    idchk = true;
                    idCheckButton.setText("OK");
                    idText.setFocusable(false);
                    Toast.makeText(getApplicationContext(), "사용 가능한 ID입니다.\n중복확인 후에는 ID를 변경 할 수 없습니다.\n변경을 원하는 경우 회원가입 취소 후\n다시 실행해주세요.", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "중복된 ID 입니다.\n다른 ID를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //중복확인 후 회원가입

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(idchk && idText.getText().toString().equals(checkedID)){
                    if(passwordText.getText().toString().equals(passwordCheckText.getText().toString())){
                        new JSONregister().execute(StaticStringUtil.SERVER_URL + "/register");//AsyncTask 시작시킴
                        Log.e(StaticStringUtil.SERVER_URL, "/register");

                        //registerActivity 종료 후 loginActivity 로 이동(새로운 창 생성 X)
                        Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                        registerIntent.putExtra("result", ""+checkedID);
                        setResult(RESULT_OK, registerIntent);
                        finish();


                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Password 확인 실패.", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "ID 중복확인 후에 회원가입 하세요.", Toast.LENGTH_LONG).show();
                }

            }
        });

        //회원가입 취소 -> 로그인으로 돌아감
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancelIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                cancelIntent.putExtra("result", "register canceled");
                setResult(RESULT_CANCELED, cancelIntent);
                finish();
            }
        });
    }



    public class JSONidchk extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {


                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                Log.e("log test " , idText.getText().toString());
                jsonObject.accumulate("uid", idText.getText().toString());
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    System.out.println(urls[0]);
                    URL url = new URL(urls[0]);

                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송


                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

//                    checkedID = buffer.toString().trim();
//                    Log.e("id check: " , checkedID);

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
//            Log.e("idCHK onPostExe test :" , result );
        }
    }


    public class JSONregister extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {


                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
//                System.out.println("log test " + idText.getText().toString());
                jsonObject.accumulate("uid", idText.getText().toString());
                jsonObject.accumulate("password", passwordText.getText().toString());
                jsonObject.accumulate("name", nameText.getText().toString());
                jsonObject.accumulate("phone", phoneNumber.getText().toString());

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    System.out.println(urls[0]);
                    URL url = new URL(urls[0]);

                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송


                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

//                    checkedID = buffer.toString();
//                    System.out.println(checkedID);

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            System.out.println("register response test :" + result );
        }
    }

    public void returnMain(){
        Intent it = new Intent(this,LoginActivity.class);
        startActivity(it);
        finish();
    }


    @Override
    public void onBackPressed() {
        returnMain();
    }



}