package com.dfire.common.mybatis.sqoopTask;

import com.dfire.common.entity.HeraSqoopTask;
import com.google.common.base.CaseFormat;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 仅仅用于heraAction
 *
 * @author: 火锅
 * @time: Created in 上午11:13 2018/5/17
 * @desc
 */
public class HeraSqoopTaskBatchInsertDriver extends XMLLanguageDriver implements LanguageDriver {

    private final Pattern inPattern = Pattern.compile("\\(#\\{(\\w+)\\}\\)");

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {

        Matcher matcher = inPattern.matcher(script);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            StringBuilder tmp = new StringBuilder();
            sb.append(" (");

            for (Field field : HeraSqoopTask.class.getDeclaredFields()) {
                sb.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName())).append(",");
                tmp.append("#{item.").append(field.getName()).append("},");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            tmp.deleteCharAt(tmp.lastIndexOf(","));
            sb.append(") values ");
            script = matcher.replaceAll(sb + " <foreach collection=\"$1\"  index=\"index\"  item=\"item\"  " +
                    "separator=\",\"  > (" + tmp + ")</foreach>");

            script = "<script>" + script + "</script>";
        }
        return super.createSqlSource(configuration, script, parameterType);
    }
}
