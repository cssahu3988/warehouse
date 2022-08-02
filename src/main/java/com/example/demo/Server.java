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
    private Map<String,Object> POST_MAP = new HashMap<>();
    public Server(){

    }
    public void listen(int i) {
        PORT = i;
        init();
    }
    private void init() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("listening on :" + serverSocket.getInetAddress() + "/" + PORT);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            ProcessRequest(socket);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("\n");
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
            ArrayList<String> content = new ArrayList<>();
            List<StringBuilder> content_ = new ArrayList<>();
            String boundry = "";
            String boundry_end = "";
            int contentType_count = 0;
            boolean content_present = true;

            while (content_present){
                if ((input = in.readLine())!=null){
                    request.add(input);
                    total.append(input);
                    System.out.println(""+input);
                    if (!boundry.equals("") && input.contains(boundry)){
                        String data = "";
                        boolean data_present = true;
                        StringBuilder stringBuilder=new StringBuilder();
                        while (data_present){
                            data = in.readLine();
                            content.add(data);
                            System.out.println(data);
                            if (data.contains(boundry)){
                                content_.add(stringBuilder);
                                stringBuilder = new StringBuilder();
                            }
                            stringBuilder.append(data);
                            if (data.contains(boundry_end)){
                                data_present = false;
                                content_present = false;
                            }
                        }
                    }
                    if (input.contains("Content-Type") && contentType_count<1){
                        contentType_count++;
                        String[] b = input.split("boundary=");
                        if (b.length>1 && b[1]!=null){
                            boundry = b[1];
                            boundry_end = boundry+"--";
                        }
                        else {

                        }
                    }
                }


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
                case "POST":
                    while (true){
                        Object o = POST_MAP.get(path);
                        if (o!=null){
                            Request req = new Request(total.toString());
                            Object[] oo = (Object[]) o;
                            if (!(boolean)oo[0]){
                                Response res = new Response(socket);
                                ((post)oo[1]).onPost(req,res);
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
    public static StringBuilder getString(InputStream in) throws IOException {
        int length = 1024*10;
        byte[] messageByte = new byte[length];
        boolean end = false;
        StringBuilder dataString = new StringBuilder(length);
        int totalBytesRead = 0;
        while(!end) {
            int currentBytesRead = in.read(messageByte);
            totalBytesRead = currentBytesRead + totalBytesRead;
            if(totalBytesRead <= length) {
                dataString
                        .append(new String(messageByte, 0, currentBytesRead, StandardCharsets.UTF_8));
            } else {
                dataString
                        .append(new String(messageByte, 0, length - totalBytesRead + currentBytesRead,
                                StandardCharsets.UTF_8));
            }
            if(dataString.length()>=length) {
                end = true;
            }
        }
        return dataString;
    }
    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }
    public void get(String path, boolean isAuth, get g) {
        Object[] ar = {isAuth, g};
        GET_MAP.put(path,ar);
    }
    interface get{
        void onGet(Request req, Response res);
    }
    public void post(String path, boolean isAuth, post p) {
        Object[] ar = {isAuth, p};
        POST_MAP.put(path,ar);
    }
    interface post{
        void onPost(Request req, Response res);
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
