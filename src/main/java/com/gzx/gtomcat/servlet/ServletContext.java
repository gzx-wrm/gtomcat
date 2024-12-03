package com.gzx.gtomcat.servlet;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

// 存储同一个app中映射对应路径的servlet
public class ServletContext {

    private HashMap<String, HttpServlet> urlServletMapping;

    public ServletContext() {
        urlServletMapping = new HashMap<>();
    }

    public void addMapping(String urlPattern, HttpServlet servlet) {
        urlServletMapping.put(urlPattern, servlet);
    }

    public HttpServlet getMapping(String urlPattern) {
        return urlServletMapping.get(urlPattern);
    }

    public Map<String, HttpServlet> getUrlServletMapping() {
        return urlServletMapping;
    }
}
