package com.pdfs;

import com.pdfs.server.HttpServer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new GnuParser();

        Map<String, String> chooseAbleParam = new HashMap<>();

        chooseAbleParam.put("qiniu_accessKey", "qiniu_accessKey");
        chooseAbleParam.put("qiniu_secretKey", "qiniu_secretKey");
        chooseAbleParam.put("qiniu_bucket", "qiniu_bucket");

        chooseAbleParam.put("github_token", "github_token");
        chooseAbleParam.put("github_username", "github_username");
        chooseAbleParam.put("github_reponame", "github_reponame");

        Options options = new Options();
        options.addOption("p", "port", true, "port");
        options.addOption("t", "type", true, "file storage in where?");
        options.addOption("k", "key", true, "your password to encry files");
        chooseAbleParam.forEach((k, v) -> options.addOption("", k, true, v));

        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("could not parse your command line");
            System.exit(-1);
        }


        Map<String, String> config = new HashMap<>();
        config.put("port", commandLine.getOptionValue("port", "8081"));
        config.put("type", commandLine.getOptionValue("type", "local"));
        config.put("key", commandLine.getOptionValue("key", "wow! pdfs!"));

        for (String k : chooseAbleParam.keySet()) {
            if (commandLine.hasOption(k)) {
                config.put(k, commandLine.getOptionValue(k));
            }
        }

        HttpServer server = new HttpServer(config);
        server.start();
    }
}
