package com.dfire.common.mapper;

import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/8/1
 */
public interface HeraJobMonitorMapper {


    @Insert("insert into hera_job_monitor values(#{jobId}, #{userIds})")
    Integer insert(HeraJobMonitor monitor);


    @Update("update hera_job_monitor set user_ids = #{userIds} where job_id = #{jobId}")
    Integer deleteMonitor(HeraJobMonitor monitor);


    @Update("update hera_job_monitor set user_ids = concat(user_ids,#{userIds}) where job_id = #{jobId}")
    Integer insertUser(HeraJobMonitor monitor);


    @Select("select * from hera_job_monitor where job_id = #{jobId} limit 1")
    HeraJobMonitor findByJobId(Integer jobId);

    @Select("select * from hera_job_monitor where user_ids like CONCAT('%',#{userId},'%')")
    List<HeraJobMonitor> findByUserId(Integer userId);


    //自己添加

    @Update("update hera_job_monitor set user_ids = #{userIds} where job_id = #{jobId}")
    Integer insertUsers(@Param("userIds") String userIds, @Param("jobId") Integer jobId);


    @Update("delete from hera_job_monitor  where job_id = #{jobId}")
    boolean removeAllMonitor(Integer jobId);
}
