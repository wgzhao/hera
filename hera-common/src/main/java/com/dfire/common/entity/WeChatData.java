package com.dfire.common.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WeChatData {
    //发送微信消息的URLString sendMsgUrl="https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
    /**
     * 成员账号
     */
    private String touser;
    /**
     * 消息类型
     */
    private String msgtype;
    /**
     * 企业应用的agentID
     */
    private int agentid;
    /**
     * 实际接收Map类型数据
     */
    private Object text;
}



