package com.dfire.common.graph;

import lombok.Data;

@Data
public class TabRelation {

    private String id;

    private String name;

    //private String dependencies;

    //private Integer pid;

    private String pname;

    //private Integer auto;

    //private Integer pAuto;


    public TabRelation(String id, String name, String pname) {
        this.id = id;
        this.name = name;
        this.pname = pname;
    }
}
