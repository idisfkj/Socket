package com.idisfkj.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by idisfkj on 16/5/28.
 * Email : idisfkj@qq.com.
 */
public class ServerSocket {

    public static List<Socket> list = new ArrayList<>();

    public static void main(String[] args) {
        // write your code here
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    java.net.ServerSocket serverSocket = new java.net.ServerSocket(30000);
                    while (true) {
                        //接收连接请求
                        Socket s = serverSocket.accept();
                        list.add(s);
                        //为每一个连接开启一个的线程
                        new Thread(new ServiceRunnable(s)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public static class ServiceRunnable implements Runnable {

        public Socket s;
        public BufferedReader br;
        public PrintWriter pw;

        public ServiceRunnable(Socket s) throws IOException {
            this.s = s;
            //初始化输入流
            br = new BufferedReader(new InputStreamReader(s.getInputStream(), "utf-8"));
        }

        @Override
        public void run() {
            String content;
            System.out.println(s.getLocalAddress());
            System.out.println(s.getPort());

            while ((content = readContent()) != null) {
                //遍历所有连接了的Socket，向所有客户端Socket发送消息
                for (Iterator<Socket> it = list.iterator(); it.hasNext(); ) {
                    Socket socket = it.next();
                    try {
                        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        pw.println(content);
                    } catch (IOException e) {
                        it.remove();
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 读取客户端发来的消息
         * @return
         */
        public String readContent() {
            try {
                return br.readLine();
            } catch (IOException e) {
                list.remove(s);
                e.printStackTrace();
            }
            return null;
        }
    }
}
