package itu.framework.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import itu.framework.annotations.MyController;
import itu.framework.annotations.MyURL;
import jakarta.servlet.ServletContext;

public class ControllerScanner {
    
    public static Map<String, Method> scanControllers(ServletContext servletContext) {
        Map<String, Method> urlMapping = new HashMap<>();
        
        System.out.println("=== Début du scan des contrôleurs ===");
        
        try {
            // Obtenir le chemin réel des classes depuis le ServletContext
            String classesPath = servletContext.getRealPath("/WEB-INF/classes");
            System.out.println("Classes path: " + classesPath);
            
            File classesDir = new File(classesPath);
            System.out.println("Existe: " + classesDir.exists());
            
            if (classesDir.exists() && classesDir.isDirectory()) {
                findClasses(classesDir, "", urlMapping);
            } else {
                System.out.println("ERREUR: Le répertoire des classes n'existe pas!");
            }
            
            System.out.println("Nombre d'URLs mappées: " + urlMapping.size());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return urlMapping;
    }

    private static void findClasses(File directory, String packageName, Map<String, Method> urlMapping) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                String newPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                findClasses(file, newPackage, urlMapping);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                System.out.println("Classe trouvée: " + className);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(MyController.class)) {
                        System.out.println("✓ Contrôleur: " + clazz.getSimpleName());
                        
                        // Scanner les méthodes de cette classe pour @MyURL
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.isAnnotationPresent(MyURL.class)) {
                                MyURL urlAnn = method.getAnnotation(MyURL.class);
                                String url = urlAnn.value();
                                urlMapping.put(url, method);
                                System.out.println("  ✓ Mapping: " + url + " -> " + method.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  ✗ Erreur: " + e.getMessage());
                }
            }
        }
    }
}