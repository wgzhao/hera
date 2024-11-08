package com.dfire.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName HeraDataDictMapper
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/12 17:58
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeraDataDictNew {
//    database_name	varchar	255	0	False	False		数据库名
//    table_name	varchar	255	0	False	False		表名
//    table_create_time	varchar	255	0	False	False		表创建时间
//    last_ddl_time	varchar	255	0	False	False		表定义最近更新时间
//    table_owner	varchar	255	0	False	False		表创建人
//    table_comment	varchar	1000	0	False	False		表注释
//    column_name	varchar	255	0	False	False		字段名
//    column_type	varchar	255	0	False	False		字段类型
//    column_comment	varchar	1000	0	False	False		字段描述
//    column_rank	int	11	0	False	False		字段排序
//    is_partition	int	11	0	False	False		是否分区字段


//    database_name	varchar	255	0	False	False		数据库名
//    table_name	varchar	255	0	False	False		表名
//    table_create_time	varchar	255	0	False	False		表创建时间
//    table_update_time	varchar	255	0	False	False		表最近更新时间
//    table_comment	varchar	1000	0	False	False		表注释
//    column_name	varchar	255	0	False	False		字段名
//    column_type	varchar	255	0	False	False		字段类型
//    column_comment	varchar	1000	0	False	False		字段描述
//    column_rank	int	11	0	False	False		字段排序
//    is_key	int	11	0	False	False		是否主键
//    column_default	varchar	255	0	False	False		默认值
//    is_nullable	int	11	0	False	False		是否可为空


    //    database_name	varchar	255	0	False	False		数据库名
//    table_name	varchar	255	0	False	False		表名
//    table_create_time	varchar	255	0	False	False		表创建时间
//    table_update_time	varchar	255	0	False	False		表最近更新时间
//    table_comment	varchar	1000	0	False	False		表注释
//    column_name	varchar	255	0	False	False		字段名
//    column_type	varchar	255	0	False	False		字段类型
//    column_comment	varchar	1000	0	False	False		字段描述
//    column_rank	int	11	0	False	False		字段排序
//    is_key	int	11	0	False	False		是否主键
//    column_default	varchar	255	0	False	False		默认值
//    is_nullable	int	11	0	False	False		是否可为空
    private String databaseName;
    private String tableName;
    private String tableCreateTime;
    private String lastDdlTime;
    private String tableUpdateTime;
    private String tableOwner;
    private String tableComment;
    private String columnName;
    private String columnType;
    private String columnComment;
    private Integer columnRank;
    private Integer isPartition;
    private Integer isKey;
    private String columnDefault;
    private Integer isNullable;
    private String tableVariable;

}
