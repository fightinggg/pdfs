package com.pdfs.normalfs;

import java.io.IOException;
import java.io.InputStream;

public class PdfsFileInputStream extends InputStream {
    private final long size;
    private final InputStream inputStream;

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
