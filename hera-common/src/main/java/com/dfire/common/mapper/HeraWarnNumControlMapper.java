package com.dfire.common.mapper;

import com.dfire.common.entity.HeraAdvice;
import com.dfire.common.entity.HeraWarnNumControl;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
public interface HeraWarnNumControlMapper {

    @Insert("replace into hera_warn_num_control (#{heraWarnNumControl})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insert(HeraWarnNumControl heraWarnNumControl);


    @Select("select * from hera_warn_num_control where job_id=${jobId} and warn_type=${warnType}")
    @Lang(HeraSelectLangDriver.class)
    HeraWarnNumControl getOne(HeraWarnNumControl heraWarnNumControl);

}
