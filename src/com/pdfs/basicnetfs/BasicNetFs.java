package com.pdfs.basicnetfs;

import java.io.IOException;

/**
 * basic net fs only support for simple file IO
 */
public interface BasicNetFs {
    /**
     * only can read filename without '/'
     * every file is ?MB
     */
    byte[] read(String fileName) throws IOException;

    /**
     * write data to files
     */
    void write(String fileName, byte[] data) throws IOException;

    /**
     * delete files
     */
    void delete(String fileName) throws IOException;


}
