package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraDataDiscovery;
import com.dfire.common.entity.HeraWarnNumControl;
import com.dfire.common.entity.vo.HeraDataDiscoveryVo;
import com.dfire.common.mapper.HeraDataDiscoveryMapper;
import com.dfire.common.mapper.HeraWarnNumControlMapper;
import com.dfire.common.service.HeraDataDiscoveryService;
import com.dfire.common.service.HeraWarnNumControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName HeraDataDiscoveryServiceImpl
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/21 14:41
 **/
@Service("heraWarnNumControlServiceImpl")
public class HeraWarnNumControlServiceImpl implements HeraWarnNumControlService {
    @Autowired
    HeraWarnNumControlMapper heraWarnNumControlMapper;


    @Override
    public Integer insert(HeraWarnNumControl heraWarnNumControl) {
        return heraWarnNumControlMapper.insert(heraWarnNumControl);
    }

    @Override
    public HeraWarnNumControl getOne(HeraWarnNumControl heraWarnNumControl) {
        return heraWarnNumControlMapper.getOne(heraWarnNumControl);
    }
}
