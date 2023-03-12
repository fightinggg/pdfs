package com.pdfs.normalfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface NormalFs {
    InputStream read(String path, long from, long size) throws IOException;

    void write(String path, long from, InputStream inputStream) throws IOException;

    void delete(String path) throws IOException;

    List<File> ls(String path) throws IOException;
}
