package com.pdfs.basicnetfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.utils.Base64;
import com.pdfs.utils.Base64WithSize;
import com.pdfs.utils.Hex;
import com.pdfs.utils.SHA;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
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
        return Base64WithSize.decodeWithSize(_retryRead(fileName));
    }

    @Override
    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        _writeBase64(fileName, Base64WithSize.encodeWithSize(data));
    }


    @NotNull
    private PdfsFileInputStream _retryRead(String fileName) throws IOException {
        for (int i = 0; i < 10; i++) {
            try {
                return _readBase64(fileName);
            } catch (FileNotFoundException e) {
                throw e;
            } catch (UnknownHostException e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    log.warn("", ex);
                }
                log.warn("UnknownHostException" + e.getMessage());
            } catch (Exception e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    log.warn("", ex);
                }
                return _readBase64(fileName);
            }
        }
        throw new RuntimeException();
    }


    private void _writeBase64(String fileName, PdfsFileInputStream data) throws IOException {
        byte[] dataBytes = data.readAllBytes();
        byte[] oldBytes = null;
        try {
            oldBytes = _readBase64(fileName).readAllBytes();
            if (Arrays.equals(oldBytes, dataBytes)) {
                // skip
                return;
            }
        } catch (FileNotFoundException ignored) {
        }

        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", githubUsername, githubRepoName, fileName);
        log.info("request to github PUT {}", url);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        Map<String, String> params = new HashMap<>();
        params.put("message", "pdfs-github-api-upload");
        // TODO
        params.put("content", new String(Base64.encode(dataBytes)));
        if (oldBytes != null) {
            params.put("sha", githubShaCompute(oldBytes));
        }

        RequestBody body = RequestBody.create(mediaType, new ObjectMapper().writeValueAsBytes(params));

        Request request = new Request.Builder()
                .url(url)
                .method("PUT", body)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .addHeader("Content-Type", "application/json")
                .build();
        long start = System.currentTimeMillis();
        Response response = client.newCall(request).execute();
        log.info("github return {}, cost {} ms", response.code(), System.currentTimeMillis() - start);
        response.close();
        if (!response.isSuccessful()) {
            throw new RuntimeException("code=" + response.code() + " msg=" + response.message());
        }
    }

    @NotNull
    private String githubShaCompute(byte[] oldBytes) {
        byte[] head = String.format("blob %d\0", oldBytes.length).getBytes();
        byte[] sum = new byte[(int) (oldBytes.length + head.length)];
        System.arraycopy(head, 0, sum, 0, head.length);
        System.arraycopy(oldBytes, 0, sum, head.length, oldBytes.length);
        return Hex.encode(SHA.encode(sum));
    }

    @NotNull
    private PdfsFileInputStream _readBase64(String fileName) throws IOException {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", githubUsername, githubRepoName, fileName);
        log.info("request to github GET {}", url);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github.v3.raw")
                .addHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
        long start = System.currentTimeMillis();
        Response response = client.newCall(request).execute();
        log.info("github return {}, cost {} ms", response.code(), System.currentTimeMillis() - start);
        if (response.isSuccessful()) {
            response.code();
            ResponseBody body = response.body();
            return new PdfsFileInputStream(body.contentLength(), new InputStream() {
                boolean open = true;
                final InputStream inputStream = body.byteStream();

                @Override
                public int read() throws IOException {
                    int res = inputStream.read();
                    if (res == -1 && open) {
                        response.close();
                        open = false;
                    }
                    return res;
                }
            });
        } else if (response.code() == 404) {
            response.close();
            throw new FileNotFoundException();
        } else {
            response.close();
            throw new RuntimeException(response.message());
        }
    }


    @Override
    public void deleteValid(String fileName) throws IOException {

    }
}
