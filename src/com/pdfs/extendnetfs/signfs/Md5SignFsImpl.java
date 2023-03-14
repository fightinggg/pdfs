package com.pdfs.extendnetfs.signfs;

import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

public class Md5SignFsImpl implements SignFs {

    ExtendableNetFs extendableNetFs;

    public Md5SignFsImpl(ExtendableNetFs extendableNetFs) {
        this.extendableNetFs = extendableNetFs;
    }

    @Override
    public PdfsFileInputStream read(String fileName) throws IOException {
        byte[] read = extendableNetFs.read(fileName).readAllBytes();
        byte[] res = new byte[read.length - 16];
        byte[] digest = new byte[16];
        System.arraycopy(read, 0, digest, 0, 16);
        System.arraycopy(read, 16, res, 0, res.length);

        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            if (!Arrays.equals(md5.digest(res), digest)) {
                throw new RuntimeException("文件被外部系统损坏");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return PdfsFileInputStream.fromBytes(res);
    }

    @Override
    public void write(String fileName, PdfsFileInputStream in) throws IOException {
        try {
            byte[] data = in.readAllBytes();


            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] digest = md5.digest(data);
            if (digest.length != 16) {
                throw new RuntimeException("MD5签名失败");
            }
            byte[] digestWithData = new byte[16 + data.length];
            System.arraycopy(digest, 0, digestWithData, 0, 16);
            System.arraycopy(data, 0, digestWithData, 16, data.length);
            extendableNetFs.write(fileName, PdfsFileInputStream.fromBytes(digestWithData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(String fileName) throws IOException {
        extendableNetFs.delete(fileName);
    }
}
