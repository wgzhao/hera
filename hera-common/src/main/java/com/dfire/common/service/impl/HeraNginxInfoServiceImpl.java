package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraNginxInfo;
import com.dfire.common.entity.HeraYarnInfoUse;
import com.dfire.common.mapper.HeraNginxInfoMapper;
import com.dfire.common.service.HeraNginxInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName HeraNginxInfoServiceImpl
 * @Description TODO
 * @Author lenovo
 * @Date 2020/2/24 15:06
 **/
@Service("heraNginxInfoService")
public class HeraNginxInfoServiceImpl implements HeraNginxInfoService {
    @Autowired
    HeraNginxInfoMapper heraNginxInfoMapper;

    @Override
    public Integer insertHeraNginxInfo(HeraNginxInfo heraNginxInfo) {
        return heraNginxInfoMapper.insertHeraNginxInfo(heraNginxInfo);
    }

    @Override
    public List<HeraNginxInfo> selectHeraNginxInfoList(HeraNginxInfo heraNginxInfo) {
        return heraNginxInfoMapper.selectHeraNginxInfoList(heraNginxInfo);
    }


}
