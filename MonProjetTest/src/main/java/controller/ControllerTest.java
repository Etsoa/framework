package controller;

import itu.framework.annotations.MyController;
import itu.framework.annotations.MyURL;

@MyController(value = "Test")
public class ControllerTest {
    
    @MyURL(value = "/test")
    public String test() {
        return "Méthode test() exécutée avec succès !";
    }
    
    @MyURL(value = "/hello")
    public String hello() {
        return "Hello World depuis le contrôleur !";
    }
}
