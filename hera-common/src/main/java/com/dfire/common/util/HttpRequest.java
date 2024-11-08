package com.dfire.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpRequest
{
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url 发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param)
    {
        try {
            if (param == null) {
                return Request.get(url).execute().returnContent().asString(StandardCharsets.UTF_8);
            }
            else {
                return Request.get(url + "?" + param).execute().returnContent().asString(StandardCharsets.UTF_8);
            }
        }
        catch (IOException e) {
            log.error("sendGet error", e);
            return "";
        }
    }

    public static String sendGet(String url)
    {
        return sendGet(url, null);
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param, ContentType contentType)
    {
        try {
            return Request.post(url)
                    .bodyString(param, contentType)
                    .execute()
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);
        }
        catch (IOException e) {
           log.error("sendPost error", e);
        }
        return null;
    }

    public static String sendPost(String url, String param, String contentType)
    {
        return sendPost(url, param, ContentType.create(contentType, StandardCharsets.UTF_8));
    }

    public static String sendPost(String url, String param, String contentType, String charset)
    {
        return sendPost(url, param, ContentType.create(contentType, charset));
    }
}