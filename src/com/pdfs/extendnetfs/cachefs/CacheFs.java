package com.pdfs.extendnetfs.cachefs;

import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CacheFs implements ExtendableNetFs {

    ExtendableNetFs fs;
    Map<String, CacheNode> cacheMap = new HashMap<>();

    @Data
    @AllArgsConstructor
    static class CacheNode {
        Integer vv;
        byte[] data;
    }


    public CacheFs(ExtendableNetFs fs) {
        this.fs = fs;
    }

    @Override
    public PdfsFileInputStream read(String fileName) throws IOException {
        if (cacheMap.containsKey(fileName)) {
            CacheNode cacheNode = cacheMap.get(fileName);
            cacheNode.vv++;
            if (cacheNode.data != null) {
                return PdfsFileInputStream.fromBytes(cacheNode.data);
            }
        }

        byte[] bytes = fs.read(fileName).readAllBytes();

        if (cacheMap.containsKey(fileName)) {
            CacheNode cacheNode = cacheMap.get(fileName);
            cacheNode.data = bytes;
        } else {
            cacheMap.put(fileName, new CacheNode(1, bytes));
        }

        List<String> cacheData = cacheMap.entrySet().stream()
                .filter(o -> o.getValue().data != null)
                .sorted(Comparator.comparing(o -> o.getValue().vv))
                .map(Map.Entry::getKey).toList();
        if (cacheData.size() > 20) {
            String remove = cacheData.get(0);
            cacheMap.remove(remove);
            log.info("remove cache: {}", remove);
        }

        List<String> notCacheData = cacheMap.entrySet().stream()
                .filter(o -> o.getValue().data == null)
                .sorted(Comparator.comparing(o -> o.getValue().vv))
                .map(Map.Entry::getKey).toList();
        if (notCacheData.size() > 1000) {
            String remove = cacheData.get(0);
            cacheMap.remove(remove);
            log.info("remove cache: {}", remove);
        }

        return PdfsFileInputStream.fromBytes(bytes);

    }

    @Override
    public void write(String fileName, PdfsFileInputStream data) throws IOException {
        if (cacheMap.containsKey(fileName)) {
            CacheNode cacheNode = cacheMap.get(fileName);
            cacheNode.vv++;
            if (cacheNode.data != null) {
                cacheNode.data = data.readAllBytes();
                data = PdfsFileInputStream.fromBytes(cacheNode.data);
            }
        }

        fs.write(fileName, data);
    }

    @Override
    public void delete(String fileName) throws IOException {
        fs.delete(fileName);
    }
}
