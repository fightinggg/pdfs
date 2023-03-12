package com.pdfs.encryptionfs;

import com.pdfs.basicnetfs.BasicNetFs;

import java.io.IOException;

public class NoEncryptionFsImpl implements EncryptionFs {

    BasicNetFs basicNetFs;

    public NoEncryptionFsImpl(BasicNetFs basicNetFs) {
        this.basicNetFs = basicNetFs;
    }

    public byte[] read(String fileName) throws IOException {
        return basicNetFs.read(fileName);
    }

    public void write(String fileName, byte[] data) throws IOException {
        basicNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        basicNetFs.delete(fileName);
    }


}
