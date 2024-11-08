package com.dfire.common.service;

import com.dfire.common.entity.HeraAdvice;
import com.dfire.common.entity.HeraWarnNumControl;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
public interface HeraWarnNumControlService {

    Integer insert(HeraWarnNumControl heraWarnNumControl);

    HeraWarnNumControl getOne(HeraWarnNumControl heraWarnNumControl);
}
