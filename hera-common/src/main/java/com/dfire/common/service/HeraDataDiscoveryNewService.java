package com.dfire.common.service;

import com.dfire.common.entity.HeraDataDiscovery;
import com.dfire.common.entity.HeraDataDiscoveryNew;
import com.dfire.common.entity.vo.HeraDataDiscoveryVo;

import java.util.List;

public interface HeraDataDiscoveryNewService {
    List<HeraDataDiscoveryNew> selectHeraDataDiscoveryNewList(HeraDataDiscoveryNew heraDataDiscoveryNew);
}
