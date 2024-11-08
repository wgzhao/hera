package com.dfire.common.entity;

public class Node {
    private String id;
    private String parentid;
    private String topic;
    private boolean expanded = true;
    private String direction = "right";
    private boolean isroot;

    public static int num = 0;

    public Node(String parentid, String topic) {
        this.parentid = parentid;
        this.topic = topic;
        num++;
        this.id = num + "";
    }

    public Node() {
    }

    public boolean isIsroot() {
        return isroot;
    }

    public void setIsroot(boolean isroot) {
        this.isroot = isroot;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
