package com.pdfs.extendnetfs;

import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;

/**
 * extend net fs
 */
public interface ExtendableNetFs {
    /**
     * only can read filename without '/'
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
