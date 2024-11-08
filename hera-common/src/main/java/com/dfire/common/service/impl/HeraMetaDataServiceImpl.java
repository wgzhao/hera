package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraMetaData;
import com.dfire.common.entity.vo.HeraMetaDataVo;
import com.dfire.common.mapper.HeraMetaDataMapper;
import com.dfire.common.service.HeraMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lenovo
 * @description
 * @date 2020/5/19 20:16
 */
@Service("heraMetaDataService")
public class HeraMetaDataServiceImpl implements HeraMetaDataService {

    @Autowired
    HeraMetaDataMapper heraMetaDataMapper;

    @Override
    public List<HeraMetaData> selectHeraMetaDataList(HeraMetaDataVo heraMetaDataVo) {
        return heraMetaDataMapper.selectHeraMetaDataList(heraMetaDataVo);
    }

    @Override
    public int selectHeraMetaDataCount(HeraMetaDataVo heraMetaDataVo) {
        return heraMetaDataMapper.selectHeraMetaDataCount(heraMetaDataVo);
    }

    @Override
    public int updateMetaData(HeraMetaData heraMetaData) {
        return heraMetaDataMapper.updateMetaData(heraMetaData);
    }

    @Override
    public int insertHeraMetaData(HeraMetaData heraMetaData) {
        return heraMetaDataMapper.insertHeraMetaData(heraMetaData);
    }

}
