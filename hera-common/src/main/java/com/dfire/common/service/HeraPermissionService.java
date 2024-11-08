package com.dfire.common.service;

import com.dfire.common.entity.HeraPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:41 2018/5/16
 * @desc
 */
public interface HeraPermissionService {

    int insert(HeraPermission heraPermission);

    int delete(@Param("id") String id);

    int update(HeraPermission heraPermission);

    List<HeraPermission> getAll();

    HeraPermission findById(HeraPermission heraPermission);

    List<HeraPermission> findByIds(List<Integer> list);

    List<HeraPermission> findByTargetId(Integer targetId);

    HeraPermission findByCond(Integer id, String owner);

    Integer deleteByTargetId(Integer id);

    Integer insertList(List<HeraPermission> permissions);

    Integer deleteUserPermission(String userName);
}
