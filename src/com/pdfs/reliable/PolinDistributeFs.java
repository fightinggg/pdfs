package com.pdfs.reliable;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;
import java.util.List;

public class PolinDistributeFs implements BasicNetFs {
    List<BasicNetFs> basicNetFs;


    @Override
    public PdfsFileInputStream read(String fileName) throws IOException {
        return null;
    }

    @Override
    public void write(String fileName, PdfsFileInputStream data) throws IOException {

    }

    @Override
    public void delete(String fileName) throws IOException {

    }
}
