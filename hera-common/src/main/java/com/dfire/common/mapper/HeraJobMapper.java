package com.dfire.common.mapper;

import com.dfire.common.entity.*;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.JobDownDenpends;
import com.dfire.common.mybatis.HeraInsertLangDriver;
import com.dfire.common.mybatis.HeraListInLangDriver;
import com.dfire.common.mybatis.HeraSelectLangDriver;
import com.dfire.common.mybatis.HeraUpdateLangDriver;
import com.dfire.common.mybatis.action.HeraActionBatchUpdateDriver;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:24 2017/12/30
 * @desc
 */
@Component
public interface HeraJobMapper {


    @Insert("insert into hera_job (#{heraJob})")
    @Lang(HeraInsertLangDriver.class)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(HeraJob heraJob);

    @Delete("delete from hera_job where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update hera_job (#{heraJob}) where id = #{id}")
    @Lang(HeraUpdateLangDriver.class)
    Integer update(HeraJob heraJob);

    @Select("select * from hera_job")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJob> getAll();

    @Select("SELECT\n" +
            "\thu.sc_key\n" +
            "FROM\n" +
            "\t(\n" +
            "\t\tSELECT\n" +
            "      `owner`\n" +
            "\t\tFROM\n" +
            "\t\t\thera_job hj\n" +
            "\t\tWHERE\n" +
            "\t\t\tid = #{jobId}\n" +
            "\t) hj\n" +
            "LEFT JOIN hera_user hu \n" +
            "ON hj.`owner`= hu.`name` limit 1")
    HeraUser findScKeyById(Integer jobId);

    @Select("select id,group_id,name,owner from hera_job")
    @Lang(HeraSelectLangDriver.class)
    List<HeraJob> selectAll();

    @Select("select * from hera_job where id = #{id}")
        //@Lang(HeraSelectLangDriver.class)
    HeraJob findById(Integer id);

    @Select("select * from hera_job where id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraJob> findByIds(@Param("list") List<Integer> list);

    @Select("select * from hera_job where group_id = #{groupId}")
    List<HeraJob> findByPid(Integer groupId);


    @Update("update hera_job set auto = #{status} where id = #{id}")
    Integer updateSwitch(@Param("id") Integer id, @Param("status") Integer status);


    @Select("select max(id) from hera_job")
    Integer selectMaxId();

    @Select("select `name`,id,dependencies,auto from hera_job")
    List<HeraJob> getAllJobRelations();

    @Select("select count(*) count, max(id) maxId, max(gmt_modified) lastModified from hera_job")
    Judge selectTableInfo();


    @Update("update hera_job set group_id = #{parentId} where id = #{newId}")
    Integer changeParent(@Param("newId") Integer newId, @Param("parentId") Integer parentId);

    @Select("select repeat_run from hera_job where id = #{jobId}")
    Integer findRepeat(Integer jobId);

    @Select("select dependencies from hera_job where id = #{id}")
    String findUpDependenciesById(Integer id);

    //   查询处指定job_id的下游任务
    @Select("SELECT id FROM hera_job WHERE FIND_IN_SET(#{id}, dependencies);")
    List<String> findDownDependenciesById(int id);

    @Select("SELECT COUNT(id) FROM hera_job WHERE  auto != 1 ")
    int getStopHeraJobInfo();

    /**
     * 　　* @Description: TODO
     * 　　* @param
     * 　　* @return
     * 　　* @throws
     * 　　* @author lenovo
     * 　　* @date 2019/11/7 9:45
     */
    @Select("select count(*) from hera_job where owner=#{owner} ")
    int selectJobCountByUserName(HeraJobVo heraJobVo);

    @Select("SELECT COUNT(*) FROM hera_permission WHERE uid=#{owner} AND type='job'")
    int selectManJobCountByUserName(HeraJobVo heraJobVo);

    //    @Select("SELECT COUNT(*) FROM (SELECT hah.job_id FROM hera_action_history hah LEFT JOIN hera_job hj ON hah.job_id=hj.id  WHERE DATE(hah.start_time) = CURDATE() AND hah.status='failed' AND hj.owner=#{owner} GROUP BY hah.job_id) a")
    @Select("SELECT COUNT(1) FROM " +
            "(SELECT * FROM (SELECT job_id, substring_index( group_concat( STATUS ORDER BY start_time DESC), \",\", 1) AS STATUS FROM hera_action_history WHERE action_id >= CURRENT_DATE () * 10000000000 GROUP BY job_id) a WHERE STATUS = 'failed') b " +
            "LEFT JOIN hera_job c ON b.job_id = c.id " +
            "WHERE c.OWNER = #{owner}")
    int selectFailedCountByUserName(HeraJobVo heraJobVo);

    //任务开启数
    @Select("SELECT  count(1) FROM hera_job where auto=1")
    int selectJobStartCount();

    @Select("SELECT  password FROM hera_user where name=#{user}")
    String getPWDbyUserName(String user);

    @Update("update hera_user set password = #{newP1} where name = #{user}")
    int updatePwdByUser(@Param("user") String user, @Param("newP1") String newP1);

    @Select("SELECT id,script,run_type FROM hera.hera_job where script rlike '(select.+insert|insert.+select)' and name not rlike '(合并|ods|模板|测试|mysql|推送|push-)' and auto!=2;")
    List<HeraJob> findSqlJob();


    @Update("update hera_job (#{heraJob}) where name = #{name} and group_id =#{groupId}  and description=#{description} and name=#{name}")
    @Lang(HeraUpdateLangDriver.class)
    int updateForZZQS(HeraJob heraJob);


    @Delete("delete from hera_job where  name = #{name} and group_id =#{groupId} and description=#{description}  and name=#{name} ")
    int deleteForZZQS(HeraJob heraJob);

    // @Select("SELECT id,dependencies FROM hera_action WHERE dependencies IS NOT  NULL and  LEFT(id, 8)>=DATE_FORMAT(NOW(),'%Y%m%d') ")
    @Select("SELECT id,dependencies FROM hera_action WHERE dependencies IS NOT  NULL and  LEFT(id, 8)>=DATE_FORMAT(DATE_SUB(CURDATE(),INTERVAL 1 DAY),'%Y%m%d') ")
    List<JobDownDenpends> getTodayAllActionId();

    //  @Select("SELECT * FROM hera_action WHERE job_id = #{id} AND LEFT(id, 8)>=DATE_FORMAT(NOW(),'%Y%m%d') ORDER BY id ASC LIMIT 1 ;\n")
    @Select("SELECT * FROM hera_action WHERE job_id = #{id} AND LEFT(id, 8)>=DATE_FORMAT(CURDATE(),'%Y%m%d')" +
            //" AND LEFT(id, 8)< DATE_FORMAT(NOW(),'%Y%m%d') " +
            " ORDER BY id ASC LIMIT 1 ")
    HeraAction getTodayEarliestActionId(String id);

    // @Select("SELECT id FROM hera_action WHERE FIND_IN_SET(#{id}, job_dependencies)  AND LEFT(id, 8)>=DATE_FORMAT(NOW(),'%Y%m%d')")
    @Select("SELECT DISTINCT id  FROM hera_action WHERE FIND_IN_SET(#{id}, dependencies)  AND LEFT(id, 8)>=DATE_FORMAT(NOW(),'%Y%m%d') ")
    List<String> getDownJobIds(String id);
    // List<HeraAction> get1DownDepends(String id);


    @Select("SELECT * FROM hera_action WHERE id = #{id} ORDER BY id ASC LIMIT 1 ")
    HeraAction getHeraAction(String id);

    @Select("SELECT id AS action_id,job_id, statistic_start_time AS start_time, statistic_end_time AS end_time,STATUS FROM hera_action" +
            " WHERE  id=#{actionId} limit 1")
    HeraJobHistory findActionHistoryByActionId(String actionId);

    @Select("select  id AS action_id,job_id, statistic_start_time AS start_time, statistic_end_time AS end_time,STATUS  from  hera_action where id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraJobHistory> batchSelect(@Param("list") List<String> list);

    @Select("select  *  from  hera_action where auto = 1 and id in (#{list})")
    @Lang(HeraListInLangDriver.class)
    List<HeraAction> batchHeraJobSelect(@Param("list") List<String> list);

    @Insert("update hera_action (#{list})")
    @Lang(HeraActionBatchUpdateDriver.class)
  /*  @Update("<script>  update hera_action  \n" +
            "SET\n" +
            "\tready_dependency = CASE id <foreach collection=\"list\" item=\"item\" >  WHEN #{item.id,jdbcType=BIGINT}  THEN #{item.readyDependency}</foreach> END ,\n" +
            "\tstatus = CASE id <foreach collection=\"list\" item=\"item\" >  WHEN #{item.id,jdbcType=BIGINT}  THEN #{item.status}</foreach> END ,\n" +
            "\tscript = CASE id <foreach collection=\"list\" item=\"item\" >  WHEN #{item.id,jdbcType=BIGINT}  THEN #{item.script}</foreach> END \n" +
            "\twhere id in <foreach collection=\"list\" item=\"item\"  separator=\",\" open=\"(\" close=\")\"> #{item.id,jdbcType=BIGINT} </foreach>\n" +
            "</script>")*/
    int batchUpdate(@Param("list") List<HeraAction> list);

    @Update("update hera_job set owner=#{admin} where owner=#{owner}")
    int updateOwnerToAdmin(@Param("admin") String admin, @Param("owner") String owner);

}
