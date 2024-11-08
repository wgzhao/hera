package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo {
    private String tableName;
    private String field;
    private String tableComment = "";
    private String fieldType = "";
    private String fieldComment ="";
}
