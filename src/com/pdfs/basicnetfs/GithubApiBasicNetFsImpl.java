package com.pdfs.basicnetfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.utils.Hex;
import com.pdfs.utils.SHA;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GithubApiBasicNetFsImpl extends ValidBasicNetFsAbstract {
    Logger log = LoggerFactory.getLogger(GithubApiBasicNetFsImpl.class);

    String githubToken;
    String githubUsername;
    String githubRepoName;

    public GithubApiBasicNetFsImpl(String githubToken, String githubUsername, String githubRepoName) {
        this.githubToken = githubToken;
        this.githubUsername = githubUsername;
        this.githubRepoName = githubRepoName;
    }

    @Override
    public byte[] readValid(String fileName) throws IOException {
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
        Response response = client.newCall(request).execute();
        log.info("github return {}", response.code());
        if (response.isSuccessful()) {
            byte[] bytes = response.body().bytes();
            log.info("github GET {} done", url);
            response.body().close();
            String src = new String(bytes);
            bytes = Base64.getDecoder().decode(src);
            return bytes;
        } else if (response.code() == 404) {
            response.body().close();
            throw new FileNotFoundException();
        } else {
            response.body().close();
            throw new RuntimeException(response.message());
        }
    }

    @Override
    public void writeValid(String fileName, byte[] data) throws IOException {
        byte[] old = null;
        try {
            old = readValid(fileName);
            old = Base64.getEncoder().encode(old);
        } catch (Exception e) {
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
        data = Base64.getEncoder().encode(data);
        params.put("content", new String(Base64.getEncoder().encode(data)));
        if (old != null) {
            byte[] head = String.format("blob %d\0", old.length).getBytes();
            byte[] sum = new byte[old.length + head.length];
            System.arraycopy(head, 0, sum, 0, head.length);
            System.arraycopy(old, 0, sum, head.length, old.length);
            params.put("sha", Hex.encode(SHA.encode(sum)));
        }

        RequestBody body = RequestBody.create(mediaType, new ObjectMapper().writeValueAsBytes(params));
        Request request = new Request.Builder()
                .url(String.format("https://api.github.com/repos/%s/%s/contents/%s", githubUsername, githubRepoName, fileName))
                .method("PUT", body)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        response.close();
        if (!response.isSuccessful()) {
            throw new RuntimeException(response.message());
        }
    }

    @Override
    public void deleteValid(String fileName) throws IOException {

    }
}
