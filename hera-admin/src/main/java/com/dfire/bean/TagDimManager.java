package com.dfire.bean;


import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class TagDimManager {

    private String tagTypeId;
    private String tagType;
    private String sqlId;
    private String tagId;
    private String tagName;
    private String operator;
    private String sqlComment;
    private String queryType;
    private String instruction;
    private String updateTime;
    private String valid;

    public static void main(String[] args) {
        TagDimManager tagDimManager = new TagDimManager();
        tagDimManager.setSqlId("1");
        tagDimManager.setTagId("2");

        System.out.println(JSONObject.toJSONString(tagDimManager));


    }

}
