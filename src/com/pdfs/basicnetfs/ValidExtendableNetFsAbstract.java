package com.pdfs.basicnetfs;

import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;

public abstract class ValidExtendableNetFsAbstract implements BasicNetFs {
    private boolean fileNameInvalid(String fileName) {
        return fileName.contains("\\") || fileName.contains("/") || fileName.contains("\n");
    }


    @Override
    public PdfsFileInputStream read(String fileName) throws IOException {
        if (fileNameInvalid(fileName)) {
            throw new IOException();
        }
        return readValid(fileName);
    }

    @Override
    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        if (fileNameInvalid(fileName)) {
            throw new IOException();
        }
        writeValid(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        if (fileNameInvalid(fileName)) {
            throw new IOException();
        }
        deleteValid(fileName);
    }

    public abstract PdfsFileInputStream readValid(String fileName) throws IOException;

    public abstract void writeValid(String fileName, PdfsFileInputStream data) throws IOException;

    public abstract void deleteValid(String fileName) throws IOException;

}
