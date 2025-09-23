package com.monframework;

import com.monframework.annotations.WebRoute;
import java.lang.reflect.Method;

public class Framework {

    // Simule un routeur
    public static String handleRequest(Object controller, String path) {
        try {
            for (Method method : controller.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(WebRoute.class)) {
                    WebRoute route = method.getAnnotation(WebRoute.class);
                    if (route.value().equals(path)) {
                        Object result = method.invoke(controller);
                        return result.toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "500 Internal Server Error";
        }
        return "404 Not Found";
    }
}
