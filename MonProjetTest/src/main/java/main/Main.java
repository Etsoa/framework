
package main;

import itu.framework.annotations.MyController;
import java.lang.reflect.Method;
import itu.framework.annotations.MyURL;
import java.io.File;
import java.net.URL;

import models.Personne;
import itu.framework.util.ControllerScanner;

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

        try {
            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            File classesDir = new File(location.toURI());
            File directory = new File(classesDir, "");
            System.out.println("Location: " + location);
            System.out.println("ClassesDir: " + classesDir);
            System.out.println("Directory: " + directory);
            findClasses(directory, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tester le scanner de contrôleurs
        try {
            System.out.println("\n=== Test du Scanner de Contrôleurs ===");
            ControllerScanner.scanAndSaveControllers();
            System.out.println("Scan terminé !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                String newPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                findClasses(file, newPackage);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                System.out.println("Trying to load: " + className);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyController.class)) {
                    System.out.println("Class annotated with @MyController: " + clazz.getName());
                }
            }
        }
    }
}
