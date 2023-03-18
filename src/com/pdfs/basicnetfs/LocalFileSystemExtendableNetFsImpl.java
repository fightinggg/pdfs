package com.pdfs.basicnetfs;

import com.pdfs.normalfs.PdfsFileInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Map;

@Slf4j
public class LocalFileSystemExtendableNetFsImpl extends ValidExtendableNetFsAbstract {

    String fileDir;

    public LocalFileSystemExtendableNetFsImpl(Map<String, String> config) {
        if (!config.containsKey("local_path")) {
            throw new RuntimeException("local file system need param: 'local_path'");
        }
        this.fileDir = config.get("local_path");
    }

    public PdfsFileInputStream readValid(String fileName) throws IOException {
        log.info("read: " + fileName);
        fileName = fileDir + "/" + fileName;

        FileInputStream fileInputStream = new FileInputStream(fileName);
        byte[] bytes = fileInputStream.readAllBytes();
        fileInputStream.close();
        return new PdfsFileInputStream(bytes.length, new InputStream() {
            final InputStream inputStream = new ByteArrayInputStream(bytes);

            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return inputStream.read();
            }
        });

    }

    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        log.info("write: " + fileName);
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
