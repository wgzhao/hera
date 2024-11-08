package com.dfire.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 驼峰命名转换
 *
 * @author 47475
 */
public class HumpTool {
    private static final Pattern linePattern = Pattern.compile("_(\\w)");
    private static final Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 下划线转驼峰 ,大写自动转小写<br />
     * 例如：USER_NAME->userName<br />
     * USERNAME->userName<br />
     * USER_NAME->userName<br />
     * fParentNoLeader->fParentNoLeader
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 驼峰转下划线,大写自动转小写<br />
     * 例如： USER_NAME->user_name USERNAME->userName<br />
     * f_parent_no_leader->f_parent_no_leader
     */
    public static String humpToLine(String str) {

        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}