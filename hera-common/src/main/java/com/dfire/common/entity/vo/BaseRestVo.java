package com.dfire.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class BaseRestVo
{
    boolean success;
    String message;
    int code;
    Map<String, Object> data;
}
