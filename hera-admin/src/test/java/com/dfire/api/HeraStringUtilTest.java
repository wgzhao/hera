package com.dfire.api;

import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.common.util.StringUtil;
import com.dfire.core.util.CronParse;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:29 2018/3/22
 * @desc
 */
public class HeraStringUtilTest {

    @Test
    public void md5() {
        System.out.println(StringUtil.EncoderByMd5("123456"));

    }

    @Test
    public void parseDate() {
        String s = RenderHierarchyProperties.render("${zdt.add(5,-2).format(\"yyyy-MM-dd\")}");
        System.out.println(s);
        s = RenderHierarchyProperties.render("${zdt.addDay(100).format(\"yyyyMMdd\")}");
        System.out.println(s);
        s = RenderHierarchyProperties.render("${yesterday}");
        System.out.println(s);
        System.out.println(RenderHierarchyProperties.render("${zdt.format(\"yyyyMMddHHmmss\")}"));
    }

    @Test
    public void testFilePath() {
        File file = new File("1.txt");
        System.out.println(file.getAbsolutePath());
    }

    @Test
    public void testDateFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd][dd/MM/yyyy][MM-dd-yyyy]");
        LocalDate.parse("2018-09-23", formatter);
        System.out.println(LocalDate.parse("2018-09-23", formatter));
        System.out.println(LocalDate.parse("09-23-2018", formatter));
        System.out.println(LocalDate.parse("23/09/2018", formatter));
        System.out.println(ActionUtil.getCurrActionVersion());
    }

    @Test
    public void testBoolean() {
        System.out.println(System.getenv());
        Map map = new HashMap(System.getenv());
        System.out.println(map.size());
    }

    @Test
    public void testDateUtil() {
        System.out.println(ActionUtil.getCurrActionVersion());

        String currString = ActionUtil.getNextDayString().getSource();
        Date nextDay = ActionUtil.getNextDayString().getTarget();
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println(currString);
        System.out.println(dfDate.format(nextDay));

        List<String> list = new ArrayList<>();
        String cron = "0 0 2 * * ?";
        String cronDate = dfDate.format(nextDay);
        boolean isCronExp = CronParse.Parser(cron, cronDate, list);
        System.out.println("isCronExp : " + isCronExp);
        System.out.println(list.size());

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd0000000000");
        String actionDate = dateFormat.format(now);
        System.out.println(actionDate);
    }


    @Test
    public void filePathTest() {
        String path = this.getClass().getClassLoader().getResource("").getPath();
        if (path != null) {
            System.out.println(path);
        }

    }

    @Test
    public void getDate() {
        System.out.println(ActionUtil.longToDate(System.currentTimeMillis()));
    }

    @Test
    public void fileNameSplit() {
        String fileName = "sqoop.sh";
        String prefix = StringUtils.substringBefore(fileName, ".");
        System.out.println(prefix);
        String suffix = StringUtils.substringAfter(fileName, ".");
        System.out.println(suffix);
    }
}
