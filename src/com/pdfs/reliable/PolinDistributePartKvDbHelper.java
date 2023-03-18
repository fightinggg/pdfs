package com.pdfs.reliable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.reliable.PolinDistributeFsCluster.NetFsDisk;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * part data key value db
 */
@Slf4j
public class PolinDistributePartKvDbHelper {


    public static void put(NetFsDisk disk, String key, String value) {
        // put to the first one

        if (disk.fsList.isEmpty()) {
            throw new RuntimeException("disk (group=%s) error, fs is empty".formatted(disk.group));
        }
        ExtendableNetFs fs = disk.fsList.get(0).extendableNetFs;

        try {
            Map<String, String> kvDb = readMap(fs);
            kvDb.put(key, value);
            byte[] bytes = new ObjectMapper().writeValueAsBytes(kvDb);
            fs.write(PolinDistributeConfigs.partKvFileName, PdfsFileInputStream.fromBytes(bytes));
        } catch (IOException e) {
            throw new RuntimeException("read part failed, maybe disk is error;", e);
        }

    }

    private static Map<String, String> readMap(ExtendableNetFs fs) throws IOException {
        try {
            PdfsFileInputStream read = fs.read(PolinDistributeConfigs.partKvFileName);
            return new ObjectMapper().readValue(read, new TypeReference<>() {
            });
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        }
    }


    public static String get(NetFsDisk disk, String key) {
        if (disk.fsList.isEmpty()) {
            throw new RuntimeException("disk (group=%s) error, fs is empty".formatted(disk.group));
        }
        ExtendableNetFs fs = disk.fsList.get(0).extendableNetFs;

        try {
            Map<String, String> kvDb = readMap(fs);
            return kvDb.get(key);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException("read part failed, maybe disk is error;", e);
        }
    }


}
