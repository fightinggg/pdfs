package com.pdfs.basicnetfs;

import java.io.IOException;

public abstract class ValidBasicNetFsAbstract implements BasicNetFs {
    private boolean fileNameInvalid(String fileName) {
        return fileName.contains("\\") || fileName.contains("/") || fileName.contains("\n");
    }


    @Override
    public byte[] read(String fileName) throws IOException {
        if (fileNameInvalid(fileName)) {
            throw new IOException();
        }
        return readValid(fileName);
    }

    @Override
    public void write(String fileName, byte[] data) throws IOException {
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

    public abstract byte[] readValid(String fileName) throws IOException;

    public abstract void writeValid(String fileName, byte[] data) throws IOException;

    public abstract void deleteValid(String fileName) throws IOException;

}
