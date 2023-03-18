package com.pdfs.reliable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.normalfs.File;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.reliable.PolinDistributeFsCluster.NetFsDisk;
import com.pdfs.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class PolinDistributeFs implements NormalFs {
    Logger log = LoggerFactory.getLogger(PolinDistributeFs.class);

    private static final long maxFileSize = 1 << 20;//1MB

    private static final String pathPrefix = "path";
    private static final String filePrefix = "file";

    public PolinDistributeFsCluster cluster;


    public PolinDistributeFs(Map<String, String> config) {
        this.cluster = new PolinDistributeFsCluster(config);
    }

    private String encodeName(int block, String prefix, String name) {
        return block + "/" + prefix + name;
    }

    private List<Object> getSizeFromFileSizeEncode(PdfsFileInputStream data) throws IOException {
        long size = 0;
        for (int i = 0; i < 8; i++) {
//            size = size * 256 + (255 & data[i]);
            size = size * 256 + (255 & data.read());
        }

        return List.of(size, data);

    }


    private PdfsFileInputStream read(String path) throws IOException {
        // TODO 兼容 读失败

        NetFsDisk netFsDisk = cluster.getDisks().get(0);
        return PolinDistributeReadWriteHelper.read(netFsDisk, path);
    }

    private PdfsFileInputStream read(String group, String path) throws IOException {
        // TODO 兼容 读失败

        Optional<NetFsDisk> any = cluster.getDisks().stream().filter(o -> o.group.equals(group)).findAny();
        if (any.isEmpty()) {
            throw new RuntimeException();
        }
        NetFsDisk netFsDisk = any.get();
        return PolinDistributeReadWriteHelper.read(netFsDisk, path);
    }


    private void write(String group, String path, PdfsFileInputStream pdfsFileInputStream) throws IOException {
        Optional<NetFsDisk> any = cluster.getDisks().stream().filter(o -> o.group.equals(group)).findAny();
        if (any.isEmpty()) {
            throw new RuntimeException();
        }
        NetFsDisk netFsDisk = any.get();
        PolinDistributeReadWriteHelper.write(netFsDisk, path, pdfsFileInputStream);
    }

    private void write(List<String> groups, String path, PdfsFileInputStream pdfsFileInputStream) throws IOException {
        for (String group : groups) {
            write(group, path, pdfsFileInputStream);
        }
    }


    private String parrentPath(String s) {
        String[] split = s.split("/");
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < split.length - 1; i++) {
            res.append(split[i]).append("/");
        }
        return res.toString();
    }


    @Override
    public PdfsFileInputStream read(String path, long from, long size) throws IOException {
        if (path.charAt(0) != '/') {
            throw new RuntimeException("file must start at '/', filename='%s'".formatted(path));
        }

        if (path.endsWith("/")) {
            throw new RuntimeException("Valid path=" + path);
        }

        List<File> ls = ls(parrentPath(path));
        Optional<File> readFile = ls.stream().filter(o -> o.name.equals(path)).findAny();
        if (readFile.isEmpty()) {
            throw new FileNotFoundException();
        }
        String group = readFile.get().getGroups().get(0);


        try {
            PdfsFileInputStream read = read(group, encodeName(0, filePrefix, path));
            List<Object> decodeSizeWithData = getSizeFromFileSizeEncode(read);
            long fileTotalSize = (long) decodeSizeWithData.get(0);
            long validSize = Math.min(size, fileTotalSize - from);

            if (validSize <= maxFileSize) {
                return (PdfsFileInputStream) decodeSizeWithData.get(1);
            } else { // 处理大文件 filezize > 1MB
                int beginPage = (int) (from / maxFileSize);
                int endPage = (int) ((from + validSize - 1) / maxFileSize);
                List<Supplier<InputStream>> res = new ArrayList<>();
                for (int i = beginPage; i <= endPage; i++) {
                    int finalI = i;
                    res.add(() -> {
                        PdfsFileInputStream readData;
                        if (finalI == 0) {
                            readData = (PdfsFileInputStream) decodeSizeWithData.get(1);
                        } else {
                            try {
                                readData = read(group, encodeName(finalI, filePrefix, path));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        long totalEnd = from + validSize - 1;
                        long start = from <= finalI * maxFileSize ? 0 : (int) (from % maxFileSize);
                        long end = totalEnd >= (finalI * maxFileSize + maxFileSize - 1) ? readData.getRemainSize() - 1 : (totalEnd % maxFileSize);
                        if (start == 0 && end == readData.getRemainSize() - 1) {
                            return readData;
                        } else {
                            try {
                                readData.skipNBytes(start);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            // TODO 子串 // (end - start + 1)
                            return readData;
                        }
                    });

                }
                log.debug("total {} bytes prepare to send", validSize);
                return new PdfsFileInputStream(validSize, IOUtils.merge(res));
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(path);
        }
    }

    @Override
    public void write(String path, long total, long from, InputStream inputStream) throws IOException {
        if (path.charAt(0) != '/') {
            throw new RuntimeException("path must start at '/', pathName='%s'".formatted(path));
        }
        if (from % maxFileSize != 0) {
            throw new RuntimeException();
        }

        String firstGroup = cluster.getDisks().get(0).group;

        // 1. write file
        if (from == 0) {
            byte[] data = inputStream.readAllBytes();
            byte[] data2 = new byte[data.length + 8];
            System.arraycopy(data, 0, data2, 8, data.length);

            long size = total;
            for (int i = 0; i < 8; i++) {
                data2[7 - i] = (byte) (size % 256);
                size /= 256;
            }

            write(firstGroup, encodeName(0, filePrefix, path), PdfsFileInputStream.fromBytes(data2));
        } else {
            byte[] data = inputStream.readAllBytes();
            if (data.length != maxFileSize && total - from + 1 > maxFileSize) {
                throw new RuntimeException("参数非法");
            }

            write(firstGroup, encodeName((int) (from / maxFileSize), filePrefix, path), PdfsFileInputStream.fromBytes(data));
        }

        // 2. last write dir
        if (from + (1 << 20) >= total) {
            dirAddItem(path, false, cluster.getDisks().stream().map(NetFsDisk::getGroup).toList());
        }
    }

    private void dirAddItem(String path, Boolean isDir, List<String> allGroup) throws IOException {
        String dir = path.substring(0, path.lastIndexOf("/") + 1);
        if (isDir) {
            dir = path.substring(0, path.substring(0, path.length() - 1).lastIndexOf("/") + 1);
        }
        List<File> ls;
        try {
            ls = ls(dir);
            Set<File> dirsSet = new HashSet<>(ls);

            if (dirsSet.add(new File(path, isDir, allGroup))) {
                String disContent = new ObjectMapper().writeValueAsString(dirsSet);
                write(allGroup, encodeName(0, pathPrefix, dir), PdfsFileInputStream.fromBytes(disContent.getBytes(StandardCharsets.UTF_8)));
            }
        } catch (FileNotFoundException fileNotFoundException) {
            Set<File> dirsSet = new HashSet<>();
            dirsSet.add(new File(path, isDir, allGroup));
            String disContent = new ObjectMapper().writeValueAsString(dirsSet);
            write(allGroup, encodeName(0, pathPrefix, dir), PdfsFileInputStream.fromBytes(disContent.getBytes(StandardCharsets.UTF_8)));
            dirAddItem(dir, true, allGroup);
        }


    }


    @Override
    public void delete(String path) throws IOException {
        throw new RuntimeException("do not support");
        // 1. update dic
        // 2. delete file

    }

    @Override
    public List<File> ls(String path) throws IOException {
        if (!path.endsWith("/")) {
            throw new RuntimeException("Valid path=" + path);
        }
        String name = encodeName(0, pathPrefix, path);

        try {
            byte[] read = read(name).readAllBytes();
            return new ObjectMapper().readValue(read, new TypeReference<>() {
            });
        } catch (FileNotFoundException e) {
            if (path.equals("/")) {
                return new ArrayList<>();
            } else {
                throw e;
            }
        }

    }
}
