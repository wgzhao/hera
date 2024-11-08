package com.dfire.common.mapper;

import com.dfire.common.entity.HeraNginxInfo;
import com.dfire.common.entity.HeraYarnInfoUse;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @ClassName HeraYarnInfoUseMapper
 * @Description TODO
 * @Author lenovo
 * @Date 2019/9/2 17:43
 **/
public interface HeraNginxInfoMapper {
    @Insert("insert into hera_nginx_info (#{heraNginxInfo})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Integer insertHeraNginxInfo(HeraNginxInfo heraNginxInfo);

    @Select("select * from hera_nginx_info where date(create_time)=#{createTime}")
    @Lang(HeraSelectLangDriver.class)
    List<HeraNginxInfo> selectHeraNginxInfoList(HeraNginxInfo heraNginxInfo);
}
