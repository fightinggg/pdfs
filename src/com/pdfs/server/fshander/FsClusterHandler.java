package com.pdfs.server.fshander;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.reliable.PolinDistributeFs;
import com.pdfs.reliable.PolinDistributeFsCluster;
import com.pdfs.server.HttpRsp;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.pdfs.utils.ParamParse.parse;

@Slf4j
public class FsClusterHandler {
    PolinDistributeFs fs;


    public FsClusterHandler(NormalFs fs) {
        this.fs = (PolinDistributeFs) fs;
    }

    public HttpRsp get(String url, String method, InputStream inputStream) {
        try {
            List<PolinDistributeFsCluster.NetFsDisk> disks = fs.cluster.getDisks();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            return new HttpRsp(200, objectMapper.writeValueAsBytes(disks));
        } catch (Exception e) {
            log.error("", e);
            return new HttpRsp(500, "Server ERROR");
        }
    }

    public HttpRsp post(String url, String method, InputStream inputStream) {
        try {
            byte[] src = inputStream.readAllBytes();
            String[] s = new String(src).split(" ");
            Map<String, String> map = parse(s);
            if (fs.cluster.addConfig(map)) {
                return new HttpRsp(200, "OK");
            } else {
                return new HttpRsp(200, "NO FS Has Bean Create");
            }
        } catch (Exception e) {
            log.error("", e);
            return new HttpRsp(500, "Server ERROR");
        }
    }

    public HttpRsp web(String url, String method, InputStream inputStream) {
        return new HttpRsp(200, """
                <html>
                        <header>
                           <meta charset="UTF-8">
                        </header>
                        <br/>
                        <textarea id="about" style="width:100%;height:100px"></textarea>
                        <br/><br/>
                        <button id='create' onclick="create()"> add a new file system </button>
                        <br/> <br/>
                        
                        <span>
                        Your Can Create a File System like This: <br/>
                          Create a local File System: -group g1 -name n1 -type local -local_path local/p1 <br/>
                          Create a GitHub File System: -group g1 -name n1 -type github_api -github_token ** -github_reponame ** -github_username ** \s <br/>
                        </span>
                        
                        <hr/>

                        <a href="/fsapi/cluster">Click Here To See All File System</a>
                        <br/>
                        <a href="/">Click Here To Main Page</a>
                 
                        
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
                        
                            function create(){
                                var data=document.getElementById("about").value;
                                document.getElementById("create").outerHTML='<span id="create">please wait......</span>'
                                httpPost(`/fsapi/cluster`,data,function(x){
                                    if(x.status!=200){
                                        alert(x.response)
                                        document.getElementById("create").outerHTML='<button id="create" onclick="createFile()"> add a new file system </button>'
                                    }else{
                                        alert(x.response)
                                        location.reload();
                                    }
                                })
                            }
                            
                        </script>
                    </html>
                                
                                
                """);
    }
}
