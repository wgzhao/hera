package com.dfire.controller;

import com.dfire.common.config.HeraGlobalEnvironment;
import com.dfire.common.logs.MonitorLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心
 */
@Controller
@RequestMapping("/editUploadFile")
public class EditUploadFileController
        extends BaseHeraController
{

   /* @RequestMapping(value = "/script2HdfsFile", method = RequestMethod.GET)
    @ResponseBody
    public String script2HdfsFileUUU(String name, String selfScript) {
        System.err.println("name  : " + name);
        System.err.println("selfScript  : " + selfScript);
        String heraFileName = name;
        String heraFilecontent = selfScript;
        System.err.println("heraFileName + heraFilecontent : " + heraFileName + "\t" + heraFilecontent);
        //文件上传逻辑
        String newFileName = "";
        //  File file = null;
        String prefix = StringUtils.substringBefore(heraFileName, ".");
        String suffix = StringUtils.substringAfter(heraFileName, ".");
        newFileName = prefix + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "." + suffix;
        //  newFilePath = HeraGlobalEnvironment.getWorkDir() + File.separator + newFileName;
        //  file = new File(newFilePath);
        //   System.err.println("file : " + file);
        // String absolutePath = file.getAbsolutePath();
        String hdfsUploadPath = HeraGlobalEnvironment.getHdfsUploadPath();

        //     System.err.println("newFileName : " + newFileName);
        //newFileName : test-20190626-132518.sh
        //System.err.println("absolutePath : " + absolutePath);
        //absolutePath : D:\00工作目录\2019-06-13-hera上传文件优化\20190623\hera-2.3.1\55-20190626-132238.sh

        //   System.err.println("hdfsUploadPath : " + hdfsUploadPath);
        //hdfsUploadPath : hdfs://192.168.153.11:9000/hera/hdfs-upload-dir/

        String hdfsFilePath = hdfsUploadPath + newFileName;
        MonitorLog.info("hdfs文件上传路径hdfsFilePath:" + hdfsFilePath);
        //    System.out.println("hdfsFilePath : " + hdfsFilePath);
        //   System.out.println("/editUploadFile/script2HdfsFile.do+++++++++++++++++++++++++");
        return saveAsHdfsFile(hdfsFilePath, heraFilecontent, heraFileName);

        //hdfs://192.168.153.11:9000/hera/hdfs-upload-dir/test-20190626-133311.sh

        //  System.err.println("heraFile : " + heraFile);

        //  return "方法返回的结果";
    }*/

    /**
     * save file to hdfs
     * first save the file to the local path ,then use hdfs command line to upload
     * using hdfs command line to avoid invoke hadoop libraries
     *
     * @param hdfsFilePath
     * @param heraFileContent
     * @param heraFileName
     * @return
     */
    public String saveAsHdfsFile(String hdfsFilePath, String heraFileContent, String heraFileName)
    {
        // create a temporary file and write heraFileContent
        try {
            java.nio.file.Path tempFile = Files.createTempFile("tempFilePrefix", ".tmp");
            Files.write(tempFile, heraFileContent.getBytes());
            // invoke hdfs command lineString
            String hdfsCommand = "hdfs dfs -put -f " + tempFile.toFile().getAbsolutePath() + " " + hdfsFilePath + "/" + heraFileName;
            // run the command
            Process process = Runtime.getRuntime().exec(hdfsCommand);
            process.waitFor();
            MonitorLog.info("写入" + hdfsFilePath + "/" + heraFileName + " 执行结束...");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String suffix = StringUtils.substringAfter(heraFileName, ".");

        if (suffix.equalsIgnoreCase(".sh")) {
//            System.out.println("download[hdfs://"+hpath +" "+heraFileName+"];\n" +
//                    "sh "+heraFileName);
            return "download[hdfs://" + hdfsFilePath + " " + heraFileName + "]\n" +
                    "sh " + heraFileName;
        }
        else if (suffix.equalsIgnoreCase(".sql")) {

            return "download[hdfs://" + hdfsFilePath + " " + heraFileName + "]\n" +
                    "hive -f " + heraFileName;
        }
        else if (suffix.equalsIgnoreCase(".py")) {

//            System.out.println("download[hdfs://"+hpath +" "+heraFileName+"];\n" +
//                    "hive -f "+heraFileName);
            return "download[hdfs://" + hdfsFilePath + " " + heraFileName + "]\n" +
                    "python3 " + heraFileName;
        }
        else {
            return "文件路径" + hdfsFilePath + "\n" +
                    "脚本生成错误";
        }
    }

    @RequestMapping(value = "/return", method = RequestMethod.POST)
    @ResponseBody
    public String script2HdfsFile(String name, String selfScript)
    {
        //     System.err.println("name  : "+name);
        //     System.err.println("selfScript  : "+selfScript);

        //文件上传逻辑
        String newFileName = "";
        //  File file = null;
        String prefix = StringUtils.substringBefore(name, ".");
        String suffix = StringUtils.substringAfter(name, ".");
        newFileName = prefix + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "." + suffix;

        String hdfsUploadPath = HeraGlobalEnvironment.getHdfsUploadPath();

        String hdfsFilePath = hdfsUploadPath + newFileName;
        MonitorLog.info("hdfs文件上传路径hdfsFilePath:" + hdfsFilePath);
        return saveAsHdfsFile(hdfsFilePath, selfScript, name);
    }
}
