
package main;


import java.lang.reflect.Method;
import itu.framework.annotations.MyURL;

import models.Personne;

public class Main {
    public static void main(String[] args) {
        Method[] methods = Personne.class.getDeclaredMethods();
        for (Method m : methods) {
            System.out.println("Method: " + m.getName());
            if (m.isAnnotationPresent(MyURL.class)) {
                MyURL ann = m.getAnnotation(MyURL.class);
                System.out.println("  @MyURL value = " + ann.value());
            }
        }
    }
}
