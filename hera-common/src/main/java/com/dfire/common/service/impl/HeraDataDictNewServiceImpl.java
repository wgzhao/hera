package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraDataDict;
import com.dfire.common.entity.vo.HeraDataDictNew;
import com.dfire.common.entity.vo.HeraDataDictVo;
import com.dfire.common.mapper.HeraDataDictMapper;
import com.dfire.common.mapper.HeraDataDictNewMapper;
import com.dfire.common.service.HeraDataDictNewService;
import com.dfire.common.service.HeraDataDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName HeraDataDictServiceImpl
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/12 20:06
 **/
@Service("heraDataDictNewServiceImpl")
public class HeraDataDictNewServiceImpl implements HeraDataDictNewService {
    @Autowired
    HeraDataDictNewMapper heraDataDictNewMapper;

    @Override
    public List<HeraDataDictNew> selectHeraDataDictNewList(HeraDataDictNew heraDataDictNew) {
        return heraDataDictNewMapper.selectHeraDataDictNewList(heraDataDictNew);
    }

    @Override
    public List<HeraDataDictNew> selectHeraDataDictNewFields(HeraDataDictNew heraDataDictVo) {

        return heraDataDictNewMapper.selectHeraDataDictNewFields(heraDataDictVo);
    }

    @Override
    public int updateHeraDataDict(HeraDataDict heraDataDict) {
        return heraDataDictNewMapper.updateHeraDataDict(heraDataDict);
    }

    @Override
    public int updateHeraDataDictTableStatus(HeraDataDict heraDataDict) {
        return heraDataDictNewMapper.updateHeraDataDictTableStatus(heraDataDict);
    }

    @Override
    public int selectHeraDataDictTableCount(HeraDataDictVo heraDataDict) {
        return heraDataDictNewMapper.selectHeraDataDictTableCount(heraDataDict);
    }
}
