//package com.pdfs.normalfs;
//
//import com.pdfs.extendnetfs.ExtendableNetFs;
//import com.pdfs.utils.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
//public class FileNormalFsImpl implements NormalFs {
//
//    Logger log = LoggerFactory.getLogger(FileNormalFsImpl.class);
//
//    private static final long maxFileSize = 1 << 20;//1MB
//
//    private static final String pathPrefix = "path";
//    private static final String filePrefix = "file";
//
//    ExtendableNetFs extendableNetFs;
//
//
//    public FileNormalFsImpl(ExtendableNetFs extendableNetFs) {
//        this.extendableNetFs = extendableNetFs;
//    }
//
//    private String encodeName(int block, String prefix, String name) {
//        return block + "/" + prefix + "/" + name;
//    }
//
//    private List<Object> getSizeFromFileSizeEncode(byte[] data) {
//        long size = 0;
//        for (int i = 0; i < 8; i++) {
//            size = size * 256 + (255 & data[i]);
//        }
//
//        byte[] dataRes = new byte[data.length - 8];
//        System.arraycopy(data, 8, dataRes, 0, dataRes.length);
//
//        return List.of(size, dataRes);
//
//    }
//
//
//    @Override
//    public PdfsFileInputStream read(String path, long from, long size) throws IOException {
//        if (path.endsWith("/")) {
//            throw new RuntimeException("Valid path=" + path);
//        }
//        try {
//            byte[] read = extendableNetFs.read(encodeName(0, filePrefix, path)).readAllBytes();
//            List<Object> decodeSizeWithData = getSizeFromFileSizeEncode(read);
//            long fileTotalSize = (long) decodeSizeWithData.get(0);
//            long validSize = Math.min(size, fileTotalSize - from);
//
//            if (validSize <= maxFileSize) {
//                byte[] res = (byte[]) decodeSizeWithData.get(1);
//                return new PdfsFileInputStream(res.length, new ByteArrayInputStream(res));
//            } else { // 处理大文件 filezize > 1MB
//                int beginPage = (int) (from / maxFileSize);
//                int endPage = (int) ((from + validSize - 1) / maxFileSize);
//                List<Supplier<InputStream>> res = new ArrayList<>();
//                for (int i = beginPage; i <= endPage; i++) {
//                    int finalI = i;
//                    res.add(() -> {
//                        byte[] readData;
//                        if (finalI == 0) {
//                            readData = (byte[]) decodeSizeWithData.get(1);
//                        } else {
//                            try {
//                                readData = extendableNetFs.read(encodeName(finalI, filePrefix, path)).readAllBytes();
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                        int totalEnd = (int) (from + validSize - 1);
//                        int start = from <= finalI * maxFileSize ? 0 : (int) (from % maxFileSize);
//                        int end = totalEnd >= (finalI * maxFileSize + maxFileSize - 1) ? readData.length - 1 : (int) (totalEnd % maxFileSize);
//                        if (start == 0 && end == readData.length - 1) {
//                            return new ByteArrayInputStream(readData);
//                        } else {
//                            byte[] subData = new byte[end - start + 1];
//                            System.arraycopy(readData, start, subData, 0, subData.length);
//                            return new ByteArrayInputStream(subData);
//                        }
//
//                    });
//
//                }
//                log.debug("total{} bytes prepare to send", validSize);
//                return new PdfsFileInputStream(validSize, IOUtils.merge(res));
//            }
//
//        } catch (FileNotFoundException e) {
//            throw new FileNotFoundException(path);
//        }
//    }
//
//    @Override
//    public void write(String path, long total, long from, InputStream inputStream) throws IOException {
//        if (path.charAt(0) != '/') {
//            throw new RuntimeException("path must start at '/', pathName='%s'".formatted(path));
//        }
//        if (from % maxFileSize != 0) {
//            throw new RuntimeException();
//        }
//
//        // 1. write file
//        if (from == 0) {
//            byte[] data = inputStream.readAllBytes();
//            byte[] data2 = new byte[data.length + 8];
//            System.arraycopy(data, 0, data2, 8, data.length);
//
//            long size = total;
//            for (int i = 0; i < 8; i++) {
//                data2[7 - i] = (byte) (size % 256);
//                size /= 256;
//            }
//
//            extendableNetFs.write(encodeName(0, filePrefix, path), PdfsFileInputStream.fromBytes(data2));
//        } else {
//            byte[] data = inputStream.readAllBytes();
//            if (data.length != maxFileSize && total - from + 1 > maxFileSize) {
//                throw new RuntimeException("参数非法");
//            }
//
//            extendableNetFs.write(encodeName((int) (from / maxFileSize), filePrefix, path), PdfsFileInputStream.fromBytes(data));
//        }
//
//        // 2. write dir
//        dirAddItem(path, false);
//    }
//
//    private void dirAddItem(String path, Boolean isDir) throws IOException {
//        String dir = path.substring(0, path.lastIndexOf("/") + 1);
//        if (isDir) {
//            dir = path.substring(0, path.substring(0, path.length() - 1).lastIndexOf("/") + 1);
//        }
//        List<File> ls;
//        try {
//            ls = ls(dir);
//            Set<File> dirsSet = new HashSet<>(ls);
//
//            if (dirsSet.add(new File(path, isDir, ))) {
//                String disContent = dirsSet.stream()
//                        .map(o -> (o.isDir ? "1" : "0") + o.name)
//                        .collect(Collectors.joining("\n"));
//
//                extendableNetFs.write(encodeName(0, pathPrefix, dir), PdfsFileInputStream.fromBytes(disContent.getBytes(StandardCharsets.UTF_8)));
//            }
//        } catch (FileNotFoundException fileNotFoundException) {
//            extendableNetFs.write(encodeName(0, pathPrefix, dir), PdfsFileInputStream.fromBytes(((isDir ? "1" : "0") + path).getBytes(StandardCharsets.UTF_8)));
//            dirAddItem(dir, true);
//        }
//
//
//    }
//
//
//    @Override
//    public void delete(String path) throws IOException {
//        throw new RuntimeException("do not support");
//        // 1. update dic
//        // 2. delete file
//
//    }
//
//    @Override
//    public List<File> ls(String path) throws IOException {
//        if (!path.endsWith("/")) {
//            throw new RuntimeException("Valid path=" + path);
//        }
//        String name = encodeName(0, pathPrefix, path);
//
//        try {
//            byte[] read = extendableNetFs.read(name).readAllBytes();
//            return Arrays.stream(new String(read).split("\n"))
//                    .map(o -> new File(o.substring(1), o.charAt(0) == '1'))
//                    .collect(Collectors.toList());
//        } catch (FileNotFoundException e) {
//            if (path.equals("/")) {
//                return new ArrayList<>();
//            } else {
//                throw e;
//            }
//        }
//
//    }
//}
