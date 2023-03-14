package com.pdfs.reliable;

import com.pdfs.basicnetfs.BasicNetFs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * clusters infomation
 */
public class PolinDistributeFsClusters {

    static class NamedBasicNetFs {
        String name;
        BasicNetFs basicNetFs;
    }

    static class NetFsDisk {
        String name;
        Map<String, NamedBasicNetFs> basicNetFs;
    }

    PolinDistributeFsClusters(Map<String, String> config) {
        config.put("group", "0");
        addConfig(config);
    }

    Map<String, NetFsDisk> disks = new HashMap<>();

    boolean hasSync = false;

    void init() {
        for (int i = 0; i < 10; i++) {
            if (!hasSync) {
                // do init
            }
        }
        if (!hasSync) {
            throw new RuntimeException("SYSTEM ERROR");
        }
    }


    List<NetFsDisk> getClusters() {
        init();
        return disks.values().stream().toList();
    }

    synchronized void addConfig(Map<String, String> config) {
        if (config.containsKey("group")) {
            String group = config.get("group");
            if ()
        }

        init();
    }


}
