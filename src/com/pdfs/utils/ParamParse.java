package com.pdfs.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ParamParse {
    @NotNull
    public static Map<String, String> parse(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser();

        Map<String, String> chooseAbleParam = new HashMap<>();

        List<String> of = List.of(

                "qiniu_accessKey",
                "qiniu_secretKey",
                "qiniu_bucket",
                "github_token",
                "github_username",
                "github_reponame",
                "group",
                "name",
                "local_path"
        );

        for (String s : of) {
            chooseAbleParam.put(s, s);

        }

        Options options = new Options();
        options.addOption("p", "port", true, "port");
        options.addOption("t", "type", true, "file storage in where?");
        options.addOption("k", "key", true, "your password to encry files");
        chooseAbleParam.forEach((k, v) -> options.addOption("", k, true, v));

        CommandLine commandLine = null;
        commandLine = parser.parse(options, args);


        Map<String, String> config = new HashMap<>();
        config.put("port", commandLine.getOptionValue("port", "8081"));
        config.put("type", commandLine.getOptionValue("type", "local"));
        config.put("key", commandLine.getOptionValue("key", "wow! pdfs!"));

        for (String k : chooseAbleParam.keySet()) {
            if (commandLine.hasOption(k)) {
                config.put(k, commandLine.getOptionValue(k));
            }
        }
        return config;
    }
}
