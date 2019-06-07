package com.stylefeng.guns.rest.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 连接ftp获取文件的工具类
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ftp")
public class FTPUtil {

    //需要:地址,端口,用户名,密码
    private String hostName;
    private Integer port;
    private String userName;
    private String password;


    //初始化ftpClient对象默认为null
    private FTPClient ftpClient =null;

    //1.初始化FTPClient
    private void initFTPClient() {
        try {
            //1,创建对象
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("utf-8");
            ftpClient.connect(hostName,port);
            ftpClient.login(userName,password);
        } catch (Exception e) {
            log.error("初始化失败", e);
        }
    }

    //2.输入路径,然后将路径里的文件转换成字符串返还给我们
    public String getFileStrByAddress(String fileAddress) {
        //以缓存形式快速读取内存中字符流
        BufferedReader bufferReader = null;
        try {
            //初始生命FTPClient才能使用
            initFTPClient();
            //快速读取文件字符流,以行的形式
            bufferReader = new BufferedReader(
                    //将文件字节流转换为文件字符流
                    new InputStreamReader(
                            //从json文件地址获取json文件的字节流
                          ftpClient.retrieveFileStream(fileAddress)));
            //更好的操控String字符串的类
            StringBuffer stringBuffer = new StringBuffer();
            //死循环,一直读
            while (true) {
                //定bufferedReader的readLine方法一行一行读
                String readLine = bufferReader.readLine();
                //如果读到的返回结果是null,就跳出结束循环,
                if (readLine == null) {
                    break;
                }
                //每读一行,添加到stringBuffer中,StringBuffer提供更方便快捷的增删字符串的方法
                stringBuffer.append(readLine);
            }
            //ftpClient退出
            ftpClient.logout();
            //返回字符串,首先要将stringbuffer转换成string
            return stringBuffer.toString();
        } catch (Exception e) {
            log.error("获取文件信息失败", e);

        } finally {
            try {
                //关闭读取的流,这个流关了,里面的所有流都关了,因为他们是一起的
                bufferReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {

        FTPUtil ftpUtil = new FTPUtil();
        String fileStrByAddress = ftpUtil.getFileStrByAddress("seats/123214.json");

        System.out.println(fileStrByAddress);
    }

}

