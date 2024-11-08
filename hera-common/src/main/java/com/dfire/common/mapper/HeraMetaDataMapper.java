package com.dfire.common.mapper;

import com.dfire.common.entity.HeraMetaData;
import com.dfire.common.entity.vo.HeraMetaDataVo;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author lenovo
 * @description
 * @date 2020/5/19 19:54
 */
public interface HeraMetaDataMapper {

    @Select({"<script> " +
            "SELECT * FROM hera_metadata_data where 1=1 " +
            "<if test=\"dt!=null and dt!=''\"> and  date(create_time) &gt;= #{startTime} and date(create_time) &lt;= #{endTime} </if> " +
            "<if test=\"queryData!=null and queryData!=''\" >" +
            "and (concat(rule_name,rule_stat,rule_sql,biz_desc,biz_aff,creator,mender) like  CONCAT('%',#{queryData},'%') " +
            ")  " +
            "</if> " +
            "<if test=\"field!=null and field!=''\"> order by ${field} ${order}</if>" +
            "LIMIT #{page},#{limit} " +
            "</script>"})
    @Lang(HeraSelectLangDriver.class)
    List<HeraMetaData> selectHeraMetaDataList(HeraMetaDataVo heraMetaDataVo);


    @Select({"<script> " +
            "SELECT count(*) FROM hera_metadata_data where 1=1 " +
            "<if test=\"dt!=null and dt!=''\"> and  date(create_time) &gt;= #{startTime} and date(create_time) &lt;= #{endTime} </if> " +
            "<if test=\"queryData!=null and queryData!=''\" >" +
            "and (concat(rule_name,rule_stat,rule_sql,biz_desc,biz_aff,creator,mender) like  CONCAT('%',#{queryData},'%') " +
            ")  " +
            "</if> " +
            "</script>"})
    @Lang(HeraSelectLangDriver.class)
    int selectHeraMetaDataCount(HeraMetaDataVo heraMetaDataVo);


    @Update("update hera_metadata_data (#{heraMetaData}) where uid = #{uid}")
    @Lang(HeraUpdateLangDriver.class)
    int updateMetaData(HeraMetaData heraMetaData);

    @Update("insert hera_metadata_data (#{heraMetaData})")
    @Lang(HeraUpdateLangDriver.class)
    int insertHeraMetaData(HeraMetaData heraMetaData);
}
