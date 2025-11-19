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
        
        if (urlMapping == null) {
            resp.getWriter().write("<p>Erreur: urlMapping non initialisé</p>");
            return;
        }
        
        Method method = null;
        Map<String, String> pathParams = null;
        
        // Cherche un mapping exact
        method = urlMapping.get(url);
        
        // Si pas trouvé, cherche les URLs paramétrées
        if (method == null) {
            for (String pattern : urlMapping.keySet()) {
                if (pattern.contains("{") && pattern.contains("}")) {
                    pathParams = matchParameterizedUrl(pattern, url);
                    if (pathParams != null) {
                        method = urlMapping.get(pattern);
                        break;
                    }
                }
            }
        }
        
        if (method == null) {
            resp.getWriter().write("<p>Aucun mapping trouvé pour cette URL</p>");
            return;
        }
        
        try {
            Class<?> controllerClass = method.getDeclaringClass();
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            
            java.lang.reflect.Parameter[] methodParams = method.getParameters();
            Object[] args = new Object[methodParams.length];
            
            for (int i = 0; i < methodParams.length; i++) {
                java.lang.reflect.Parameter param = methodParams[i];
                String value = null;
                
                // Vérifie @MyParam
                if (param.isAnnotationPresent(itu.framework.annotations.MyParam.class)) {
                    String paramName = param.getAnnotation(itu.framework.annotations.MyParam.class).value();
                    value = req.getParameter(paramName);
                }
                
                // Vérifie les paramètres de l'URL si pas trouvé
                if (value == null && pathParams != null && pathParams.containsKey(param.getName())) {
                    value = pathParams.get(param.getName());
                }
                
                // Conversion selon le type
                Class<?> paramType = param.getType();
                if (value != null) {
                    if (paramType == Integer.class || paramType == int.class) {
                        args[i] = Integer.parseInt(value);
                    } else if (paramType == Long.class || paramType == long.class) {
                        args[i] = Long.parseLong(value);
                    } else if (paramType == Double.class || paramType == double.class) {
                        args[i] = Double.parseDouble(value);
                    } else if (paramType == Boolean.class || paramType == boolean.class) {
                        args[i] = Boolean.parseBoolean(value);
                    } else {
                        args[i] = value;
                    }
                } else {
                    args[i] = null; // valeur absente
                }
            }
            
            // Invocation de la méthode
            Object result = method.invoke(controllerInstance, args);
            
            if (result instanceof ModelView) {
                ModelView mv = (ModelView) result;
                
                // Ajout des données comme attributs de requête
                for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                
                String viewPath = getServletContext().getInitParameter("viewPath");
                if (viewPath == null || viewPath.isEmpty()) {
                    viewPath = "/";
                }
                req.getRequestDispatcher(viewPath + mv.getView()).forward(req, resp);
            } else if (result != null) {
                resp.getWriter().write(result.toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("<p style='color:red;'>Erreur : " + e.getMessage() + "</p>");
        }
    }

    
    /**
     * Match a parameterized URL pattern against a request URL
     * @param pattern The pattern like "/departement/{id}"
     * @param url The actual URL like "/departement/123"
     * @return Map of parameter names to values, or null if no match
     */
    private Map<String, String> matchParameterizedUrl(String pattern, String url) {
        String[] patternParts = pattern.split("/");
        String[] urlParts = url.split("/");
        
        if (patternParts.length != urlParts.length) {
            return null;
        }
        
        Map<String, String> params = new java.util.HashMap<>();
        
        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String urlPart = urlParts[i];
            
            if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                // This is a parameter
                String paramName = patternPart.substring(1, patternPart.length() - 1);
                params.put(paramName, urlPart);
            } else if (!patternPart.equals(urlPart)) {
                // Static parts don't match
                return null;
            }
        }
        
        return params;
    }
}
