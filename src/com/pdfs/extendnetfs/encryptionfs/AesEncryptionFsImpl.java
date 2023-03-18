package com.pdfs.extendnetfs.encryptionfs;

import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.utils.AES;
import com.pdfs.utils.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AesEncryptionFsImpl implements EncryptionFs {


    ExtendableNetFs extendableNetFs;
    byte[] key;

    public AesEncryptionFsImpl(ExtendableNetFs extendableNetFs, byte[] key) {
        this.extendableNetFs = extendableNetFs;
        this.key = key;
    }

    public PdfsFileInputStream read(String fileName) throws IOException {
        byte[] fileNameAes = AES.encode(fileName.getBytes(StandardCharsets.UTF_8), key);
        fileName = new String(Base64.encode(fileNameAes)).replaceAll("/", "-") + ".bin";

        return AES.decodePerSize(extendableNetFs.read(fileName), key);

    }

    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        byte[] fileNameAes = AES.encode(fileName.getBytes(StandardCharsets.UTF_8), key);
        fileName = new String(Base64.encode(fileNameAes)).replaceAll("/", "-") + ".bin";

        data = AES.encodePerSize(data, key);
        extendableNetFs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        extendableNetFs.delete(fileName);
    }
}
