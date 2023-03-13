package com.pdfs.basicnetfs;

import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * basic net fs only support for simple file IO
 */
public interface BasicNetFs {
    /**
     * only can read filename without '/'
     * every file is ?MB
     */
    PdfsFileInputStream read(String fileName) throws IOException;

    /**
     * write data to files
     */
    void write(String fileName, PdfsFileInputStream data) throws IOException;

    /**
     * delete files
     */
    void delete(String fileName) throws IOException;


}
