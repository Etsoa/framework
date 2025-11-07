package models;

import itu.framework.annotations.MyURL;

public class Personne {

    @MyURL(value="/personne")
    public String test(){
        return "Méthode test() de Personne exécutée !";
    }
}