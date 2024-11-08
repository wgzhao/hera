package com.dfire.common.service;

import com.dfire.common.entity.HeraNginxInfo;
import com.dfire.common.entity.HeraYarnInfoUse;

import java.util.List;

/**
 * @ClassName HeraNginxInfoService
 * @Description TODO
 * @Author lenovo
 * @Date 2020/2/24 15:02
 **/
public interface HeraNginxInfoService {

    Integer insertHeraNginxInfo(HeraNginxInfo heraNginxInfo);

    List<HeraNginxInfo> selectHeraNginxInfoList(HeraNginxInfo heraNginxInfo);

}
