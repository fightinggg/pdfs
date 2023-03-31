# PDFS

PDFS is Polin Distribute File System...

- Dreaming For Larger Space to Storage File

- Dreaming For High Fault Tolerance

- Dreaming For Faster Downloading and Uploading Speed

# PDFS V1.0 
PDFS v1.0 is PFS(Polin File System), don't support for distribute , just file system.

In PFS, every file storage using system's git shell

# Quick Start

PDFS v1.0 only support for me now, support for others will come soon

welcome to [PDFS v1.0](http://43.153.75.227:8081/)

```shell
docker pull fightinggg/pdfs:master && \
docker rm -f pdfs &&  \
docker run -d -v $HOME/.ssh:/root/.ssh:ro \
-p 8081:8081 --name pdfs fightinggg/pdfs:master  \
java -jar /app/pdfs.jar \
-type github_api \
-github_token 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 \
-github_reponame pdfs-data-githubapi \
-github_username fightinggg \
-group github -name data1 -key pdfs5
```
