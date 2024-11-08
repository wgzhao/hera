package com.dfire.common.util;

import com.alibaba.fastjson2.JSON;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.WeChatData;
import com.dfire.common.entity.WeChatUrlData;
import com.dfire.common.logs.DebugLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName WeChatUtil
 * @Description TODO
 * @Author lenovo
 * @Date 2019/8/29 14:40
 **/
public class WeChatUtil
{

    /**
     * 　　* @Description: TODO 发送微信消息警告
     * 　　* @param [heraUser, text, desp]
     * 　　* @return void
     * 　　* @throws
     * 　　* @author lenovo
     * 　　* @date 2019/8/29 14:57
     */
    public static void sendWeChatMessage(HeraUser heraUser, String text, String desp)
    {

        DebugLog.info("向{}发送预警消息到微信", heraUser.getName());
        HttpRequest.sendPost("https://sc.ftqq.com/" + heraUser.getScKey() + ".send",
                "text=" + text + "&desp=" + desp,
                "application/x-www-form-urlencoded");
    }

    /**
     * 　　* @Description: TODO 发送微信消息警告
     * 　　* @param [heraUser, text, desp]
     * 　　* @return void
     * 　　* @throws
     * 　　* @author lenovo
     * 　　* @date 2019/8/29 14:57
     */
    public static void sendWeChatMessage(String scKey, String text, String desp)
    {

        HttpRequest.sendPost("https://sc.ftqq.com/" + scKey + ".send",
                "text=" + text + "&desp=" + desp,
                "application/x-www-form-urlencoded");
    }

    /**
     * 微信授权请求，GET类型，获取授权响应，用于其他方法截取token
     *
     * @param Get_Token_Url
     * @return String 授权响应内容
     * @throws IOException
     */
    protected String toAuth(String Get_Token_Url)
    {

        return HttpRequest.sendGet(Get_Token_Url);
    }

    /**
     * corpid应用组织编号   corpsecret应用秘钥
     * 获取toAuth(String Get_Token_Url)返回结果中键值对中access_token键的值
     *
     */
    public String getToken(String corpid, String corpsecret)
    {
        WeChatUrlData uData = new WeChatUrlData();
        uData.setGet_Token_Url(corpid, corpsecret);
        String resp = this.toAuth(uData.getGet_Token_Url());
        System.out.println("resp=====:" + resp);
        Map map = JSON.parseObject(resp, Map.class);
        return map.get("access_token").toString();
    }

    /**
     * @return String
     * @Title:创建微信发送请求post数据 touser发送消息接收者    ，msgtype消息类型（文本/图片等），
     * application_id应用编号。
     * 本方法适用于text型微信消息，contentKey和contentValue只能组一对
     */
    public String createPostData(String touser, String msgtype,
            int application_id, String contentKey, String contentValue)
    {
        WeChatData wcd = new WeChatData();
        wcd.setTouser(touser);
        wcd.setAgentid(application_id);
        wcd.setMsgtype(msgtype);
        Map<Object, Object> content = new HashMap<Object, Object>();
        content.put(contentKey, contentValue);
        wcd.setText(content);
        return JSON.toJSONString(wcd);
    }

    /**
     * @return String
     * @Title 创建微信发送请求post实体
     * charset消息编码    ，contentType消息体内容类型，
     * url微信消息发送请求地址，data为post数据，token鉴权token
     */
    public String post(String charset, String contentType, String url,
            String data, String token)
            throws IOException
    {
        return HttpRequest.sendPost(url + token, data, contentType, charset);
    }
}
