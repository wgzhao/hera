package com.dfire.common.service;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.graph.JobRelation;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:08 2018/1/11
 * @desc
 */
public interface HeraDataManageService {

    Map<String, Object> findCurrentTableGraph(int jobId, Integer type);

    HeraJob findById(int id);

    List<JobRelation> getJobRelations();

    List<HeraJob> getAllJobDependencies();

}
