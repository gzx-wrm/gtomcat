package com.gzx.gtomcat;


import com.gzx.gtomcat.processor.SocketProcessor;
import com.gzx.gtomcat.servlet.ServletContext;
import org.omg.CORBA.PUBLIC_MEMBER;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Tomcat {

    private HttpServlet dispatcherServlet;

    private static HashMap<String, ServletContext> appMapping = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // 需要做的事情：
        // 1. 使用线程池处理socket请求
        // 2. 解析servlet类进行请求处理
        loadApps();
        ExecutorService threadPool = new ThreadPoolExecutor(10,
                20,
                5,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>());

        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            threadPool.execute(new SocketProcessor(socket, appMapping));
        }


    }

    public Tomcat() {
    }

    public void start() throws IOException {
        ExecutorService threadPool = new ThreadPoolExecutor(10,
                20,
                5,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>());

        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            threadPool.execute(new SocketProcessor(socket, appMapping));
        }
    }

    public void addServlet(String path, HttpServlet servlet) {
        if (!appMapping.containsKey("/")) {
            ServletContext defaultApp = new ServletContext();
            appMapping.put("/", defaultApp);
        }
        ServletContext defaultApp = appMapping.get("/");
        defaultApp.addMapping(path, servlet);
    }

    public static void loadApps() {
        String property = System.getProperty("user.dir");
        File appsDir = new File(property + "/webapps");

        for (File file : appsDir.listFiles()) {
            if (file.isDirectory()) {
                loadApp(appsDir.getPath(), file.getName());
            }
        }
    }

    private static void loadApp(String appPath, String appName) {
        // 列出文件夹下的所有文件
        List<String> files = listFiles(appPath + "/" + appName);

        // 找到类路径
        String classPath = appPath + "/" + appName + "/classes";
        // 创建类加载器加载.class文件
        TomcatClassLoader tomcatClassLoader;
        try {
            URL classPathUrl = new File(classPath).toURI().toURL();
            tomcatClassLoader = new TomcatClassLoader(new URL[]{classPathUrl});
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        for (String file : files) {
            if (file.startsWith(classPath) && file.endsWith(".class")) {
                String className = file.substring(classPath.length() + 1, file.length() - 6).replace("/", ".");
                Class<?> clazz = null;
                try {
                    clazz = tomcatClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
//                    System.out.println(clazz);
                // 如果该类是servlet的话，就把它放到容器中
                if (!HttpServlet.class.isAssignableFrom(clazz)) {
                    continue;
                }
                if (!clazz.isAnnotationPresent(WebServlet.class)) {
                    continue;
                }
                handleServlet(appName, clazz);
            }
        }
//        System.out.println(appMapping);

    }

    private static void handleServlet(String appName, Class clazz) {
        WebServlet webServlet = (WebServlet) clazz.getAnnotation(WebServlet.class);
        ServletContext servletContext = appMapping.getOrDefault(appName, new ServletContext());
        Arrays.stream(webServlet.urlPatterns()).forEach(url -> {
            try {
                servletContext.addMapping(url, (HttpServlet) clazz.newInstance());
            } catch (InstantiationException e) {
                System.out.println(e.getMessage());
            } catch (IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
        });
        appMapping.put(appName, servletContext);
    }

    /**
     * 递归地列出所有文件
     * @param path
     * @return
     */
    private static List<String> listFiles(String path) {
        File[] files = new File(path).listFiles();
        LinkedList<String> res = new LinkedList<>();
        if (files == null) {
            return res;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                res.addAll(listFiles(file.getPath()));
            }
            else if (file.isFile()) {
                res.add(file.getPath());
            }
        }
        return res;
    }
}
