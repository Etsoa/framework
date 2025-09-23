package com.example;

import com.monframework.Framework;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    private final MyController controller = new MyController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String responseText = Framework.handleRequest(controller, "/hello");

        out.println("<html><body>");
        out.println("<h1>Servlet GET</h1>");
        out.println("<p>" + responseText + "</p>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String responseText = Framework.handleRequest(controller, "/postExample");

        out.println("<html><body>");
        out.println("<h1>Servlet POST</h1>");
        out.println("<p>" + responseText + "</p>");
        out.println("</body></html>");
    }
}
