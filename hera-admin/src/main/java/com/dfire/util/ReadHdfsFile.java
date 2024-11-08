package com.dfire.util;

import com.dfire.common.config.HeraGlobalEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by E75 on 2019/10/28.
 */
@Slf4j
public class ReadHdfsFile {

    static String hdfsUploadPath = HeraGlobalEnvironment.getHdfsUploadPath();
    //"hdfs://192.168.153.11:9000"
    static String Fs = hdfsUploadPath.split("/hera/")[0];

    public static String hdfsCat(String hdfsFilePath) {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", Fs);
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        FileSystem fs = null;
        FSDataInputStream in = null;
        BufferedReader d = null;
        StringBuilder sb = new StringBuilder();
        try {
            fs = FileSystem.get(conf);
            Path remotePath = new Path(hdfsFilePath);
            in = fs.open(remotePath);
            d = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = d.readLine()) != null) {
                sb.append(line).append("\n");
            }
            d.close();
            in.close();
            fs.close();
        } catch (IOException e) {
            log.error("读取hdfs文件失败", e);
        } finally {
            try {
                if (d != null) d.close();
                if (in != null) in.close();
                if (fs != null) fs.close();
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
        }
        return sb.toString();
    }
}
