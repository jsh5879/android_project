package com.example.iyeeun.qr_termproject;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import org.json.JSONArray;
import org.json.JSONException;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {
    final static int ACT_EDIT = 0;
    public TextView message;
    public Button modifyButton;
    public Button logoutButton;

    ImageView qrCode;
    TextView viewID;
    TextView viewName;
    TextView viewNumber;
    String myinform;
    UserInform myObj = new UserInform();
    String savedID;
    String qrurl;


    String TAG = "GenerateQRCode";
    String inputValue;
    QRGEncoder qrgEncoder;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        message = (TextView)findViewById(R.id.viewMessage);
        modifyButton = (Button)findViewById(R.id.modifyButton);

        viewName = (TextView) findViewById(R.id.viewName);
        viewNumber = (TextView) findViewById(R.id.viewNumber);
        logoutButton = (Button) findViewById(R.id.logoutButton);

        SharedPreferences pref = getSharedPreferences("myFile", MODE_PRIVATE);
        String text2 = pref.getString("ID", "");
        savedID = text2;

        qrCode = (ImageView) findViewById(R.id.qrCode);
        viewID = (TextView) findViewById(R.id.viewID);

        qrurl = StaticStringUtil.SERVER_URL + "/mypage?id=" + savedID;

        inputValue = qrurl;
        if (inputValue.length() > 0) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                qrCode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.v(TAG, e.toString());
            }
        } else {
            viewID.setError("Required");
        }


        new MainActivity.JSONmain().execute(StaticStringUtil.SERVER_URL + "/mainpage");

        try {
            myinform = new JSONmain().execute(StaticStringUtil.SERVER_URL + "/mainpage").get();
            System.out.println(myinform);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        myObj.stringToUser(myinform);

        viewID.setText(myObj.uid);
        viewName.setText(myObj.name);
        viewNumber.setText(myObj.phonenumber);
        if(myObj.message !=null){
            message.setText(myObj.message);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(logoutIntent);
            }
        });

        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("TextIn",message.getText().toString());
                startActivityForResult(intent, ACT_EDIT);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==ACT_EDIT && resultCode == RESULT_OK){
            message.setText(data.getStringExtra("TextOut"));
        }
    }


    public class JSONmain extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {

                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("uid", savedID);

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

}
