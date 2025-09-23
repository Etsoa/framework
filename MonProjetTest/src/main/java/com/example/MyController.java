package com.example;

import com.monframework.annotations.WebRoute;

public class MyController {

    @WebRoute("/hello")
    public String hello() {
        return "Bonjour depuis MyController avec le framework !";
    }

    @WebRoute("/postExample")
    public String postExample() {
        return "POST reçu et traité par le framework !";
    }
}
