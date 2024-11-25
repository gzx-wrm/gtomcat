package com.gzx.gtomcat.servlet;

import javax.servlet.WriteListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ServletOutputStream extends javax.servlet.ServletOutputStream {

    private List<byte[]> bufferList;

    private int pos;

    private int contentLength;

    public ServletOutputStream() {
        this.bufferList = new LinkedList<>();
        bufferList.add(new byte[4096]);
        this.pos = 0;
        this.contentLength = 0;
    }

    @Override
    public void write(int b) throws IOException {
        contentLength += 1;
        byte[] buffer = this.bufferList.get(this.bufferList.size() - 1);
        buffer[pos++] = (byte) b;
        if (pos == buffer.length) {
            // 缓冲区满了就需要将缓冲区扩容
            buffer = new byte[4096];
            this.bufferList.add(buffer);
            pos = 0;
        }
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    public List<byte[]> getBufferList() {
        return bufferList;
    }

    public int getPos() {
        return pos;
    }

    public int getContentLength() {
        return contentLength;
    }
}
