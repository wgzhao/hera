package com.dfire.common.service.impl;

import com.dfire.common.entity.ColumnDeps;
import com.dfire.common.entity.FieldInfo;
import com.dfire.common.entity.Node;
import com.dfire.common.mapper.DepsGraphMapper;
import com.dfire.common.service.DepsGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("depsGraphServiceImpl")
public class DepsGraphServiceImpl implements DepsGraphService {

    private static final Logger LOG = LoggerFactory.getLogger(DepsGraphServiceImpl.class);

    @Autowired
    DepsGraphMapper depsGraphMapper;

    //存储所有的字段依赖数据
    // key: tableName.field  value: list<ColumnDeps>
    private static final Map<String,List<ColumnDeps> > fieldMap = new HashMap<>();
    private static final Map<String,List<ColumnDeps> > sourceFieldMap = new HashMap<>();
    // key: tableName value: Set<field>
    private static final Map<String, Set<String>> tableMap = new HashMap<>();
    private static final Map<String, FieldInfo> fieldInfoMap = new HashMap<>();

    @Override
    public FieldInfo getFieldInfo(String tableName, String field) {
        if(fieldInfoMap.isEmpty()) loadDataDictionary();
        String key = tableName + "." + field;
        return fieldInfoMap.get(key);
    }

    @Override//查询指定字段的来源.
    public List<Node> selectFieldBefore(String table_name, String field) {
        if(tableMap.isEmpty()) {
            loadData();
        }

        //查询该表所有的字段
        Set<String> fields = tableMap.get(table_name);
        if( fields == null || fields.isEmpty()) return null;

        ArrayList<Node> res = new ArrayList<>();
        //创建根节点
        Node node = new Node();
        node.setId("root");
        node.setIsroot(true);
        node.setTopic(table_name + "表");
        res.add(node);

        //遍历该表的所有字段
        for (String fieldName : fields) {
            //默认field不传查询全部字段
            if("-1".equals(field)){
                searchAllSource(table_name, fieldName, "root", res, false);
            }else{
                searchAllSource(table_name, field, "root", res, false);
                break;
            }
        }

        return res;
    }

    //递归调用查询方法
    public void searchAllSource(String tableName, String field, String parentId, List<Node> res, boolean flag){

        String topic = MessageFormat.format("<label class='node' ondblclick='getColumnByNode(this)' onmouseover='doExtend(this)' value=''{0}''>{1}</label>", tableName + "." + field, field);
        Node node = new Node(parentId, topic);
        res.add(node);
        if(flag) return;

        //查询当前节点
        String key = tableName + "." + field;
        List<ColumnDeps> tableCols = fieldMap.get(key);
        if(tableCols == null || tableCols.isEmpty()) return;

        //递归查询所有子节点
        tableCols.forEach(tableCol -> {
            String name = tableCol.getSourceTableName();
            String sourceField = tableCol.getSourceField();
            //有些表读自己写自己，目标表和来源表一样，针对这样的表再创建一个子节点即可，防止无限递归下去内存溢出.
            boolean isEquals = tableName.equals(name);
            searchAllSource(name, sourceField, node.getId(), res, isEquals);
        });
    }


    //查询指定字段的去向
    public List<Node> selectFieldAfter(String tableName, String field){
        if(tableMap.isEmpty()) loadData();
        //查询所有直接使用到该字段的表.
        String key = tableName + "." + field;
        List<ColumnDeps> tableCols = sourceFieldMap.get(key);
        if(tableCols == null || tableCols.isEmpty()) return null;

        ArrayList<Node> res = new ArrayList<>();
        //创建根节点
        Node node = new Node();
        node.setId("root");
        node.setIsroot(true);
        node.setTopic(field);
        res.add(node);

        //递归查询
        for (ColumnDeps tableCol : tableCols) {
            String targetTable = tableCol.getTableName();
            String targetField = tableCol.getField();
            //如果源表和目标表一样的话，会造成无限递归，因此只创建一个父节点即可。
            if(tableName.equals(targetTable)) continue;
            searchAllTarget(targetTable, targetField, "root", res);
        }

        return res;
    }



    //递归调用
    public void  searchAllTarget(String tableName, String field,  String parentId, List<Node> res){
        String topic = MessageFormat.format("<label class='node' ondblclick='getColumnByNode(this)' onmouseover='doExtend(this)' value=''{0}''>{1}</label>", tableName + "." + field, field);
        Node node = new Node(parentId, topic);
        res.add(node);

        //查询当前节点
        String key = tableName + "." + field;
        List<ColumnDeps> tableCols = sourceFieldMap.get(key);
        if(tableCols == null || tableCols.isEmpty()) return;

        //递归查询所有子节点
        tableCols.forEach(tableCol -> {
            String targetTable = tableCol.getTableName();
            String targetField = tableCol.getField();
            //源表和目标表一样的话，只创建一个父节点即可，防止无限递归下去内存溢出.
            if(tableName.equals(targetTable)) return;
            searchAllTarget(targetTable, targetField, node.getId(), res);
        });
    }


    //定时加载数据到缓存里.
    @Scheduled(cron = "0 0 9,15 * * ?")
    public void loadData(){
        LOG.info("定时加载表依赖数据开始。。。");
        long start = System.currentTimeMillis();
        //清空原来数据
        fieldMap.clear();
        tableMap.clear();
        sourceFieldMap.clear();
        //重新置为0
        Node.num = 0;

        //查询所有数据
        List<ColumnDeps> tableCols = depsGraphMapper.selectAll();

        tableCols.forEach(columnDeps -> {

            //目标表与字段的关系
            String tableName = columnDeps.getTableName();
            String field = columnDeps.getField();
            String key = tableName + "." + field;
            if(!fieldMap.containsKey(key)){
                ArrayList<ColumnDeps> list = new ArrayList<>();
                list.add(columnDeps);
                fieldMap.put(key, list);
            }else{
                fieldMap.get(key).add(columnDeps);
            }

            //来源表与字段的关系
            String sourceTableName = columnDeps.getSourceTableName();
            String sourceField = columnDeps.getSourceField();
            String sourceKey = sourceTableName + "." + sourceField;
            if(!sourceFieldMap.containsKey(sourceKey)){
                ArrayList<ColumnDeps> list = new ArrayList<>();
                list.add(columnDeps);
                sourceFieldMap.put(sourceKey, list);
            }else{
                sourceFieldMap.get(sourceKey).add(columnDeps);
            }

            //目标表的所有字段
            if(!tableMap.containsKey(tableName)){
                HashSet<String> set = new LinkedHashSet<>();
                set.add(field);
                tableMap.put(tableName, set);
            }else{
                tableMap.get(tableName).add(field);
            }
        });

        LOG.info("定时加载表依赖数据结束。。。");
        long end = System.currentTimeMillis();
        LOG.info("耗时：{}秒", (end - start) / 1000.0);
    }

    //定时加载数据字典信息到缓存里.
    @Scheduled(cron = "0 0 9,15 * * ?")
    public void loadDataDictionary(){
        LOG.info("定时加载数据字典开始。。。");
        long start = System.currentTimeMillis();
        fieldInfoMap.clear();
        List<FieldInfo> fieldInfos = depsGraphMapper.selectDataDictionaryAll();
        fieldInfos.forEach(fieldInfo -> {
            String key = fieldInfo.getTableName() + "." + fieldInfo.getField();
            fieldInfoMap.put(key, fieldInfo);
        });

        LOG.info("定时加载数据字典结束。。。");
        long end = System.currentTimeMillis();
        LOG.info("耗时：{}秒", (end - start) / 1000.0);
    }

}
