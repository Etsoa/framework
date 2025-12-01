package itu.framework.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import itu.framework.model.ModelView;
import itu.framework.util.ControllerScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    private static class MethodMatch {
        final Method method;
        final Map<String, String> pathParams;

        MethodMatch(Method method, Map<String, String> pathParams) {
            this.method = method;
            this.pathParams = pathParams;
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Map<String, Map<String, Method>> urlMapping = ControllerScanner.scanControllers(getServletContext());
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

        String url = extractUrlPath(req);
        Map<String, Map<String, Method>> urlMapping = getUrlMapping(req);

        if (urlMapping == null) {
            resp.getWriter().write("<p>Erreur: urlMapping non initialisé</p>");
            return;
        }

        String httpMethod = req.getMethod().toUpperCase();
        MethodMatch methodMatch = findMethod(url, httpMethod, urlMapping);

        if (methodMatch == null) {
            resp.getWriter().write("<p>Aucun mapping trouvé pour cette URL</p>");
            return;
        }

        try {
            Object result = invokeControllerMethod(req, methodMatch, httpMethod);
            handleResult(req, resp, result);
        } catch (Exception e) {
            handleError(resp, e);
        }
    }

    private String extractUrlPath(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        return requestURI.substring(contextPath.length());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Method>> getUrlMapping(HttpServletRequest req) {
        return (Map<String, Map<String, Method>>) req.getServletContext().getAttribute("urlMapping");
    }

    private MethodMatch findMethod(String url, String httpMethod, Map<String, Map<String, Method>> urlMapping) {
        // Cherche un mapping exact
        Map<String, Method> methodsForUrl = urlMapping.get(url);
        if (methodsForUrl != null) {
            Method method = methodsForUrl.get(httpMethod);
            if (method != null) {
                return new MethodMatch(method, null);
            }
        }

        // Cherche les URLs paramétrées
        for (String pattern : urlMapping.keySet()) {
            if (pattern.contains("{") && pattern.contains("}")) {
                Map<String, String> pathParams = matchParameterizedUrl(pattern, url);
                if (pathParams != null) {
                    Map<String, Method> methodsForPattern = urlMapping.get(pattern);
                    if (methodsForPattern != null) {
                        Method method = methodsForPattern.get(httpMethod);
                        if (method != null) {
                            return new MethodMatch(method, pathParams);
                        }
                    }
                }
            }
        }

        return null;
    }

    private Object invokeControllerMethod(HttpServletRequest req, MethodMatch methodMatch, String httpMethod)
            throws Exception {
        Method method = methodMatch.method;
        Class<?> controllerClass = method.getDeclaringClass();
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

        Object[] args = prepareMethodArguments(req, method, methodMatch.pathParams, httpMethod);
        return method.invoke(controllerInstance, args);
    }

    private Object[] prepareMethodArguments(HttpServletRequest req, Method method,
            Map<String, String> pathParams, String httpMethod) {

        java.lang.reflect.Parameter[] methodParams = method.getParameters();

        if ("POST".equals(httpMethod) && methodParams.length == 1) {
            java.lang.reflect.Parameter param = methodParams[0];

            // Vérifie que c'est exactement HashMap<String, Object>
            if (param.getType() == HashMap.class) {
                java.lang.reflect.Type genericType = param.getParameterizedType();
                if (genericType instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) genericType;
                    java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();

                    // Vérifie les types génériques: HashMap<String, Object>
                    if (typeArgs.length == 2
                            && typeArgs[0] == String.class
                            && typeArgs[1] == Object.class) {
                        return new Object[] { buildParameterMap(req) };
                    }
                }
            }
        }

        return buildArgumentsFromParameters(req, methodParams, pathParams);
    }

    private HashMap<String, Object> buildParameterMap(HttpServletRequest req) {
        HashMap<String, Object> params = new HashMap<>();
        java.util.Enumeration<String> paramNames = req.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = req.getParameter(paramName);
            params.put(paramName, paramValue);
        }

        return params;
    }

    private Object[] buildArgumentsFromParameters(HttpServletRequest req,
            java.lang.reflect.Parameter[] methodParams, Map<String, String> pathParams) {

        Object[] args = new Object[methodParams.length];

        for (int i = 0; i < methodParams.length; i++) {
            java.lang.reflect.Parameter param = methodParams[i];
            String value = getParameterValue(req, param, pathParams);
            args[i] = convertParameterValue(value, param.getType());
        }

        return args;
    }

    private String getParameterValue(HttpServletRequest req, java.lang.reflect.Parameter param,
            Map<String, String> pathParams) {

        // Vérifie @MyParam
        if (param.isAnnotationPresent(itu.framework.annotations.MyParam.class)) {
            String paramName = param.getAnnotation(itu.framework.annotations.MyParam.class).value();
            return req.getParameter(paramName);
        }

        // Vérifie les paramètres de l'URL
        if (pathParams != null && pathParams.containsKey(param.getName())) {
            return pathParams.get(param.getName());
        }

        // Vérifie les paramètres de la requête
        return req.getParameter(param.getName());
    }

    private Object convertParameterValue(String value, Class<?> paramType) {
        if (value == null) {
            return null;
        }

        if (paramType == Integer.class || paramType == int.class) {
            return Integer.parseInt(value);
        } else if (paramType == Long.class || paramType == long.class) {
            return Long.parseLong(value);
        } else if (paramType == Double.class || paramType == double.class) {
            return Double.parseDouble(value);
        } else if (paramType == Boolean.class || paramType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            return value;
        }
    }

    private void handleResult(HttpServletRequest req, HttpServletResponse resp, Object result)
            throws ServletException, IOException {

        if (result instanceof ModelView) {
            handleModelViewResult(req, resp, (ModelView) result);
        } else if (result != null) {
            resp.getWriter().write(result.toString());
        }
    }

    private void handleModelViewResult(HttpServletRequest req, HttpServletResponse resp, ModelView mv)
            throws ServletException, IOException {

        // Ajout des données comme attributs de requête
        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }

        String viewPath = getServletContext().getInitParameter("viewPath");
        if (viewPath == null || viewPath.isEmpty()) {
            viewPath = "/";
        }

        req.getRequestDispatcher(viewPath + mv.getView()).forward(req, resp);
    }

    private void handleError(HttpServletResponse resp, Exception e) throws IOException {
        e.printStackTrace();
        resp.getWriter().write("<p style='color:red;'>Erreur : " + e.getMessage() + "</p>");
    }

    /**
     * Match a parameterized URL pattern against a request URL
     * 
     * @param pattern The pattern like "/departement/{id}"
     * @param url     The actual URL like "/departement/123"
     * @return Map of parameter names to values, or null if no match
     */
    private Map<String, String> matchParameterizedUrl(String pattern, String url) {
        String[] patternParts = pattern.split("/");
        String[] urlParts = url.split("/");

        if (patternParts.length != urlParts.length) {
            return null;
        }

        Map<String, String> params = new HashMap<>();

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
