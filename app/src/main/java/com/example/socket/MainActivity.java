package com.example.socket;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.Debug;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.RenderNode;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Socket cSocket;
    PrintWriter pw;
    Button connectButton;
    Button sendButton;
    ImageButton settingButton;
    Button disconnectButton;
    Handler handler;
    BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初期は非表示
        findViewById(R.id.sendLayout).setVisibility(View.GONE);


        handler = new Handler();

        //ボタンを変数に代入
        connectButton = findViewById(R.id.connectButton);
        sendButton = findViewById(R.id.sendButton);
        settingButton = findViewById(R.id.settingButton);
        disconnectButton = findViewById(R.id.disconnectButton);


        //初期値取得
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultAddr = sharedPref.getString("addr", "empty");
        int defaultPort = sharedPref.getInt("port", -1);
        //初期値が存在すればセットする
        if (!defaultAddr.equals("empty")) {
            ((EditText) findViewById(R.id.addrEditText)).setText(defaultAddr);
        }
        if ((defaultPort != -1)) {
            ((EditText) findViewById(R.id.portEditText)).setText(((Integer) defaultPort).toString());
        }

        settingButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == settingButton.getId()) {
            MyDialog dialog = new MyDialog();
            dialog.show(this.getSupportFragmentManager(), null);

        }

        if (v.getId() == connectButton.getId()) {
            String addr;
            int port;
            try {
                 addr = ((EditText) findViewById(R.id.addrEditText)).getText().toString();
                 port = Integer.parseInt(((EditText) findViewById(R.id.portEditText)).getText().toString());
            }catch (Exception e){// ←こんな大雑把なことはしちゃだめ
                Toast.makeText(MainActivity.this.getApplicationContext(),
                        "ipまたはportが入力されていません",
                        Toast.LENGTH_LONG).show();
                return;//returnで以降の処理を無視するけどこの書き方は良くないかもしれない
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cSocket = new Socket();
                        cSocket.connect(new InetSocketAddress(addr, port), 1000);//接続

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this.getApplicationContext(),
                                        "接続に成功しました。",
                                        Toast.LENGTH_LONG).show();
                                findViewById(R.id.connectLayout).setVisibility(View.GONE);
                                findViewById(R.id.sendLayout).setVisibility(View.VISIBLE);

                            }
                        });


                        new Thread(new Runnable() {
                            @Override
                            public void run() {


                                try {
                                    reader = new BufferedReader
                                            (new InputStreamReader
                                                    (cSocket.getInputStream()));
                                    while (true) {
                                        String res = reader.readLine();
                                        handler.post(() -> {
                                            ((EditText)findViewById(R.id.resEditText)).setText(res);
                                        });
                                    }

                                } catch (IOException e) {
                                    handler.post(() -> {
                                        Toast.makeText(MainActivity.this.getApplicationContext(),
                                                "切断されました。",
                                                Toast.LENGTH_LONG).show();
                                        findViewById(R.id.connectLayout).setVisibility(View.VISIBLE);
                                        findViewById(R.id.sendLayout).setVisibility(View.GONE);
                                        (((EditText) findViewById(R.id.commandEditText))).setText("");
                                    });
                                    e.printStackTrace();
                                }
                            }

                        }).start();

                        pw = new PrintWriter(cSocket.getOutputStream(), true);


                    } catch (IOException e) {
                        //接続に失敗したとき
                        try {
                            cSocket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                        //この例外処理はあまり適切ではない。どのような理由で接続に失敗したのかを
                        // 細分化するためにcatchするExceptionを具体的なものにするべき
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this.getApplicationContext(),
                                        "接続に失敗しました。 ip, portが正しいか、サーバが正常に稼働しているか確認してください。",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            }).start();


        }

        if (v.getId() == sendButton.getId()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        pw = new PrintWriter(cSocket.getOutputStream(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    pw.println((((EditText) findViewById(R.id.commandEditText))).getText().toString());
                }
            }).start();


        }

        if (v.getId() == disconnectButton.getId()) {
            try {
                cSocket.close();
                pw.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            (((EditText) findViewById(R.id.commandEditText))).setText("");
            findViewById(R.id.connectLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.sendLayout).setVisibility(View.GONE);


        }
    }
}
