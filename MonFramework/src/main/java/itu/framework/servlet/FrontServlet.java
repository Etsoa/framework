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
        resp.setContentType("text/html;charset=UTF-8");

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI.substring(contextPath.length());

        @SuppressWarnings("unchecked")
        Map<String, Method> urlMapping = (Map<String, Method>) getServletContext().getAttribute("urlMapping");

        if (urlMapping != null) {
            Method method = null;
            Map<String, String> pathParams = null;

            // Chercher correspondance exacte
            method = urlMapping.get(url);

            // Si pas de correspondance exacte, chercher URLs paramétrées
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

            if (method != null) {
                try {
                    Class<?> controllerClass = method.getDeclaringClass();
                    Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                    // Préparer les arguments de la méthode
                    java.lang.reflect.Parameter[] methodParams = method.getParameters();
                    Object[] args = new Object[methodParams.length];

                    for (int i = 0; i < methodParams.length; i++) {
                        java.lang.reflect.Parameter param = methodParams[i];
                        String paramName = param.getName(); // vrai nom grâce à parameters=true
                        Class<?> paramType = param.getType();
                        String value = null;

                        // D'abord vérifier paramètres dans URL
                        if (pathParams != null && pathParams.containsKey(paramName)) {
                            value = pathParams.get(paramName);
                        }

                        // Sinon, vérifier paramètres de requête
                        if (value == null) {
                            value = req.getParameter(paramName);
                        }

                        // Conversion en type approprié
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
                                args[i] = value; // String par défaut
                            }
                        } else {
                            args[i] = null; // Valeur nulle si non fournie
                        }
                    }

                    // Invocation de la méthode
                    Object result = method.invoke(controllerInstance, args);

                    // Gérer le résultat
                    if (result instanceof ModelView mv) {
                        String view = mv.getView();
                        // Ajouter les données du ModelView dans la requête
                        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }

                        String viewPath = getServletContext().getInitParameter("viewPath");
                        if (viewPath == null || viewPath.isEmpty()) viewPath = "/";

                        req.getRequestDispatcher(viewPath + view).forward(req, resp);

                    } else if (result instanceof String str) {
                        resp.getWriter().write(str);
                    } else if (result != null) {
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
            resp.getWriter().write("<p>Erreur : urlMapping non initialisé</p>");
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
