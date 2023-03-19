package com.pdfs.utils;

import com.pdfs.normalfs.PdfsFileInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Queue;

public class AES {

    private static final int blockSize = 64; // every 16 bit do AES

    public static PdfsFileInputStream decodePerSize(PdfsFileInputStream data, byte[] key) throws IOException {
        byte[] oldSizeBytes = data.readNBytes(8);
        long oldSize = LongEncodeDecode.decode(oldSizeBytes);

        final Queue<Byte> queue = new ArrayDeque<>();

        int size = blockSize; // 16B
        return new PdfsFileInputStream(oldSize, new InputStream() {

            @Override
            public int read() throws IOException {
                if (queue.isEmpty()) {
                    byte[] bytes = data.readNBytes(size);
                    if (bytes.length != 0) {
                        for (byte b : AES.decode(bytes, key)) {
                            queue.add(b);
                        }
                    }
                }
                return queue.isEmpty() ? -1 : (255 & queue.poll());
            }

            @Override
            public void close() throws IOException {
                super.close();
                data.close();
            }
        });
    }

    private static long aesOutSize(long x) {
        if (x == 0) {
            return 0;
        }
        if (x % 16 == 0) {
            return x + 16;
        }
        return (x + 15) / 16 * 16;
    }

    public static PdfsFileInputStream encodePerSize(PdfsFileInputStream data, byte[] key) {
        int size = blockSize - 1;


        final Queue<Byte> queue = new ArrayDeque<>();
        long last = data.getRemainSize() % size;
        long resultSize = data.getRemainSize() / size * aesOutSize(size) + aesOutSize(last) + 8;
        for (byte b : LongEncodeDecode.encode(data.getRemainSize())) {
            queue.add(b);
        }

        return new PdfsFileInputStream(resultSize, new InputStream() {

            @Override
            public int read() throws IOException {
                if (queue.isEmpty()) {
                    byte[] bytes = data.readNBytes(size);
                    if (bytes.length != 0) {
                        byte[] encode = AES.encode(bytes, key);
                        if (encode.length != aesOutSize(bytes.length)) {
                            throw new RuntimeException();
                        }
                        for (byte b : encode) {
                            queue.add(b);
                        }
                    }
                }
                return queue.isEmpty() ? -1 : (255 & queue.poll());
            }

            @Override
            public void close() throws IOException {
                super.close();
                data.close();
            }
        });
    }


    public static byte[] decode(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encode(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
