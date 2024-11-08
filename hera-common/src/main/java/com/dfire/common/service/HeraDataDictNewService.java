package com.dfire.common.service;

import com.dfire.common.entity.HeraDataDict;
import com.dfire.common.entity.vo.HeraDataDictNew;
import com.dfire.common.entity.vo.HeraDataDictVo;

import java.util.List;

/**
 * @ClassName HeraDataDictService
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/12 20:04
 **/
public interface HeraDataDictNewService {
    List<HeraDataDictNew> selectHeraDataDictNewList(HeraDataDictNew heraDataDictNew);

    List<HeraDataDictNew> selectHeraDataDictNewFields(HeraDataDictNew heraDataDictNew);

    int updateHeraDataDict(HeraDataDict heraDataDict);

    int updateHeraDataDictTableStatus(HeraDataDict heraDataDict);

    int selectHeraDataDictTableCount(HeraDataDictVo heraDataDict);
}