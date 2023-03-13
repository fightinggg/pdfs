package com.pdfs.basicnetfs;

import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.*;

public class LocalFileSystemBasicNetFsImpl extends ValidBasicNetFsAbstract {

    String fileDir;

    public LocalFileSystemBasicNetFsImpl(String fileDir) {
        this.fileDir = fileDir;
    }

    public PdfsFileInputStream readValid(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileDir + "/" + fileName);
        byte[] bytes = fileInputStream.readAllBytes();
        fileInputStream.close();
        return bytes;

    }

    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileDir + "/" + fileName);
        fileOutputStream.write(data);
        fileOutputStream.close();
    }

    @Override
    public void deleteValid(String fileName) throws IOException {
        new File(fileDir + "/" + fileName).delete();
    }
}
