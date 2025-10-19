package models;

import itu.framework.annotations.MyURL;

public class Personne {

    @MyURL(value="/test")
    public void test(){
        System.out.println("Ok");
    }
}