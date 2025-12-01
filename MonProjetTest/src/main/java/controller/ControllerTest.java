package controller;

import itu.framework.annotations.MyController;
import itu.framework.annotations.MyURL;
import itu.framework.annotations.MyParam;
import itu.framework.model.ModelView;
import java.util.HashMap;

@MyController(value = "Test")
public class ControllerTest {

    @MyURL(value = "/admin", method = "GET")
    public ModelView showAdmin() {
        return new ModelView("admin/dashboard.jsp");
    }

    @MyURL(value = "/departement/{id}/{b}", method = "GET")
    public ModelView getDepartementById(Integer id, String b) {
        ModelView mv = new ModelView("departement.jsp");
        mv.addItem("id", id);
        mv.addItem("b", b);
        return mv;
    }

    @MyURL(value = "/form", method = "GET")
    public ModelView showForm() {
        return new ModelView("form.jsp");
    }

    @MyURL(value = "/form", method = "POST")
    public ModelView submitForm(HashMap<String, Object> params) {
        ModelView mv = new ModelView("form-result.jsp");
        mv.addItem("name", params.get("name"));
        mv.addItem("age", params.get("age"));
        return mv;
    }
}
