package com.gzx.gtomcat.processor;

import com.gzx.gtomcat.servlet.DefaultServlet;
import com.gzx.gtomcat.servlet.ServletContext;
import com.gzx.gtomcat.servlet.ServletRequest;
import com.gzx.gtomcat.servlet.ServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理socket事件
 */
public class SocketProcessor implements Runnable{

    private Socket socket;

    private static HashMap<String, ServletContext> appMapping;

    private byte[] requestBytes;

    private int requestLength;

    private static final Pattern APP_PATTERN = Pattern.compile("^/([^/]+)/?");

    public SocketProcessor(Socket socket, HashMap<String, ServletContext> appMapping) {
        this.socket = socket;
        this.appMapping = appMapping;
    }

    @Override
    public void run() {
        try {
            process(socket);
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(Socket socket) throws IOException, ServletException {
//        System.out.println(Thread.currentThread().getName());
        requestBytes = new byte[10240];
        InputStream inputStream = socket.getInputStream();
        int byteNum = inputStream.read(requestBytes);
        requestLength = byteNum;
        System.out.println("request byte num: " + byteNum);

        if (byteNum == -1) {
            socket.close();
            return;
        }
        ServletRequest servletRequest = parseHttpServletRequest();
//        servletRequest.getHeaders().forEach((k, v) -> {
//            System.out.println(k + ": " + v);
//        });

        ServletResponse servletResponse = new ServletResponse(socket, servletRequest);
        // 选择合适的servlet去处理这个请求
//        Matcher matcher = APP_PATTERN.matcher(servletRequest.getRequestURI());
//        if (matcher.find()) {
//            String appName = matcher.group(1);
//            System.out.println(appName);
//        }
        String uri = servletRequest.getRequestURI();
        for (String appContext : appMapping.keySet()) {
            if (!uri.startsWith(appContext)) {
                continue;
            }
            uri = uri.substring(appContext.length());
            uri = uri.startsWith("/") ? uri : "/" + uri;
            ServletContext servletContext = appMapping.get(appContext);
            for (String servletPath : servletContext.getUrlServletMapping().keySet()) {
                if (!uri.startsWith(servletPath)) {
                    continue;
                }
                HttpServlet servlet = servletContext.getMapping(servletPath);
                uri = uri.substring(servletPath.length());
                uri = uri.startsWith("/") ? uri : "/" + uri;
                servletRequest.setRequestURI(uri);
                servlet.service(servletRequest, servletResponse);
                servletResponse.complete();
                return;
            }
        }
        new DefaultServlet().service(servletRequest, servletResponse);
        servletResponse.complete();
//        ServletContext servletContext = appMapping.get("/" + servletRequest.getRequestURI());
//        if (servletContext == null) {
//            new DefaultServlet().service(servletRequest, servletResponse);
//        }
//        servletContext.getMapping("/" + servletRequest.getRequestURI())
//        socket.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Type: text/html;charset=utf-8\r\nContent-Length: 1\r\n\r\nh".getBytes());
    }

    /**
     * 按照Http请求的格式解析socket收到的字节
     * @return
     */
    private ServletRequest parseHttpServletRequest() {
        int start = 0, pos = 0;
        while (requestBytes[pos] != ' ') {
            pos++;
        }

        byte[] methodBytes = Arrays.copyOfRange(requestBytes, start, pos);
        String method = new String(methodBytes);
//        System.out.println(method);

        start = pos + 1;
        pos = start;
        while (requestBytes[pos] != ' ') {
            pos++;
        }
        byte[] urlBytes = Arrays.copyOfRange(requestBytes, start, pos);
        String url = new String(urlBytes);

        start = pos + 1;
        pos = start;
        while (requestBytes[pos] != '\r' && requestBytes[pos + 1] != '\n') {
            pos++;
        }
        byte[] protocolBytes = Arrays.copyOfRange(requestBytes, start, pos);
        String protocol = new String(protocolBytes);

        start = pos + 2;
        pos = start;
        HashMap<String, String> headers = new HashMap<>();
        // todo: 处理异常情况的逻辑
        while (requestBytes[start] != '\r' && requestBytes[start + 1] != '\n') {
            while (requestBytes[pos] != ':') {
                pos++;
            }
            int delimiter = pos;
            pos += 2;
            while (requestBytes[pos] != '\r' && requestBytes[pos + 1] != '\n') {
                pos++;
            }
            String key = new String(Arrays.copyOfRange(requestBytes, start, delimiter));
            String value = new String(Arrays.copyOfRange(requestBytes, delimiter + 2, pos));

            headers.put(key, value);
            start = pos + 2;
            pos = start;
        }
        start = start + 2;
        byte[] body;
        if (start >= requestLength) {
            body = null;
        } else {
            body = Arrays.copyOfRange(requestBytes, start, requestLength - 1);
        }

        return new ServletRequest(method, url, protocol, headers, body);
    }
}
