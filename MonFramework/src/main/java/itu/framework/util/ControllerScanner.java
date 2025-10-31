package itu.framework.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import itu.framework.annotations.MyController;

public class ControllerScanner {
    
    public static void scanAndSaveControllers() {
        List<String> controllerInfo = new ArrayList<>();
        
        try {
            // Scanner directement le répertoire target/classes du projet courant
            // On suppose que le projet suit la structure Maven standard
            String userDir = System.getProperty("user.dir");
            File classesDir = new File(userDir, "target/classes");
            
            if (classesDir.exists() && classesDir.isDirectory()) {
                findClasses(classesDir, "", controllerInfo);
            }
            
            // Sauvegarder dans un fichier
            saveToFile(controllerInfo);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findClasses(File directory, String packageName, List<String> controllerInfo) {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                String newPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                findClasses(file, newPackage, controllerInfo);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(MyController.class)) {
                        MyController controllerAnn = clazz.getAnnotation(MyController.class);
                        String controllerLine = "Controller trouvé: " + clazz.getName() + " (value: " + controllerAnn.value() + ")";
                        controllerInfo.add(controllerLine);
                    }
                } catch (Exception e) {
                    // Ignorer les classes qui ne peuvent pas être chargées
                }
            }
        }
    }

    private static void saveToFile(List<String> controllerInfo) {
        try {
            // Créer le répertoire s'il n'existe pas
            File outputDir = new File("controller-mapping");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Sauvegarder dans un fichier
            File outputFile = new File(outputDir, "controllers.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                writer.println("=== MAPPING DES CONTROLEURS ===");
                writer.println("Date: " + new java.util.Date());
                writer.println();
                
                for (String line : controllerInfo) {
                    writer.println(line);
                }
                
                writer.println();
                writer.println("=== FIN DU MAPPING ===");
            }
            
            System.out.println("Mapping des contrôleurs sauvegardé dans: " + outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}