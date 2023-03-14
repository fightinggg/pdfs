package com.pdfs.basicnetfs;

import com.pdfs.normalfs.PdfsFileInputStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LocalFileSystemBasicNetFsImpl extends ValidBasicNetFsAbstract {

    String fileDir;

    public LocalFileSystemBasicNetFsImpl(String fileDir) {
        this.fileDir = fileDir;
    }

    public PdfsFileInputStream readValid(String fileName) throws IOException {
        fileName = fileDir + "/" + fileName;

        FileInputStream fileInputStream = new FileInputStream(fileName);
        byte[] bytes = fileInputStream.readAllBytes();
        fileInputStream.close();
        return PdfsFileInputStream.fromBytes(bytes);

    }

    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        fileName = fileDir + "/" + fileName;

        FileUtils.forceMkdir(new File(fileName));
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.write(data.readAllBytes());
        fileOutputStream.close();
    }

    @Override
    public void deleteValid(String fileName) throws IOException {
        new File(fileDir + "/" + fileName).delete();
    }
}
