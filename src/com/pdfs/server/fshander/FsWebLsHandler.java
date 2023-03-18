package com.pdfs.server.fshander;

import com.pdfs.normalfs.File;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.server.HttpRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FsWebLsHandler {
    Logger log = LoggerFactory.getLogger(FsReadHandler.class);

    NormalFs fs;


    public FsWebLsHandler(NormalFs fs) {
        this.fs = fs;
    }


    public HttpRsp webLsHandler(String url, String method, InputStream inputStream) throws IOException {
        String path = url.substring("/fsapi/webls/".length() - 1);
        if (!path.endsWith("/")) {
            return new HttpRsp(400, path + " is not a dir  ");
        }

        try {
            List<File> ls = fs.ls(path);

            String form = """
                     <html>
                        <header>
                           <meta charset="UTF-8">
                        </header>
                        <span>fileName: </span>
                        <br/>
                        <input type="text" id="filename" value="_INNER_README.txt" />
                        <br/><br/>
                        <span>fileContent: </span>
                        <br/>
                        <textarea id="about" style="width:100%;height:100px"></textarea>
                        <br/><br/>
                        <button id='create' onclick="createFile()"> create or update file </button>
                        <br/> <br/>
                        <input type="file" id="file-uploader">
                        
                        <br/>
                        <a href="/">Click Here To Main Page</a>
                        
                        <hr/>
                        
                        <script>
                            function httpPost(url,body,onload){
                                xmlobj = new XMLHttpRequest();
                                xmlobj.open("POST", url, true); //调用weather.php
                                xmlobj.setRequestHeader("cache-control","no-cache");
                                xmlobj.setRequestHeader("contentType","text/html;charset=uft-8") //指定发送的编码
                                xmlobj.setRequestHeader("Content-Type", "application/text;");  //设置请求头信息
                                xmlobj.onreadystatechange = function(x){
                                    if(x.target.readyState==4){
                                        onload(x.target)
                                    }
                                }
                                xmlobj.send(body); //设置为发送给服务器数据
                            }
                        
                            defaultValue = " 1. choose a good filename\\n 2. write something here\\n 3. click 'create or update file' button"
                            document.getElementById("about").value = defaultValue
                            function createFile(){
                                var data=document.getElementById("about").value;
                                var filename = location.pathname.substr(12) + document.getElementById("filename").value;
                                document.getElementById("create").outerHTML='<span id="create">please wait......</span>'
                                httpPost(`/fsapi/writeBig/${data.length}/0`+filename,data,function(x){
                                    if(x.status!=200){
                                        alert(x.response)
                                        document.getElementById("create").outerHTML='<button id="create" onclick="createFile()"> create or update file </button>'
                                    }else{
                                        location.reload();
                                    }
                                })
                            }
                            
                            
                            // 上传文件
                            const fileUploader = document.getElementById('file-uploader');
                            fileUploader.addEventListener('change', (event) => {
                              const file = event.target.files[0];
                              console.log('file=', file);
                              
                              size = 1<<20 // 1MB
                              const fileChunkList = []
                              let cur = 0
                              while (cur < file.size) {
                                fileChunkList.push({
                                  file: file.slice(cur, cur + size),
                                })
                                cur += size
                              }
                              console.log(fileChunkList)
                                
                              second = Date.parse(new Date())
                              totalSend = 0
                              speed = '计算中'
                              uploadFunc = function(index){
                                  currendSecond = Date.parse(new Date())
                                  totalSend = totalSend+1
                                  if(currendSecond > second+1000){
                                      speed = size*totalSend * 1000.0/ (currendSecond - second) 
                                      
                                      if(speed<=1024){
                                         speed = `${speed}B/S`
                                      }else if(speed<=1024*1024){
                                         speed = `${speed/1024}KB/S`
                                      }else {
                                         speed = `${speed/1024/1024}MB/S`
                                      }
                                      second = currendSecond
                                      totalSend = 0
                                  }
                                  
                                  rate = index/(file.size)*size
                                  document.getElementById("file-uploader").outerHTML=`<p id='file-uploader'>上传中：${Math.floor(rate * 10000) / 100}% 速度： ${speed}</p>`
                                  reader = new FileReader()
                                  reader.onload = function(){
                                      httpPost(`/fsapi/writeBig/${file.size}/${index}/${file.name}`,reader.result,function(x){
                                          if(x.status!=200){
                                              alert(x.response)
                                              location.reload();
                                          }else if(index+1<fileChunkList.length){
                                               uploadFunc(index+1)
                                          }else{
                                               location.reload();
                                          }
                                      })
                                  }
                                  reader.readAsArrayBuffer(fileChunkList[index].file)
                              }
                              
                              uploadFunc(0)

                            });
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
                                return String.format("<a href='/fsapi/read%s'>%s </a>[repete on %s]<br><br>"
                                        , o.name, o.name, String.join(",", o.getGroups()));
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
            HttpRsp rsp = new HttpRsp(200, body);
            return rsp;
        } catch (FileNotFoundException e) {
            return new HttpRsp(404, "Could Not Found " + path);
        } catch (Exception e) {
            log.error("", e);
            return new HttpRsp(500, "Server ERROR ");
        }
    }
}
