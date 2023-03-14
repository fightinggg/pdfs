package com.pdfs.normalfs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfsFileInputStream extends InputStream {
    private final long size;
    private final InputStream inputStream;

    public static PdfsFileInputStream fromBytes(byte[] s) {
        return new PdfsFileInputStream(s.length, new ByteArrayInputStream(s));
    }

    public PdfsFileInputStream(long size, InputStream inputStream) {
        this.size = size;
        this.inputStream = inputStream;
    }

    public long getFileSize() {
        return size;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }
}
