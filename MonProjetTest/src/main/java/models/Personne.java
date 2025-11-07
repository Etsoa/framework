package models;

import itu.framework.annotations.MyController;
import itu.framework.annotations.MyURL;

@MyController(value = "Personne")
public class Personne {

    @MyURL(value="/personne")
    public String test(){
        return "Méthode test() de Personne exécutée !";
    }
}