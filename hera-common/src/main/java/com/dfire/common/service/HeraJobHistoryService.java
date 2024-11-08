package com.dfire.common.service;

import com.dfire.common.entity.HeraJobHistory;
import com.dfire.common.entity.HeraJobHistoryForTime;
import com.dfire.common.entity.vo.PageHelper;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 19:18 2018/1/12
 * @desc
 */
public interface HeraJobHistoryService {


    int updateHeraJobHistoryStatus(HeraJobHistory jobStatus);

    int updateHeraJobHistoryLog(HeraJobHistory heraJobHistory);

    int insert(HeraJobHistory heraJobHistory);

    int delete(String id);

    int update(HeraJobHistory heraJobHistory);

    List<HeraJobHistory> getAll();

    List<HeraJobHistoryForTime> findByStatus(String status);

    List<HeraJobHistory> findByStatusJobId(String status, Long jobId);

    HeraJobHistory findById(String id);

    HeraJobHistory findByActionId(String actionId);

    HeraJobHistory findStatusByActionId(Long actionId);

    Integer updateHeraJobHistoryLogAndStatus(HeraJobHistory build);

    /**
     * 根据jobId查询运行历史
     *
     * @param jobId
     * @return
     */
    List<HeraJobHistory> findByJobId(String jobId);

    HeraJobHistory findLogById(Integer id);

    Map<String, Object> findLogByPage(PageHelper pageHelper);

    List<HeraJobHistory> findTodayJobHistory();

    List<HeraJobHistory> getJobStatus(Long actionId);


    //  List<HeraJobHistory> findTodayActionHistory() ;

}
