package com.pdfs.server;

import com.pdfs.fs.Factory;
import com.pdfs.normalfs.File;
import com.pdfs.normalfs.NormalFs;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.xpath.XPath;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FsHandler {

    Logger log = LoggerFactory.getLogger(FsHandler.class);

    NormalFs fs = Factory.getFs("wow! pdfs!", "system_git", new HashMap<>());

    int httpHandler(String url, String method, InputStream inputStream, OutputStream outputStream) throws IOException {
        if (method.equals("GET") && url.startsWith("/fsapi/read/")) {
            return readHandler(url, method, inputStream, outputStream);
        }
        if (method.equals("POST") && url.startsWith("/fsapi/write/")) {
            return writeHandler(url, method, inputStream, outputStream);
        }
        if (method.equals("GET") && url.startsWith("/fsapi/ls/")) {
            return lsHandler(url, method, inputStream, outputStream);
        }
        if (method.equals("GET") && url.startsWith("/fsapi/webls/")) {
            return webLsHandler(url, method, inputStream, outputStream);
        }
        outputStream.write("SORRY! Your Request Is Not Valid! ".getBytes(StandardCharsets.UTF_8));
        return 403;
    }

    int readHandler(String url, String method, InputStream inputStream, OutputStream outputStream) throws IOException {
        String path = url.substring("/fsapi/read/".length() - 1);

        try {
            InputStream read = fs.read(path, 0, 99999999999L);
            IOUtils.copy(read, outputStream);
            return 200;
        } catch (FileNotFoundException e) {
            outputStream.write(("Could Not Found " + path).getBytes(StandardCharsets.UTF_8));
            return 404;
        } catch (Exception e) {
            outputStream.write("Server ERROR ".getBytes(StandardCharsets.UTF_8));
            log.error("", e);
            return 500;
        }
    }

    int writeHandler(String url, String method, InputStream inputStream, OutputStream outputStream) throws IOException {
        try {
            String path = url.substring("/fsapi/write/".length() - 1);
            fs.write(path, 0, inputStream);
            return 200;
        } catch (Exception e) {
            outputStream.write("Server ERROR ".getBytes(StandardCharsets.UTF_8));
            log.error("", e);
            return 500;
        }
    }

    int lsHandler(String url, String method, InputStream inputStream, OutputStream outputStream) throws IOException {
        return 500;
    }

    int webLsHandler(String url, String method, InputStream inputStream, OutputStream outputStream) throws IOException {
        String path = url.substring("/fsapi/webls/".length() - 1);
        if (!path.endsWith("/")) {
            outputStream.write((path + " is not a dir  ").getBytes(StandardCharsets.UTF_8));
            return 400;
        }

        try {
            List<File> ls = fs.ls(path);

            String form = """
                     <html>
                        <span>fileName: </span>
                        <br/>
                        <input type="text" id="filename" value="_INNER_README.txt" />
                        <br/><br/>
                        <span>fileContent: </span>
                        <br/>
                        <textarea id="about" style="width:100%;height:100px"></textarea>
                        <br/><br/>
                        <button id='create' onclick="createFile()"> create or update file </button>
                        <br/>
                        <hr/>
                        
                        <script>
                            defaultValue = " 1. choose a good filename\\n 2. write something here\\n 3. click 'create or update file' button"
                            document.getElementById("about").value = defaultValue
                            function createFile(){
                                var data=document.getElementById("about").value;
                                var filename = location.pathname.substr(12) + document.getElementById("filename").value;
                                document.getElementById("create").outerHTML='<span>please wait......</span>'
                                
                                xmlobj = new XMLHttpRequest();
                                var parm = data;//构造URL参数
                                xmlobj.open("POST", "/fsapi/write"+filename, true); //调用weather.php
                                xmlobj.setRequestHeader("cache-control","no-cache");
                                xmlobj.setRequestHeader("contentType","text/html;charset=uft-8") //指定发送的编码
                                xmlobj.setRequestHeader("Content-Type", "application/text;");  //设置请求头信息
                                xmlobj.onreadystatechange = function(x){
                                    if(x.target.readyState==4){
                                        if(x.target.status!=200){
                                            alert(x.target.response)
                                            document.getElementById("create").outerHTML='<button id="create" onclick="createFile()"> create or update file </button>'
                                        }else{
                                            location.reload();
                                        }
                                    }
                                }
                                xmlobj.send(parm); //设置为发送给服务器数据
                            }
                        </script>
                    </html>
                    """;
            String body = "";
            if (ls.isEmpty()) {
                body = form + "here is empty!";
            } else {
                String disList = ls.stream()
                        .map(o -> {
                            if (o.isDir) {
                                return String.format("<a href='/fsapi/webls%s'>%s</a><br><br>", o.name, o.name);
                            } else {
                                return String.format("<a href='/fsapi/read%s'>%s</a><br><br>", o.name, o.name);
                            }
                        })
                        .collect(Collectors.joining());
                if (path.equals("/")) {
                    String father = String.format("<a href='/fsapi/webls%s'>..</a><br><br>", path);
                    body = form + father + disList;
                } else {
                    String fatherdir = path.substring(0, path.substring(0, path.length() - 1).lastIndexOf("/") + 1);
                    String father = String.format("<a href='/fsapi/webls%s'>..</a><br><br>", fatherdir);
                    body = form + father + disList;
                }

            }
            outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            return 200;
        } catch (FileNotFoundException e) {
            outputStream.write(("Could Not Found " + path).getBytes(StandardCharsets.UTF_8));
            return 404;
        } catch (Exception e) {
            outputStream.write("Server ERROR ".getBytes(StandardCharsets.UTF_8));
            log.error("", e);
            return 500;
        }
    }


}
