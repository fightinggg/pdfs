package com.pdfs.utils;

import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Queue;

import static com.pdfs.utils.Base64.encode;
import static com.pdfs.utils.Base64.decode;

public class Base64WithSize {
    public static PdfsFileInputStream encodeWithSize(PdfsFileInputStream pdfsFileInputStream) throws IOException {
        long remainSize = pdfsFileInputStream.getRemainSize();
        byte[] encode = LongEncodeDecode.encode(remainSize);
        byte[] sizeBase64 = encode(encode);


        return new PdfsFileInputStream(12 + (remainSize + 2) / 3 * 4, new InputStream() {
            final Queue<Byte> queue = new ArrayDeque<>();

            {
                for (byte b : sizeBase64) {
                    queue.add(b);
                }
            }

            @Override
            public int read() throws IOException {
                if (queue.isEmpty()) {
                    byte[] bytes = pdfsFileInputStream.readNBytes(3);
                    byte[] decode = encode(bytes);
                    for (byte b : decode) {
                        queue.add(b);
                    }
                }
                return queue.isEmpty() ? -1 : (queue.poll() & 255);
            }
        });
    }

    public static PdfsFileInputStream decodeWithSize(PdfsFileInputStream pdfsFileInputStream) throws IOException {
        byte[] bytes = pdfsFileInputStream.readNBytes(12); // 8 byte split to 3 block, 12bit
        byte[] decode = decode(bytes);
        long remainSize = LongEncodeDecode.decode(decode);

        return new PdfsFileInputStream(remainSize, new InputStream() {
            final Queue<Byte> queue = new ArrayDeque<>();

            @Override
            public int read() throws IOException {
                if (queue.isEmpty()) {
                    byte[] bytes = pdfsFileInputStream.readNBytes(4);
                    byte[] decode = decode(bytes);
                    for (byte b : decode) {
                        queue.add(b);
                    }
                }
                return queue.isEmpty() ? -1 : (queue.poll() & 255);
            }
        });
    }
}
