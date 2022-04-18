package com.nhnacademy.project.network;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class NetCatServer {
    // clientId: 클라이언트 전송용 OutputStream
    private final ConcurrentHashMap<String, DataOutputStream> clientOutMap = new ConcurrentHashMap();
//    private int port;
//    ClientSession session;
static String cmd;
static String optionOrHostname;
static String port;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tokenizer = new StringTokenizer(bufferedReader.readLine(), " ");
        cmd = tokenizer.nextToken();
        optionOrHostname = tokenizer.nextToken();
        port = tokenizer.nextToken();

        if(Objects.equals(optionOrHostname, "-l")) {
            NetCatServer server = new NetCatServer();
            server.start();
        } else if (Objects.equals(optionOrHostname, "-h")){
            System.out.println("Usage: snc [option] [hostname] [port]\n" +
             "Options: \n" +
            "-l   <port>     서버 모드로 동작, 입력 받은 포트로 listen");
        } else {
            NetCatClient client = new NetCatClient();
            client.connect(optionOrHostname, (Integer.parseInt(port)));
        }

    }

    public void start() throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port))) {
            System.out.println(getTime() + " Start server " + serverSocket.getLocalSocketAddress());
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientSession client = new ClientSession(socket);
                    Sender sender = new Sender(socket);
                    client.start();
                    sender.start();
                } catch (IOException e) {
                    // TODO 클라이언트 접속 실패
                }
            }
        }
    }

    private String getTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
    }

//    private void leaveChat(ClientSession session) {
//        clientOutMap.remove(session.hostname);
//
//        sendToAll("[System] " + session.hostname + "님이 나갔습니다.");
//        System.out.println(getTime() + " " + session.hostname + " is leaved: " + session.socket.getInetAddress());
//        loggingCurrentClientCount();
//    }
//    private void sendToAll(String message) {
//        for (DataOutputStream out : clientOutMap.values()) {
//            try {
//                out.writeUTF(message);
//            } catch (IOException e) {
//                // TODO: 해당 클라이언트로 송출 스트림이 실패함(네트워크 끈김)
//            }
//        }
//    }

    static class ClientSession extends Thread {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;

        ClientSession(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            connect();
        }

        private void connect() {
            try {
                while (isConnect()) {
                    System.out.println(in.readUTF());
                }
            } catch (IOException cause) {
                // TODO: 채팅 중 연결이 끊기는 경우
            } finally {
//                disconnect();
            }
        }

        private boolean isConnect() {
            return this.in != null;
        }
//        private void disconnect() {
//            leaveChat(this);
//        }
    }

    private static class Sender extends Thread {
        private DataOutputStream out;

        private Sender(Socket socket) throws IOException {
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                sendMessage();
            } catch (IOException e) {
                // TODO
            }
        }

        private boolean isSendable() {
            return this.out != null;
        }

        private void sendMessage() throws IOException {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isSendable()) {
                    this.out.writeUTF("> " + scanner.nextLine());
                }
            }
        }
    }

    private static class Receiver extends Thread {
        private final DataInputStream in;

        private Receiver(Socket socket) throws IOException {
            this.in = new DataInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (isReceivable()) {
                receiveMessage();
            }
        }

        private boolean isReceivable() {
            return this.in != null;
        }

        private void receiveMessage() {
            try {
                System.out.println(in.readUTF());
            } catch (IOException e) {
                // TODO
            }
        }
    }

    static class NetCatClient {

        void connect(String optionOrHostname, int port) {
            try {
                Socket socket = new Socket(optionOrHostname, port);
                System.out.println("Connected to server " + optionOrHostname + ":" + port);
                Thread sender = new Sender(socket);
                Thread receiver = new Receiver(socket);

                sender.start();
                receiver.start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

