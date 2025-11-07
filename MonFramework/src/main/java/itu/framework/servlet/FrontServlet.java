package itu.framework.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import itu.framework.util.ControllerScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("=== FrontServlet init() called ===");
        
        // Scanner les contrôleurs et obtenir la Map
        Map<String, Method> urlMapping = ControllerScanner.scanControllers(getServletContext());
        
        // Sauvegarder la Map dans le ServletContext
        getServletContext().setAttribute("urlMapping", urlMapping);
        
        System.out.println("=== FrontServlet init completed ===");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("=== SERVLET CALLED ===");
        resp.setContentType("text/html");
        
        // Récupérer l'URL demandée (sans le context path)
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI.substring(contextPath.length());
        
        System.out.println("URL demandée: " + url);
        
        // Récupérer la Map depuis le ServletContext
        @SuppressWarnings("unchecked")
        Map<String, Method> urlMapping = (Map<String, Method>) getServletContext().getAttribute("urlMapping");
        
        if (urlMapping != null) {
            Method method = urlMapping.get(url);
            
            if (method != null) {
                // Récupérer la classe du contrôleur
                Class<?> controllerClass = method.getDeclaringClass();
                
                // Afficher uniquement le contrôleur et la méthode
                resp.getWriter().write("<p>Contrôleur : " + controllerClass.getSimpleName() + "</p>");
                resp.getWriter().write("<p>Méthode : " + method.getName() + "</p>");
            } else {
                // Aucun mapping trouvé
                resp.getWriter().write("<p>Aucun mapping trouvé pour cette URL</p>");
            }
        } else {
            resp.getWriter().write("<p>Erreur: urlMapping non initialisé</p>");
        }
    }
}
