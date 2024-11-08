package com.dfire.common.mapper;

import com.dfire.common.entity.HeraDataDiscovery;
import com.dfire.common.entity.HeraDataDiscoveryNew;
import com.dfire.common.entity.vo.HeraDataDiscoveryVo;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:55 2017/12/29
 * @desc
 */
public interface HeraDataDiscoveryNewMapper {
@Select("<script>" +
        "select p1.database_name\n" +
        "     , p1.table_name\n" +
        "     , coalesce(p2.table_comment, '') as table_comment\n" +
        "     , p1.change_type\n" +
        "     , p1.change_detail\n" +
        "     , p2.is_middle_base\n" +
        "     , p3.is_hive_base\n" +
        "     , p1.dt\n" +
        "from (\n" +
        "         select database_name\n" +
        "              , table_name\n" +
        "              , change_type\n" +
        "              , change_detail\n" +
        "              , dt\n" +
        "         from ${tableVar}\n" +
        "         where 1 = 1\n" +
        "           <if test=\"dt!=null and dt!=''\">and dt &gt;= #{startTime} and dt &lt;= #{endTime}</if>\n" +
        "     ) p1\n" +
        "         left join\n" +
        "     (\n" +
        "         select database_name\n" +
        "              , table_name\n" +
        "              , table_comment\n" +
        "              , 1 as is_middle_base\n" +
        "         from dwd_herafunc_middlebase_dictionary_df\n" +
        "         group by database_name, table_name, table_comment\n" +
        "     ) p2\n" +
        "     on p1.database_name = p2.database_name and p1.table_name = p2.table_name\n" +
        "         left join\n" +
        "     (\n" +
        "         select case\n" +
        "                    when table_name rlike '_(df|di)_0db$' then replace(replace(table_name, '_di_0db', ''), '_df_0db', '')\n" +
        "                    when table_name rlike '_(df|di|0db)$' then replace(replace(replace(table_name, '_df', ''), '_di', ''), '_0db', '')\n" +
        "             end  as join_condition\n" +
        "              , 1 as is_hive_base\n" +
        "         from dwd_herafunc_hive_dictionary_df\n" +
        "         where database_name = 'ods'\n" +
        "           and table_name not rlike '(log|1db)$'\n" +
        "         group by case\n" +
        "                      when table_name rlike '_(df|di)_0db$' then replace(replace(table_name, '_di_0db', ''), '_df_0db', '')\n" +
        "                      when table_name rlike '_(df|di|0db)$' then replace(replace(replace(table_name, '_df', ''), '_di', ''), '_0db', '')\n" +
        "                      end\n" +
        "     ) p3\n" +
        "     on concat('ods_', p1.database_name, '_', p1.table_name) = p3.join_condition\n" +
        "where 1 = 1\n" +
        "  <if test=\"queryData!=null and queryData!=''\">and CONCAT_WS(',', p1.database_name, p1.table_name, p2.table_comment, p1.change_type, p1.change_detail) like CONCAT('%', #{queryData}, '%')</if>\n" +
        "  <if test=\"isMiddleBase!=null\">and p2.is_middle_base = #{isMiddleBase}</if>\n" +
        "  <if test=\"isHiveBase!=null\">and p3.is_hive_base = #{isHiveBase}</if>\n" +
        "  <if test=\"field!=null and field!=''\"> order by ${field} ${order}</if>\n" +
        "  <if test=\"field==null or field ==''\"> order by dt desc </if>\n" +
        "</script>")
@Lang(HeraSelectLangDriver.class)
List<HeraDataDiscoveryNew> selectHeraDataDiscoveryNewList(HeraDataDiscoveryNew heraDataDiscoveryNew);
}
