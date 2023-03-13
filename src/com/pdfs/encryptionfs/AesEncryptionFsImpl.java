package com.pdfs.encryptionfs;

import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.utils.AES;
import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.signfs.SignFs;

import java.io.IOException;
import java.io.InputStream;

public class AesEncryptionFsImpl implements EncryptionFs {


    BasicNetFs basicNetFs;
    byte[] key;

    public AesEncryptionFsImpl(SignFs basicNetFs, byte[] key) {
        this.basicNetFs = basicNetFs;
        this.key = key;
    }

    public PdfsFileInputStream read(String fileName) throws IOException {
        byte[] read = basicNetFs.read(fileName);
        return AES.decode(read, key);

    }

    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        data = AES.encode(data, key);
        basicNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        basicNetFs.delete(fileName);
    }
}
