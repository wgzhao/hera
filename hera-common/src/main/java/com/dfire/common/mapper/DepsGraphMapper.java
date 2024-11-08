package com.dfire.common.mapper;

import com.dfire.common.entity.ColumnDeps;
import com.dfire.common.entity.FieldInfo;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DepsGraphMapper {
    @Select("select * from hera.table_column_deps where table_name = #{table_name} and field = #{field}")
    @Lang(HeraSelectLangDriver.class)
    List<ColumnDeps> selectNode(@Param("table_name") String table_name, @Param("field") String field);

    @Select("select field from hera.table_column_deps where table_name = #{table_name} group by field")
    @Lang(HeraSelectLangDriver.class)
    List<ColumnDeps> selectNodeWithName(String table_name);


    @Select("select * from hera.table_column_deps")
    @Lang(HeraSelectLangDriver.class)
    List<ColumnDeps> selectAll();

    @Select("select CONCAT_WS('.', database_name ,table_name) as tableName, column_name as field, table_comment as tableComment, column_type as fieldType, column_comment as fieldComment from dwd_herafunc_hive_dictionary_df")
    @Lang(HeraSelectLangDriver.class)
    List<FieldInfo> selectDataDictionaryAll();
}
