package com.dfire.common.service;

import com.dfire.common.entity.ColumnDeps;
import com.dfire.common.entity.FieldInfo;
import com.dfire.common.entity.Node;

import java.util.List;

public interface DepsGraphService {
    //查询指定字段的来源
    List<Node> selectFieldBefore(String table_name, String field);
    //查询指定字段的去向
    List<Node> selectFieldAfter(String tableName, String field);
    //查询指定字段的数据字典.
    FieldInfo getFieldInfo(String tableName, String field);
}
