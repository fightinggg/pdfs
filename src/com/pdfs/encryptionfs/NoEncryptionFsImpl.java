package com.pdfs.encryptionfs;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;
import java.io.InputStream;

public class NoEncryptionFsImpl implements EncryptionFs {

    BasicNetFs basicNetFs;

    public NoEncryptionFsImpl(BasicNetFs basicNetFs) {
        this.basicNetFs = basicNetFs;
    }

    public PdfsFileInputStream read(String fileName) throws IOException {
        return basicNetFs.read(fileName);
    }

    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        basicNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        basicNetFs.delete(fileName);
    }


}
