package com.pdfs.extendnetfs.encryptionfs;

import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;

public class NoEncryptionFsImpl implements EncryptionFs {

    ExtendableNetFs extendableNetFs;

    public NoEncryptionFsImpl(ExtendableNetFs extendableNetFs) {
        this.extendableNetFs = extendableNetFs;
    }

    public PdfsFileInputStream read(String fileName) throws IOException {
        return extendableNetFs.read(fileName);
    }

    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        extendableNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        extendableNetFs.delete(fileName);
    }


}
