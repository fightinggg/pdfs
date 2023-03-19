package com.pdfs.extendnetfs.cachefs;

import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CacheFs implements ExtendableNetFs {

    static class Cache {

        @Data
        @AllArgsConstructor
        static class CacheNode {
            Integer vv;
            byte[] data;
        }


        Map<String, CacheNode> cacheMap = new HashMap<>();

        byte[] get(String key) {
            if (cacheMap.containsKey(key)) {
                CacheNode cacheNode = cacheMap.get(key);
                cacheNode.vv++;
                return cacheNode.data;
            }
            return null;
        }

        void put(String key, byte[] data) {
            int cacheDataSize = 20;
            int noCacheDataSize = 1000;

            if (cacheMap.containsKey(key)) {
                CacheNode cacheNode = cacheMap.get(key);
                cacheNode.data = data;
            } else {
                cacheMap.put(key, new CacheNode(1, data));
            }

            List<String> cacheData = cacheMap.entrySet().stream()
                    .filter(o -> o.getValue().data != null)
                    .sorted(Comparator.comparing(o -> o.getValue().vv))
                    .map(Map.Entry::getKey).toList();
            if (cacheData.size() > cacheDataSize) {
                String remove = cacheData.get(0);
                cacheMap.remove(remove);
                log.info("remove cache: {}", remove);
            }

            List<String> notCacheData = cacheMap.entrySet().stream()
                    .filter(o -> o.getValue().data == null)
                    .sorted(Comparator.comparing(o -> o.getValue().vv))
                    .map(Map.Entry::getKey).toList();
            if (notCacheData.size() > noCacheDataSize) {
                String remove = cacheData.get(0);
                cacheMap.remove(remove);
//                log.info("remove cache: {}", remove);
            }
        }
    }

    ExtendableNetFs fs;
    Cache cache = new Cache();


    public CacheFs(ExtendableNetFs fs) {
        this.fs = fs;
    }

    @NotNull
    private PdfsFileInputStream putIntoCacheAfterReadAll(String fileName, PdfsFileInputStream read) {
        return new PdfsFileInputStream(read.getRemainSize(), new InputStream() {
            private byte[] data = new byte[(int) read.getRemainSize()];
            private int index = 0;

            @Override
            public int read() throws IOException {
                int x = read.read();
                if (x != -1) {
                    data[index++] = (byte) x;
                } else if (data != null) {
                    cache.put(fileName, data);
                    data = null;
                }
                return x;
            }
        });
    }

    @Override
    public PdfsFileInputStream read(String fileName) throws IOException {
        byte[] bytes = cache.get(fileName);
        if (bytes != null) {
            return PdfsFileInputStream.fromBytes(bytes);
        }

        PdfsFileInputStream read = fs.read(fileName);
        return putIntoCacheAfterReadAll(fileName, read);
    }


    @Override
    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        fs.write(fileName, putIntoCacheAfterReadAll(fileName, data));
    }


    @Override
    public void delete(String fileName) throws IOException {
        fs.delete(fileName);
    }
}
