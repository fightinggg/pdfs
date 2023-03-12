package com.pdfs.normalfs;

import com.pdfs.utils.AES;
import com.pdfs.utils.Hex;
import com.pdfs.encryptionfs.EncryptionFs;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DirectFileNormalFsImpl implements NormalFs {
    private static final String pathPrefix = "path=";
    private static final String filePrefix = "file=";

    byte[] fileNameKey;
    EncryptionFs encryptionFs;


    public DirectFileNormalFsImpl(byte[] fileNameKey, EncryptionFs encryptionFs) {
        this.fileNameKey = fileNameKey;
        this.encryptionFs = encryptionFs;
    }

    private String encodeName(String prefix, String name) {
        // key = 0, for debug
        if (fileNameKey.length == 0) {
            return prefix + name.replace("/", "-") + ".txt";
        }
        byte[] pathName = (prefix + name).getBytes(StandardCharsets.UTF_8);
        return Hex.encode(AES.encode(pathName, fileNameKey)) + ".bin";
    }

    @Override
    public InputStream read(String path, long from, long size) throws IOException {
        if (path.endsWith("/")) {
            throw new RuntimeException("Valid path=" + path);
        }
        try {
            String name = encodeName(filePrefix, path);
            byte[] read = encryptionFs.read(name);
            byte[] res = new byte[(int) Math.min(size, read.length - from)];
            System.arraycopy(read, (int) from, res, 0, res.length);
            return new ByteArrayInputStream(res);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(path);
        }
    }

    @Override
    public void write(String path, long from, InputStream inputStream) throws IOException {
        if (path.charAt(0) != '/') {
            throw new RuntimeException("path must start at '/', pathName='%s'".formatted(path));
        }
        if (from != 0) {
            throw new RuntimeException();
        }

        // 1. write file
        String name = encodeName(filePrefix, path);
        encryptionFs.write(name, inputStream.readAllBytes());

        // 2. write dir
        dirAddItem(path, false);
    }

    private void dirAddItem(String path, Boolean isDir) throws IOException {
        String dir = path.substring(0, path.lastIndexOf("/") + 1);
        if (isDir) {
            dir = path.substring(0, path.substring(0, path.length() - 1).lastIndexOf("/") + 1);
        }
        List<File> ls;
        try {
            ls = ls(dir);
            Set<File> dirsSet = new HashSet<>(ls);

            if (dirsSet.add(new File(path, isDir))) {
                String disContent = dirsSet.stream()
                        .map(o -> (o.isDir ? "1" : "0") + o.name)
                        .collect(Collectors.joining("\n"));

                encryptionFs.write(encodeName(pathPrefix, dir), disContent.getBytes(StandardCharsets.UTF_8));
            }
        } catch (FileNotFoundException fileNotFoundException) {
            encryptionFs.write(encodeName(pathPrefix, dir), ((isDir ? "1" : "0") + path).getBytes(StandardCharsets.UTF_8));
            dirAddItem(dir, true);
        }


    }


    @Override
    public void delete(String path) throws IOException {
        throw new RuntimeException("do not support");
        // 1. update dic
        // 2. delete file

    }

    @Override
    public List<File> ls(String path) throws IOException {
        if (!path.endsWith("/")) {
            throw new RuntimeException("Valid path=" + path);
        }
        String name = encodeName(pathPrefix, path);

        try {
            byte[] read = encryptionFs.read(name);
            return Arrays.stream(new String(read).split("\n"))
                    .map(o -> new File(o.substring(1), o.charAt(0) == '1'))
                    .collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            if (path.equals("/")) {
                return new ArrayList<>();
            } else {
                throw e;
            }
        }

    }
}
