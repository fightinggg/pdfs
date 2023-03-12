package com.pdfs.encryptionfs;

import com.pdfs.utils.AES;
import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.signfs.SignFs;

import java.io.IOException;

public class AesEncryptionFsImpl implements EncryptionFs {


    BasicNetFs basicNetFs;
    byte[] key;

    public AesEncryptionFsImpl(SignFs basicNetFs, byte[] key) {
        this.basicNetFs = basicNetFs;
        this.key = key;
    }

    public byte[] read(String fileName) throws IOException {
        byte[] read = basicNetFs.read(fileName);
        return AES.decode(read, key);

    }

    public void write(String fileName, byte[] data) throws IOException {
        data = AES.encode(data, key);
        basicNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        basicNetFs.delete(fileName);
    }
}
