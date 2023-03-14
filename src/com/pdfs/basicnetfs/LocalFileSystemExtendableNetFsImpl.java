package com.pdfs.basicnetfs;

import com.pdfs.normalfs.PdfsFileInputStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class LocalFileSystemExtendableNetFsImpl extends ValidExtendableNetFsAbstract {

    String fileDir;

    public LocalFileSystemExtendableNetFsImpl(Map<String, String> config) {
        if (!config.containsKey("local_path")) {
            throw new RuntimeException("local file system need param: 'local_path'");
        }
        this.fileDir = config.get("local_path");
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

        FileUtils.forceMkdir(new File(fileName).getParentFile());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.write(data.readAllBytes());
        fileOutputStream.close();
    }

    @Override
    public void deleteValid(String fileName) throws IOException {
        new File(fileDir + "/" + fileName).delete();
    }
}