package com.dfire.common.vo;

import com.dfire.common.constants.Constants;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:24 2018/1/12
 * @desc 任务运行过程中的日志记录
 */

@Data
@Builder
public class LogContent
{

    private static final int COUNT = 10000;
    @Getter private int lines;
    @Setter private StringBuffer content;

    public void appendConsole(String log)
    {

        if (lines < COUNT) {
            //空日志不记录
            if (StringUtils.isBlank(log)) {
                return;
            }

            content.append(log).append(Constants.NEW_LINE);

            if (++lines >= COUNT) {
                content.append("控制台输出信息过多，停止记录，建议您优化自己的Job");
            }
        }
    }

    public void appendHera(String log)
    {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(log).append(Constants.NEW_LINE);
        }
    }

    public void append(String log)
    {
        lines++;
        if (content == null) {
            content = new StringBuffer();
        }
        if (lines < COUNT) {
            content.append(log).append(Constants.NEW_LINE);
        }
    }

    public void appendHeraException(Exception e)
    {
        if (e == null) {
            return;
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        appendHera(sw.toString());
    }

    public String getContent()
    {
        return content.toString();
    }
}
