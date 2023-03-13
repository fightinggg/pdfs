package com.pdfs.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

public class IOUtils {
    public static InputStream merge(List<Supplier<InputStream>> supplierList) {
        if (supplierList.isEmpty()) {
            return InputStream.nullInputStream();
        }

        return new InputStream() {
            int i = 0;
            InputStream inputStream = supplierList.get(0).get();

            @Override
            public int read() throws IOException {
                while (true) {
                    int value = inputStream.read();
                    if (value != -1) {
                        return value;
                    }

                    // inputStream read EOF, go to next inputStream
                    i++;
                    if (i < supplierList.size()) {
                        inputStream = supplierList.get(i).get();
                    } else {
                        return -1;
                    }
                }
            }
        };
    }
}
