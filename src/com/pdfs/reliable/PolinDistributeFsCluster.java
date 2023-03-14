package com.pdfs.reliable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.fsfactory.Factory;
import com.pdfs.normalfs.PdfsFileInputStream;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * clusters infomation
 */
public class PolinDistributeFsCluster {

    private static final Logger log = LoggerFactory.getLogger(PolinDistributeFsCluster.class);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NamedBasicNetFs {
        String name;
        @JsonIgnore
        ExtendableNetFs extendableNetFs;
        Map<String, String> config;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetFsDisk {
        String name;
        Map<String, NamedBasicNetFs> basicNetFs;

    }


    @Data
    public static class Cluster {
        Map<String, NetFsDisk> disks = new HashMap<>();
        Long version = 0L;
    }

    boolean hasSync = false;
    Cluster cluster = new Cluster();


    public PolinDistributeFsCluster(Map<String, String> config) {
        addConfig(config);
    }


    void init() {
        for (int i = 0; i < 10; i++) {
            if (!hasSync) {
                doSync();
            }
        }
        if (!hasSync) {
            throw new RuntimeException("SYSTEM ERROR");
        }
    }

    private void doSync() {
        List<NamedBasicNetFs> namedBasicNetFs = cluster.disks.values().stream()
                .flatMap(o -> o.basicNetFs.values().stream())
                .toList();

        if (namedBasicNetFs.isEmpty()) {
            throw new RuntimeException("SERVER ERR. NO FS HERE");
        }


        // read from all
        for (NamedBasicNetFs fs : namedBasicNetFs) {
            try {
                PdfsFileInputStream read = fs.extendableNetFs.read(PolinDistributeConfigs.clusterFileName);
                byte[] bytes = read.readAllBytes();
                Cluster remoteCluster = new ObjectMapper().readValue(bytes, new TypeReference<Cluster>() {
                });
                if (remoteCluster.version > this.cluster.version) {
                    this.cluster = remoteCluster;
                    hasSync = false;
                }
            } catch (FileNotFoundException e) {
                log.warn("could not read clusterinfo from {}", fs.name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // write to all
        for (NamedBasicNetFs fs : namedBasicNetFs) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                byte[] bytes = objectMapper.writeValueAsBytes(cluster);
                fs.extendableNetFs.write(PolinDistributeConfigs.clusterFileName, PdfsFileInputStream.fromBytes(bytes));
                hasSync = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }


    public List<NetFsDisk> getDisks() {
        init();
        return cluster.disks.values().stream().toList();
    }


    public synchronized void addConfig(Map<String, String> config) {
        if (config.containsKey("group")) {
            String group = config.get("group");
            cluster.disks.computeIfAbsent(group, s -> new NetFsDisk(group, new HashMap<>()));
            NetFsDisk netFsDisk = cluster.disks.get(group);
            String name = config.get("name");
            NamedBasicNetFs namedBasicNetFs = new NamedBasicNetFs(name, Factory.getExtendNetFs(config), config);
            netFsDisk.basicNetFs.put(name, namedBasicNetFs);
            hasSync = false;
            cluster.version++;
        }
        init();
    }
}
