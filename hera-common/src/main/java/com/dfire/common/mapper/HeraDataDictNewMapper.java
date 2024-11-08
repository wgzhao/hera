package com.dfire.common.mapper;

import com.dfire.common.entity.HeraDataDict;
import com.dfire.common.entity.vo.HeraDataDictNew;
import com.dfire.common.entity.vo.HeraDataDictVo;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 23:55 2017/12/29
 * @desc
 */
public interface HeraDataDictNewMapper {


    @Select("SELECT * FROM ${tableVariable}")
    @Lang(HeraSelectLangDriver.class)
    List<HeraDataDictNew> selectHeraDataDictNewList(HeraDataDictNew HeraDataDictNew);


    @Select("SELECT * FROM ${tableVariable} where database_name=#{databaseName} and table_name=#{tableName} order by column_rank")
    @Lang(HeraSelectLangDriver.class)
    List<HeraDataDictNew> selectHeraDataDictNewFields(HeraDataDictNew heraDataDictNew);

    @Update("update hera_data_dict (#{heraDataDict}) where table_schema = #{tableSchema} and table_name2=#{tableName2} and column_name=#{columnName}")
    @Lang(HeraUpdateLangDriver.class)
    int updateHeraDataDict(HeraDataDict heraDataDict);

    @Update("update hera_data_dict set table_status=#{tableStatus}," +
            "update_time=#{updateTime}," +
            "tb_owner=#{tbOwner}," +
            "business=#{business}," +
            "table_name1=#{tableName1} where table_schema = #{tableSchema} and table_name2=#{tableName2}")
    @Lang(HeraUpdateLangDriver.class)
    int updateHeraDataDictTableStatus(HeraDataDict heraDataDict);

    @Select("select count(*) from hera_data_dict where  table_schema = #{tableSchema} and table_name2=#{tableName2}")
    int selectHeraDataDictTableCount(HeraDataDictVo heraDataDict);
}
