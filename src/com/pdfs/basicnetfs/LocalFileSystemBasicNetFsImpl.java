package com.pdfs.basicnetfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LocalFileSystemBasicNetFsImpl extends ValidBasicNetFsAbstract {

    String fileDir;

    public LocalFileSystemBasicNetFsImpl(String fileDir) {
        this.fileDir = fileDir;
    }

    public byte[] readValid(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileDir + "/" + fileName);
        byte[] bytes = fileInputStream.readAllBytes();
        fileInputStream.close();
        return bytes;

    }

    public void writeValid(String fileName, byte[] data) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileDir + "/" + fileName);
        fileOutputStream.write(data);
        fileOutputStream.close();
    }

    @Override
    public void deleteValid(String fileName) throws IOException {
        new File(fileDir + "/" + fileName).delete();
    }
}
