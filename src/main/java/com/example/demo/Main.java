package com.example.demo;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.get("/", false, (req, res) -> {
            res.send("{Hi from server.}");
        });
        server.get("/home", false, (req, res) -> {
            res.send("{Hi from server home}");
        });
        server.get("/home/chandra", false, (req, res) -> {
            res.send("{Hi from server home chandra}");
        });
        server.post("/",false,(req,res)->{
            res.send("upload done");
        });
        server.listen(8080);
        /*String s = "/";
        String s1 = s.substring(0,s.lastIndexOf("/"));
        System.out.printf(""+s1);*/
    }
}
