package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraDataDiscovery;
import com.dfire.common.entity.HeraDataDiscoveryNew;
import com.dfire.common.entity.vo.HeraDataDiscoveryVo;
import com.dfire.common.mapper.HeraDataDiscoveryMapper;
import com.dfire.common.mapper.HeraDataDiscoveryNewMapper;
import com.dfire.common.service.HeraDataDiscoveryNewService;
import com.dfire.common.service.HeraDataDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName HeraDataDiscoveryServiceImpl
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/21 14:41
 **/
@Service("heraDataDiscoveryNewServiceImpl")
public class HeraDataDiscoveryNewServiceImpl implements HeraDataDiscoveryNewService {
    @Autowired
    HeraDataDiscoveryNewMapper heraDataDiscoveryNewMapper;

    @Override
    public List<HeraDataDiscoveryNew> selectHeraDataDiscoveryNewList(HeraDataDiscoveryNew heraDataDiscoveryNew) {
        return heraDataDiscoveryNewMapper.selectHeraDataDiscoveryNewList(heraDataDiscoveryNew);
    }
}
