package com.dfire.monitor.mapper;

import com.dfire.common.entity.HeraActionAll;
import com.dfire.common.entity.HeraJob;
import com.dfire.monitor.domain.ActionTime;
import com.dfire.monitor.domain.JobHistoryVo;
import com.dfire.monitor.domain.JobStatusNum;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:05 2018/8/14
 * @desc
 */
public interface JobManagerMapper {

    /**
     * 今日任务详情
     *
     * @param status
     * @return
     */

 /*   @Select("select " +
            " his.job_id,job.name as job_name,job.description,his.start_time,his.end_time,his.execute_host,his.status,his.operator,count(*) as times " +
            " from" +
            " (select job_id,start_time start_time,end_time,execute_host,status,operator from hera_action_history " +
            " where action_id >= CURRENT_DATE () * 10000000000  and status = #{status,jdbcType=VARCHAR}) his " +
            " left join hera_job job on his.job_id = job.id" +
            " group by his.job_id,job.name,job.description,his.start_time,his.end_time,his.execute_host,his.status,his.operator" +
            " order by job_id")*/

   /* @Select("select  his.job_id,job.name as job_name,job.description,his.start_time,his.end_time,his.execute_host,his.status,his.operator," +
            "hih.times  from (select job_id,start_time start_time,end_time,execute_host,status,operator from hera_action_history  " +
            "where action_id >= CURRENT_DATE () * 10000000000   and status = #{status,jdbcType=VARCHAR} ) his left join hera_job job  " +
            "on his.job_id = job.id  left join (select job_id,count(1) as times  from hera_action_history  " +
            "where action_id >= CURRENT_DATE () * 10000000000  and status = #{status,jdbcType=VARCHAR} group by 1) " +
            "hih on his.job_id=hih.job_id group by his.job_id,job.name,job.description,his.start_time,his.end_time,his.execute_host," +
            "his.status,his.operator,hih.times order by job_id")*/
    /*@Select("select round(TIMESTAMPDIFF(second,his.start_time,his.end_time),2) run_time, his.job_id,job.name as job_name,job.description,his.start_time,his.end_time,his.execute_host,his.status,his.operator," +
            "hih.times  from (select job_id,start_time start_time,end_time,execute_host,status,operator from hera_action_history  " +
            "where action_id >= CURRENT_DATE () * 10000000000   and status = #{status,jdbcType=VARCHAR} ) his left join hera_job job  " +
            "on his.job_id = job.id  left join (select job_id,count(1) as times  from hera_action_history  " +
            "where action_id >= CURRENT_DATE () * 10000000000  and status = #{status,jdbcType=VARCHAR} group by 1) " +
            "hih on his.job_id=hih.job_id group by his.job_id,job.name,job.description,his.start_time,his.end_time,his.execute_host," +
            "his.status,his.operator,hih.times order by job_id , start_time desc ;")
    List<JobHistoryVo> findAllJobHistoryByStatus(String status);*/

   /* @Select({"<script> " +
            "SELECT * FROM hera_data_discovery where 1=1 " +
            " <if test=\"dt!=null and dt!=''\"> and  dt=#{dt} </if> " +
            "<if test=\"queryData!=null and queryData!=''\" >" +
            "and (table_comment like  CONCAT('%',#{queryData},'%') or table_name like CONCAT('%',#{queryData},'%'))  " +
            "</if> " +
            "<if test=\"field!=null and field!=''\"> order by ${field} ${order}</if>" +
            "LIMIT #{page},#{limit} " +
            "</script>"})*/
    @Select({
            "<script> select round(TIMESTAMPDIFF(second,his.start_time,his.end_time),2) run_time, his.job_id,job.name as job_name,job.description,his.start_time,his.end_time "
                    + " ,his.execute_host, his.status ,his.operator "
                    + " ,j.times  "
                    + " ,CAST(timestampdiff(SECOND, his.start_time,CASE WHEN his.end_time IS NOT NULL THEN his.end_time WHEN his.status='running' THEN NOW() END)/60.0 AS decimal(10,1))  AS durations  "
                    + " ,job.group_id as groupId,grp.name as groupName"
                    + " ,job.dependencies"
                    + " FROM "
                    + " (SELECT job_id,MAX(`id`) as id_max,count(1) as times   "
                    + " FROM hera_action_history   "
                    + " WHERE (start_time &gt;= CAST(#{dt,jdbcType=VARCHAR} AS date) and  start_time &lt; ADDDATE(CAST(#{dt,jdbcType=VARCHAR} AS date) ,1) ) "
                    + " GROUP BY job_id ) j   "
                    + " left join hera_action_history his on j.job_id=his.job_id and j.id_max=his.`id`   "
                    + " left join hera_job job on j.job_id = job.`id` "
                    + " left join hera_group grp on job.group_id=grp.`id` "
                    + " where ( his.status = #{status,jdbcType=VARCHAR}  or 'all' =  #{status,jdbcType=VARCHAR} ) "
                    + "<if test=\"operator!=null and operator!=''\"> and his.operator=#{operator} </if>"
                    + " ORDER BY his.start_time DESC, grp.name,job.name </script>"
    })
    List<JobHistoryVo> findAllJobHistoryByStatus(@Param("status") String status, @Param("dt") String dt, @Param("operator") String operator);


    /**
     * 任务运行时长top10
     *
     * @param map
     * @return
     */


    @Select("select job_id,action_id, (select hj.name from hera_job hj where hj.id=job_id) as job_name,timestampdiff(SECOND,start_time,end_time)/60 as job_time " +
            "from hera_action_history " +
            "where action_id >= #{startDate} and action_id < #{endDate} " +
            "order by job_time desc limit #{limitNum}")
    List<ActionTime> findJobRunTimeTop10(Map<String, Object> map);

    /**
     * 任务昨日运行时长
     *
     * @param jobId
     * @param id
     * @return
     */
    @Select(" select max(timestampdiff(SECOND,start_time,end_time)/60) from hera_action_history" +
            "        WHERE  job_id = #{jobId}" +
            "        AND left(action_id,8) = #{id}")
    Integer getYesterdayRunTime(@Param("jobId") Integer jobId, @Param("id") String id);

    /**
     * 按照运行状态汇总,初始化首页饼图
     *
     * @return
     */
    @Select(" select status,count(1) as num" +
            "        from" +
            "        (" +
            "        select job_id,substring_index(group_concat(status order by start_time desc),\",\",1) as status" +
            "        from hera_action_history" +
            "        where action_id>=CURRENT_DATE () * 10000000000" +
            "        group by job_id" +
            "        ) t" +
            "        group by status")
    List<JobStatusNum> findAllJobStatus();


    /**
     * 按照日期查询任务明细
     *
     * @return
     */

    @Select("select status, count(1) as num " +
            "from (" +
            "select job_id, status  from hera_action where id >= #{startDate} and id < #{endDate} and status is not null group by job_id,status " +
            ") tmp group by status")
    List<JobStatusNum> findJobDetailByDate(@Param("startDate") long startDate, @Param("endDate") long endDate);

    /**
     * 按照status查询任务明细
     *
     * @param status
     * @return
     */
    @Select(" select count(1) num ,status, LEFT(start_time,10) curDate " +
            "        from  hera_action_history " +
            "        where " +
            "        action_id >= #{lastDate} " +
            "        and status = #{status,jdbcType=VARCHAR} " +
            "        GROUP BY LEFT(start_time,10),status")
    List<JobStatusNum> findJobDetailByStatus(@Param("lastDate") long lastDate, @Param("status") String status);


    /**
     * 自己添加 查找关闭的任务详情
     *
     * @param status
     * @return
     */
    @Select(" select * from hera_job WHERE  auto = #{auto}")
    //@Lang(HeraSelectLangDriver.class)
    List<HeraJob> findStopJobHistoryByStatus(@Param("auto") String status);
    //@Param("id") String id


    //findNoRunJobHistoryByStatus
    @Select("SELECT * FROM hera_action WHERE STATUS IS NULL AND LEFT(id, 12) <= DATE_FORMAT(DATE_ADD(NOW(),INTERVAL 10 MINUTE),'%Y%m%d%H%i') AND LEFT(id, 8)>=DATE_FORMAT(CAST(#{dt,jdbcType=VARCHAR} AS DATE),'%Y%m%d') AND auto='1'  ORDER BY job_id,id ")
    //@Lang(HeraSelectLangDriver.class)
    List<HeraActionAll> findNoRunJobHistoryByStatus(@Param("dt") String dt);


/*    @Select(
            "select round(TIMESTAMPDIFF(second,his.start_time,his.end_time),2) run_time, his.job_id,job.name as job_name,job.description,his.start_time,his.end_time "
                    + " ,his.execute_host, his.status ,his.operator "
                    + " ,j.times  "
                    + " ,CAST(timestampdiff(SECOND, his.start_time,CASE WHEN his.end_time IS NOT NULL THEN his.end_time WHEN his.status='running' THEN NOW() END)/60.0 AS decimal(10,1))  AS durations  "
                    + " ,job.group_id as groupId,grp.name as groupName"
                    + " FROM "
                    + " (SELECT job_id,MAX(`id`) as id_max,count(1) as times   "
                    + " FROM hera_action_history   "
                    + " WHERE (start_time>=CAST(#{dt,jdbcType=VARCHAR} AS date) and  start_time< ADDDATE(CAST(#{dt,jdbcType=VARCHAR} AS date) ,1) ) "
                    + " and ( status is NULL ) "
                    + " GROUP BY job_id ) j   "
                    + " left join hera_action_history his on j.job_id=his.job_id and j.id_max=his.`id`   "
                    + " left join hera_job job on j.job_id = job.`id` "
                    + " left join hera_group grp on job.group_id=grp.`id` "
                    + " ORDER BY his.start_time DESC, grp.name,job.name"
    )
    List<JobHistoryVo> findNoRunJobHistoryByStatus(@Param("status") String status,@Param("dt") String dt);*/
    //List<HeraAction> findNoRunJobHistoryByStatus(String status);


}


