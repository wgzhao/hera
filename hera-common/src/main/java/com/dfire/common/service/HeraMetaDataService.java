package com.dfire.common.service;

import com.dfire.common.entity.HeraMetaData;
import com.dfire.common.entity.vo.HeraMetaDataVo;

import java.util.List;

public interface HeraMetaDataService {

    List<HeraMetaData> selectHeraMetaDataList(HeraMetaDataVo heraMetaDataVo);

    int selectHeraMetaDataCount(HeraMetaDataVo heraMetaDataVo);


    int updateMetaData(HeraMetaData heraMetaData);

    int insertHeraMetaData(HeraMetaData heraMetaData);
}
