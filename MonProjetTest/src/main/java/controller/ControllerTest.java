package controller;

import itu.framework.annotations.MyController;
import itu.framework.annotations.MyURL;
import itu.framework.model.ModelView;

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

    @MyURL(value = "/page")
    public ModelView showPage() {
        return new ModelView("index.jsp");  
    }

    @MyURL(value = "/admin")
    public ModelView showAdmin() {
        return new ModelView("admin/dashboard.jsp");  
    }
}
