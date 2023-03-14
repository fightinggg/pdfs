package com.pdfs.basicnetfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.utils.Base64;
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
import java.io.InputStream;
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
        Response response = client.newCall(request).execute();
        log.info("github return {}", response.code());
        if (response.isSuccessful()) {
            InputStream inputStream = response.body().byteStream();
            long length = response.body().contentLength();
            return Base64.decode(new PdfsFileInputStream(length, inputStream));
        } else if (response.code() == 404) {
            throw new FileNotFoundException();
        } else {
            throw new RuntimeException(response.message());
        }
    }

    @Override
    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        PdfsFileInputStream old = null;
        try {
            old = readValid(fileName);
            old = Base64.encode(old);
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
        data = Base64.encode(data);
        // TODO
        params.put("content", new String(Base64.encode(data).readAllBytes()));
        if (old != null) {
            byte[] head = String.format("blob %d\0", old.getFileSize()).getBytes();
            byte[] sum = new byte[(int) (old.getFileSize() + head.length)];
            System.arraycopy(head, 0, sum, 0, head.length);
            System.arraycopy(old.readAllBytes(), 0, sum, head.length, (int) old.getFileSize());
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
