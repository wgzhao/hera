package com.dfire.common.service;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.vo.HeraJobTreeNodeVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.kv.JobDownDenpends;
import com.dfire.common.graph.JobRelation;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
public interface HeraJobService {

    int insert(HeraJob heraJob);

    int delete(int id);

    int update(HeraJob heraJob);


    List<HeraJob> getAll();

    HeraUser findScKeyById(int jobId);

    HeraJob findById(int id);

    List<HeraJob> findByIds(List<Integer> list);

    List<HeraJob> findByPid(int groupId);

    /**
     * 构建job树形目录结构
     *
     * @return
     */
    Map<String, List<HeraJobTreeNodeVo>> buildJobTree(String owner);

    boolean changeSwitch(int id, int status);

    JsonResponse checkAndUpdate(HeraJob heraJob);

    Map<String, Object> findCurrentJobGraph(int jobId, int type);

    Map<String, Object> findCurrentJobGraphForDownRecovery(String jobId, String action, int type);

    JsonResponse changeDownActionId(String jobId, boolean isReRun, String runDay,boolean isfirstDay);

    List<Integer> findJobImpact(int jobId, int type);

    /**
     * 构建依赖图边
     *
     * @return
     */
    List<JobRelation> getJobRelations();


    List<HeraJob> findDownStreamJob(int jobId);

    List<HeraJob> findUpStreamJob(int jobId);

    List<HeraJob> getAllJobDependencies();


    boolean changeParent(int newId, int parentId);

    boolean isRepeat(int jobId);

    //查看任务关闭数
    int getStopHeraJobInfo();

    int selectJobCountByUserName(HeraJobVo heraJobVo);

    int selectManJobCountByUserName(HeraJobVo heraJobVo);

    int selectfailedCountByUserName(HeraJobVo heraJobVo);

    int selectJobStartCount();

    String getPWDbyUserName(String user);


    int updatePwdByUser(String user, String newP1);


    List<HeraJob> findSqlJob();


    /**
     * 自助取数关闭任务
     *
     * @param heraJob
     * @return
     */
    int updateForZZQS(HeraJob heraJob);

    int deleteForZZQS(HeraJob heraJob);


    HeraAction getTodayEarliestActionId(String jobId);

    int updateOwnerToAdmin(@Param("admin") String admin, @Param("owner") String owner);

    List<JobDownDenpends> getTodayAllActionId();

    List<HeraAction> batchHeraJobSelect(ArrayList<String> strings);

    int batchUpdate(List<HeraAction> heraActions);

}
