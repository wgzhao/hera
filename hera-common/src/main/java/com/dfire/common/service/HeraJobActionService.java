package com.dfire.common.service;

import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.kv.Tuple;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.common.vo.JobStatus;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:41 2018/5/16
 * @desc
 */
public interface HeraJobActionService {


    int insert(HeraAction heraAction, Long nowAction);

    /**
     * 批量插入
     *
     * @param heraActionList
     * @return
     */
    List<HeraAction> batchInsert(List<HeraAction> heraActionList, Long nowAction);

    int delete(String id);

    int update(HeraAction heraAction);

    List<HeraAction> getAll();

    HeraAction findById(String actionId);

    HeraAction findLatestByJobId(String jobId);

    List<HeraAction> findByJobId(String jobId);

    int updateStatus(JobStatus jobStatus);

    Tuple<HeraActionVo, JobStatus> findHeraActionVo(String jobId);

    /**
     * 查找当前版本的运行状态
     *
     * @param actionId
     * @return
     */
    JobStatus findJobStatus(String actionId);

    String findJobConfig(String actionId);

    JobStatus findJobStatusByJobId(String jobId);


    Integer updateStatus(HeraAction heraAction);

    Integer updateStatusAndReadDependency(HeraAction heraAction);


    List<HeraAction> getAfterAction(Long action);

    /**
     * 根据jobId 获取所有的版本
     *
     * @param jobId
     * @return
     */
    List<String> getActionVersionByJobId(Long jobId);


    List<HeraActionVo> getNotRunScheduleJob();

    List<HeraActionVo> getFailedJob();


    List<GroupTaskVo> findByJobIds(List<Integer> idList, String startDate, String endDate, TablePageForm pageForm, Integer type);


    //获取最近版本的id和状态
    String getLatestVersionAndStatus(String jobId);

    //获取指定版本的状态
    String getStatus(String jobId);

    //获取最近版本的id
    String getLatestVersionId(String jobId);

    //获取上游任务的id和状态
    List<String> judgeUpDependsStatus(String id);

    //获取上游任务的id
    String getUpDependsId(String id);

    //获取下游任务的状态
    List<String> judgeDownDependsStatus(String id);

    //获取下游任务的id
    List<String> judgeDownDependsId(String id);

    //获取任务的依赖和已准备好的依赖
    String getReadDependency(String id);

    //获取任务运行的常规时间
    String getTaskCommonTime(String id);

}
