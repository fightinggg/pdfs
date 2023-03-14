package com.pdfs.encryptionfs;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.utils.AES;
import com.pdfs.utils.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AesEncryptionFsImpl implements EncryptionFs {


    BasicNetFs basicNetFs;
    byte[] key;

    public AesEncryptionFsImpl(BasicNetFs basicNetFs, byte[] key) {
        this.basicNetFs = basicNetFs;
        this.key = key;
    }

    public PdfsFileInputStream read(String fileName) throws IOException {
        byte[] fileNameAes = AES.encode(fileName.getBytes(StandardCharsets.UTF_8), key);
        fileName = new String(Base64.encode(fileNameAes)).replaceAll("/", "-") + ".bin";

        return AES.decode(basicNetFs.read(fileName), key);

    }

    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        byte[] fileNameAes = AES.encode(fileName.getBytes(StandardCharsets.UTF_8), key);
        fileName = new String(Base64.encode(fileNameAes)).replaceAll("/", "-") + ".bin";

        data = AES.encode(data, key);
        basicNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        basicNetFs.delete(fileName);
    }
}
