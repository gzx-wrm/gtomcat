package com.gzx.gtomcat.servlet;

import com.google.gson.Gson;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@WebServlet(urlPatterns = "/hello")
public class TestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("test doGet");
        resp.setContentType("text/html; charset=utf-8");

        HashMap<String, String> jsonBody = new HashMap<>();
        jsonBody.put("name", "gzx");
        jsonBody.put("age", "18");
        jsonBody.put("gender", "male");
        resp.getOutputStream().write(new Gson().toJson(jsonBody).getBytes());
    }
}
