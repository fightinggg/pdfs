package com.pdfs.normalfs;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class PdfsFileInputStream extends InputStream {
    private long size;
    private final InputStream inputStream;

    public static PdfsFileInputStream fromBytes(byte[] s) {
        return new PdfsFileInputStream(s.length, new ByteArrayInputStream(s));
    }

    public PdfsFileInputStream(long size, InputStream inputStream) {
        this.size = size;
        this.inputStream = inputStream;
    }

    public long getRemainSize() {
        return size;
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        if (read == -1) {
            if (size != 0) {
                throw new RuntimeException();
            }
        } else {
            size = size - 1;
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
        size = 0;
    }
}
