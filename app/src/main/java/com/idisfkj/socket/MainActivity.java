package com.idisfkj.socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv;
    private EditText et;
    private Button bt;
    private Socket socket;
    private ClientThread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        et = (EditText) findViewById(R.id.et);
        bt = (Button) findViewById(R.id.bt);
        bt.setOnClickListener(this);
        clientThread = new ClientThread(mHandler);
        new Thread(clientThread).start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                tv.append(msg.obj.toString());
            }
        }
    };

    @Override
    public void onClick(View v) {
        Message message = new Message();
        message.what = 2;
        message.obj = et.getText().toString();
        clientThread.clientHandler.sendMessage(message);
        et.setText("");
    }

    public class ClientThread implements Runnable {
        private Handler handler;
        private BufferedReader br;
        private PrintWriter pw;
        private Handler clientHandler;

        public ClientThread(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                socket = new Socket("192.168.56.1", 30000);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                new Thread(new Runnable() {
                    String line;

                    @Override
                    public void run() {
                        try {
                            while ((line = br.readLine()) != null) {
                                Message message = new Message();
                                message.what = 1;
                                message.obj = line;
                                handler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

//                Looper.prepare();
                clientHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 2) {
                            try {
                                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                                pw.println(msg.obj);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
//                Looper.loop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
