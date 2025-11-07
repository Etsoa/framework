package itu.framework.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import itu.framework.model.ModelView;
import itu.framework.util.ControllerScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("=== FrontServlet init START ===");
        Map<String, Method> urlMapping = ControllerScanner.scanControllers(getServletContext());
        System.out.println("URLs mappées: " + urlMapping.size());
        for (String url : urlMapping.keySet()) {
            Method m = urlMapping.get(url);
            System.out.println("  " + url + " -> " + m.getDeclaringClass().getSimpleName() + "." + m.getName());
        }
        getServletContext().setAttribute("urlMapping", urlMapping);
        System.out.println("=== FrontServlet init END ===");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        service(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("text/html");
        
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI.substring(contextPath.length());
        
        System.out.println("URL demandée: '" + url + "'");
        
        @SuppressWarnings("unchecked")
        Map<String, Method> urlMapping = (Map<String, Method>) getServletContext().getAttribute("urlMapping");
        
        System.out.println("urlMapping null? " + (urlMapping == null));
        if (urlMapping != null) {
            System.out.println("urlMapping size: " + urlMapping.size());
            System.out.println("Contains key '" + url + "'? " + urlMapping.containsKey(url));
        }
        
        if (urlMapping != null) {
            Method method = urlMapping.get(url);
            
            if (method != null) {
                try {
                    Class<?> controllerClass = method.getDeclaringClass();
                    Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                    Object result = method.invoke(controllerInstance);
                    
                    if (result instanceof ModelView) {
                        // Si c'est un ModelView, faire un RequestDispatcher vers la JSP
                        ModelView modelView = (ModelView) result;
                        String view = modelView.getView();
                        
                        // Récupérer le chemin de base des vues depuis web.xml
                        String viewPath = getServletContext().getInitParameter("viewPath");
                        if (viewPath == null || viewPath.isEmpty()) {
                            viewPath = "/";
                        }
                        
                        // Construire le chemin complet de la vue
                        String fullViewPath = viewPath + view;
                        System.out.println("Forward vers: " + fullViewPath);
                        req.getRequestDispatcher(fullViewPath).forward(req, resp);
                    } else if (result instanceof String) {
                        // Si c'est un String, l'afficher directement
                        resp.getWriter().write(result.toString());
                    } else if (result != null) {
                        // Autres types, afficher avec toString()
                        resp.getWriter().write(result.toString());
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.getWriter().write("<p style='color:red;'>Erreur : " + e.getMessage() + "</p>");
                }
            } else {
                resp.getWriter().write("<p>Aucun mapping trouvé pour cette URL: '" + url + "'</p>");
            }
        } else {
            resp.getWriter().write("<p>Erreur: urlMapping non initialisé</p>");
        }
    }
}
