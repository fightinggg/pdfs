package com.pdfs.basicnetfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.utils.Base64;
import com.pdfs.utils.Hex;
import com.pdfs.utils.SHA;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GithubApiExtendableNetFsImpl extends ValidExtendableNetFsAbstract {
    Logger log = LoggerFactory.getLogger(GithubApiExtendableNetFsImpl.class);

    String githubToken;
    String githubUsername;
    String githubRepoName;

    public GithubApiExtendableNetFsImpl(String githubToken, String githubUsername, String githubRepoName) {
        this.githubToken = githubToken;
        this.githubUsername = githubUsername;
        this.githubRepoName = githubRepoName;
    }

    @Override
    public PdfsFileInputStream readValid(String fileName) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", githubUsername, githubRepoName, fileName);
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github.v3.raw")
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
        log.info("request to github GET {}", url);
        long start = System.currentTimeMillis();
        Response response = client.newCall(request).execute();
        log.info("github return {}, cost {} ms", response.code(), System.currentTimeMillis() - start);
        if (response.isSuccessful()) {
            response.code();
            ResponseBody body = response.body();
            return Base64.decode(new PdfsFileInputStream(body.contentLength(), body.byteStream()));
        } else if (response.code() == 404) {
            response.code();
            throw new FileNotFoundException();
        } else {
            response.code();
            throw new RuntimeException(response.message());
        }
    }

    @Override
    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        byte[] dataBytes = data.readAllBytes();
        byte[] oldBytes = null;
        try {
            oldBytes = readValid(fileName).readAllBytes();
            if (Arrays.equals(oldBytes, dataBytes)) {
                // skip
                return;
            }
            oldBytes = Base64.encode(oldBytes);
        } catch (FileNotFoundException e) {
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        Map<String, String> params = new HashMap<>();
        params.put("message", "pdfs-github-api-upload");
        dataBytes = Base64.encode(dataBytes);
        // TODO
        params.put("content", new String(Base64.encode(dataBytes)));
        if (oldBytes != null) {
            byte[] head = String.format("blob %d\0", oldBytes.length).getBytes();
            byte[] sum = new byte[(int) (oldBytes.length + head.length)];
            System.arraycopy(head, 0, sum, 0, head.length);
            System.arraycopy(oldBytes, 0, sum, head.length, (int) oldBytes.length);
            params.put("sha", Hex.encode(SHA.encode(sum)));
        }

        RequestBody body = RequestBody.create(mediaType, new ObjectMapper().writeValueAsBytes(params));
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", githubUsername, githubRepoName, fileName);
        Request request = new Request.Builder()
                .url(url)
                .method("PUT", body)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .addHeader("Content-Type", "application/json")
                .build();
        log.info("request to github PUT {}", url);
        long start = System.currentTimeMillis();
        Response response = client.newCall(request).execute();
        log.info("github return {}, cost {} ms", response.code(), System.currentTimeMillis() - start);
        response.close();
        if (!response.isSuccessful()) {
            throw new RuntimeException(response.message());
        }
    }

    @Override
    public void deleteValid(String fileName) throws IOException {

    }
}
