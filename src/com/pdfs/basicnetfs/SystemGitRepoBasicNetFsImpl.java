package com.pdfs.basicnetfs;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SystemGitRepoBasicNetFsImpl extends ValidBasicNetFsAbstract {

    Logger log = LoggerFactory.getLogger(SystemGitRepoBasicNetFsImpl.class);

    String localGitRepo;
    String remoteGitRepo;
    String pk;

    public SystemGitRepoBasicNetFsImpl(String localGitRepo, String remoteGitRepo, String pk) {
        this.localGitRepo = localGitRepo;
        this.remoteGitRepo = remoteGitRepo;
        this.pk = pk;
    }

    private void awaitExec(Process exec) throws InterruptedException, IOException {
        exec.waitFor();
        if (false) {
            byte[] stdout = exec.getInputStream().readAllBytes();
            byte[] errout = exec.getErrorStream().readAllBytes();

            if (stdout.length != 0) {
                log.info("{}", new String(stdout));
            }
            if (errout.length != 0) {
                log.error("{}", new String(errout));
            }
        }
    }

    @Override
    public byte[] readValid(String fileName) throws IOException {
        try {
            init();

            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", "master"}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", "-b", fileName}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", fileName}, null, new File(localGitRepo)));

            FileInputStream fileInputStream = new FileInputStream(localGitRepo + "/" + fileName);
            byte[] bytes = fileInputStream.readAllBytes();
            fileInputStream.close();
            return bytes;
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void init() throws IOException, InterruptedException {
        if (!new File(localGitRepo + "/.git").exists()) {
            FileUtils.forceMkdir(new File(localGitRepo));

            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "clone", remoteGitRepo, "."}, null, new File(localGitRepo)));


            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", "-b", "master"}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", "master"}, null, new File(localGitRepo)));

            FileOutputStream fileOutputStream = new FileOutputStream(localGitRepo + "/README.txt");
            fileOutputStream.write("README".getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();

            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "add", "README.txt"}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "commit", "-m", "-"}, null, new File(localGitRepo)));


//            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "remote", "add", "remote", remoteGitRepo}, null, new File(localGitRepo)));


            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "push", "--set-upstream", "origin", "master"}, null, new File(localGitRepo)));
        }
    }

    @Override
    public void writeValid(String fileName, byte[] data) throws IOException {
        try {

            init();

            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", "master"}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", "-b", fileName}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "checkout", fileName}, null, new File(localGitRepo)));

            FileOutputStream fileOutputStream = new FileOutputStream(localGitRepo + "/" + fileName);
            fileOutputStream.write(data);
            fileOutputStream.close();

            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "add", fileName}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "commit", "-m", "-"}, null, new File(localGitRepo)));
            awaitExec(Runtime.getRuntime().exec(new String[]{"git", "push", "--set-upstream", "origin", fileName}, null, new File(localGitRepo)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteValid(String fileName) throws IOException {

    }
}
