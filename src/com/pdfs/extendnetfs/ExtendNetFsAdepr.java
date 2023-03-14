package com.pdfs.extendnetfs;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;

public class ExtendNetFsAdepr implements ExtendableNetFs {
    BasicNetFs basicNetFs;

    public ExtendNetFsAdepr(BasicNetFs basicNetFs) {
        this.basicNetFs = basicNetFs;
    }

    @Override
    public PdfsFileInputStream read(String fileName) throws IOException {
        return basicNetFs.read(fileName);
    }

    @Override
    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        basicNetFs.write(fileName, data);

    }

    @Override
    public void delete(String fileName) throws IOException {
        basicNetFs.delete(fileName);
    }
}
