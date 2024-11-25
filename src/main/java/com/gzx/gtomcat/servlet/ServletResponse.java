package com.gzx.gtomcat.servlet;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServletResponse extends AbstractServletResponse {

    private Socket socket;

    private Map<String, String> headers;

    private ServletRequest request;

    private com.gzx.gtomcat.servlet.ServletOutputStream servletOutputStream;

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8";


    public ServletResponse(Socket socket, ServletRequest request) {
        this.socket = socket;
        this.request = request;
        this.servletOutputStream = new com.gzx.gtomcat.servlet.ServletOutputStream();
        this.headers = new HashMap<String, String>();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutputStream;
    }

    public void complete() throws IOException {
        // 如果没有设置过content length就判断响应体的长度自动设置
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", DEFAULT_CONTENT_TYPE);
        }
        if (!headers.containsKey("Content-Length")) {
            headers.put("Content-Length", String.valueOf(servletOutputStream.getContentLength()));
        }

        // 写状态行
        writeStatusLine();
        // 写响应头
        writeResponseHeader();
        // 写响应体
        writeResponseBody();
    }

    private void writeResponseBody() throws IOException {
        for (byte[] buffer : servletOutputStream.getBufferList()) {
            this.socket.getOutputStream().write(buffer);
        }
    }

    private void writeResponseHeader() throws IOException {
        StringBuilder headerBuilder = new StringBuilder();
        headers.forEach((k, v) -> {
            headerBuilder.append(k).append(": ").append(v).append("\r\n");
        });
        headerBuilder.append("\r\n");

        this.socket.getOutputStream().write(headerBuilder.toString().getBytes());
    }

    private void writeStatusLine() throws IOException {
        StringBuilder lineBuilder = new StringBuilder();
        lineBuilder.append(request.getProtocol());
        lineBuilder.append(" ");
        lineBuilder.append(SC_OK);
        lineBuilder.append(" ");
        lineBuilder.append("OK");
        lineBuilder.append("\r\n");
        this.socket.getOutputStream().write(lineBuilder.toString().getBytes());
    }

    @Override
    public void addHeader(String s, String s1) {
        this.headers.put(s, s1);
    }
}
