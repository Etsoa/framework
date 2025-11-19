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
        Map<String, Method> urlMapping = ControllerScanner.scanControllers(getServletContext());
        getServletContext().setAttribute("urlMapping", urlMapping);
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
        
        @SuppressWarnings("unchecked")
        Map<String, Method> urlMapping = (Map<String, Method>) getServletContext().getAttribute("urlMapping");
        
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
                        
                        // Ajouter les données du ModelView comme attributs de requête
                        for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }
                        
                        // Récupérer le chemin de base des vues depuis web.xml
                        String viewPath = getServletContext().getInitParameter("viewPath");
                        if (viewPath == null || viewPath.isEmpty()) {
                            viewPath = "/";
                        }
                        
                        // Construire le chemin complet de la vue
                        String fullViewPath = viewPath + view;
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
                resp.getWriter().write("<p>Aucun mapping trouvé pour cette URL</p>");
            }
        } else {
            resp.getWriter().write("<p>Erreur: urlMapping non initialisé</p>");
        }
    }
}
