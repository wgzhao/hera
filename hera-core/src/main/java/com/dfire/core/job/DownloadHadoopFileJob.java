package com.dfire.core.job;


import java.util.ArrayList;
import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午5:18 2018/5/1
 * @desc
 */
public class DownloadHadoopFileJob extends ProcessJob {

    private final String hadoopPath;
    private final String localPath;

    public DownloadHadoopFileJob(JobContext jobContext, String hadoopPath, String localPath) {
        super(jobContext);
        this.hadoopPath = hadoopPath;
        this.localPath = localPath;
    }

    @Override
    public List<String> getCommandList() {
        List<String> commands = new ArrayList<>();
        commands.add("hadoop fs -copyToLocal " + hadoopPath + " " + localPath);
        //格式转换
        return commands;
    }
}
