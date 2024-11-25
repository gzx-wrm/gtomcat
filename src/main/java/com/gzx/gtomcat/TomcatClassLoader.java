package com.gzx.gtomcat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class TomcatClassLoader extends URLClassLoader {

    public TomcatClassLoader(URL[] urls) {
        super(urls);
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        URL url = new File("/Users/bytedance/gzx/java_project/tomcat/webapps/hello/classes").toURI().toURL();
        TomcatClassLoader tomcatClassLoader = new TomcatClassLoader(new URL[]{url});
        Class<?> clazz = tomcatClassLoader.loadClass("com.gzx.gtomcat.servlet.TestServlet");
        System.out.println(clazz);
    }
}
