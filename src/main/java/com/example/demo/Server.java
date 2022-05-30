package com.example.demo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int PORT = 8080;
    private BufferedInputStream bufferedInputStream = null;
    private OutputStream outputStream = null;
    private Map<String,Object> GET_MAP = new HashMap<>();

    public Server(){

    }
    public void listen(int i) {
        PORT = i;
        init();
    }
    private void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("listening on :" + serverSocket.getInetAddress() + "/" + PORT);
            while (true) {
                try {
                    Socket socket;
                    socket = serverSocket.accept();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            ProcessRequest(socket);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void ProcessRequest(Socket socket) {
        try {
            System.out.println("Accepted connection..." + socket);
            InputStream inputStream = socket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String input;
            StringBuilder total= new StringBuilder();
            ArrayList<String> request = new ArrayList<>();
            while (!Objects.equals(input = in.readLine(), "")){
                if (input==null){
                    break;
                }
                request.add(input);
                total.append(input);
                System.out.println(""+input);
            }
            if (total.toString().isEmpty()){
                System.out.println("hand-shake");
                return;
            }
            String[] x = request.get(0).split(" ");
            String path = x[1];
            switch (x[0]){
                case "GET":
                    while (true){
                        Object o = GET_MAP.get(path);
                        if (o!=null){
                            Request req = new Request(total.toString());
                            Object[] oo = (Object[]) o;
                            if (!(boolean)oo[0]){
                                Response res = new Response(socket);
                                ((get)oo[1]).onGet(req,res);
                            }
                            else{
                                //todo code
                            }
                            break;
                        }
                        path = path.substring(0,path.lastIndexOf("/"));
                        if (path.isEmpty()){
                            path = "/";
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            System.out.printf(""+e);
        }
    }
    public void get(String path, boolean isAuth, get g) {
        Object[] ar = {isAuth, g};
        GET_MAP.put(path,ar);
    }
    interface get{
        void onGet(Request req, Response res);
    }

    public static class Request{
        Socket socket;
        String s;
        public Request(String s){
            this.s = s;
        }
    }
    public static class Response{
        private Socket socket;
        public Response(Socket socket){
            this.socket = socket;
        }
        public void send(Object o){
            String header = "HTTP/1.1 200 OK\n\n";
            String s = header+o;
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            try {
                socket.getOutputStream().write(bytes);
                System.out.println("Response : "+s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                try {
                    socket.close();
                    socket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
