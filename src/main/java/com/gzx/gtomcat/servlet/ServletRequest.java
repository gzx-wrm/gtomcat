package com.gzx.gtomcat.servlet;


import java.util.HashMap;
import java.util.Map;

public class ServletRequest extends AbstractServletRequest{

    private String method;

    private String uri;

    private String protocol;

    private Map<String, String> headers;

    private byte[] body;

    public ServletRequest(String method, String uri, String protocol, Map<String, String> headers, byte[] body) {
        this.method = method;
        this.uri = uri;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
