package com.pdfs.reliable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.reliable.PolinDistributeFsCluster.NamedBasicNetFs;
import com.pdfs.reliable.PolinDistributeFsCluster.NetFsDisk;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class PolinDistributeReadWriteHelper {

    public static PdfsFileInputStream read(NetFsDisk disk, String fileName) throws IOException {
        String s = PolinDistributePartKvDbHelper.get(disk, fileName);
        Optional<NamedBasicNetFs> any = disk.fsList.stream().filter(o -> o.name.equals(s)).toList().stream().findAny();
        if (any.isEmpty()) {
            throw new FileNotFoundException(fileName);
        }

        return any.get().extendableNetFs.read(fileName);
    }


    public static void write(NetFsDisk disk, String fileName, PdfsFileInputStream data) throws IOException {
        String old = PolinDistributePartKvDbHelper.get(disk, fileName);
        Optional<NamedBasicNetFs> filter = disk.fsList.stream().filter(o -> o.name.equals(old)).findAny();

        if (filter.isPresent()) {
            try {
                filter.get().extendableNetFs.write(fileName, data);
                return;
            } catch (Exception e) {
                log.warn("原始文件写入失败");
            }
        }

        List<Exception> es = new ArrayList<>();
        List<NamedBasicNetFs> fsList = new ArrayList<>(disk.fsList);
        Collections.shuffle(fsList);
        for (NamedBasicNetFs namedBasicNetFs : fsList) {
            try {
                namedBasicNetFs.extendableNetFs.write(fileName, data);
                PolinDistributePartKvDbHelper.put(disk, fileName, namedBasicNetFs.name);
                return;
            } catch (Exception e) {
                es.add(e);
            }
        }
        for (Exception e : es) {
            log.error("", e);
        }
        throw new RuntimeException("File can not write to all fs, file=" + fileName);
    }


}
