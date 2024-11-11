package com.dfire.util;

import com.dfire.common.config.HeraGlobalEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by E75 on 2019/10/28.
 */
@Slf4j
public class ReadHdfsFile
{
    static String hdfsUploadPath = HeraGlobalEnvironment.getHdfsUploadPath();

    public static String hdfsCat(String hdfsFilePath)
    {
        try {
            String tempFile = Files.createTempFile("/tmp", ".dat").toString();
            String command = "hdfs dfs -cat " + hdfsUploadPath + "/" + hdfsFilePath + " 2>/dev/null > " + tempFile;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            // read all output stream into a string
            return String.join("\n", Files.readAllLines(Paths.get(tempFile)));
        }
        catch (IOException e) {
            log.error("读取hdfs文件失败", e);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
