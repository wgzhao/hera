package com.dfire.controller;

import com.alibaba.fastjson2.JSONObject;
import com.dfire.bean.TagDimManager;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraSqoopTaskService;
import com.dfire.common.util.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.dfire.common.util.HeraTodayDateUtil.getTodayTime;

/**
 * @author:
 * @time: Created in 16:50 2019/10/25
 * @desc 调度中心sqoop监控
 */
@Controller
@RequestMapping("/tagManageController")
public class TagManageController
{

    @Autowired
    private HeraSqoopTaskService heraSqoopTaskService;

    @Value("${tag.admin}")
    private String tagAdmin;

    @Value("${tag.url}")
    private String tagPushUrl;

    @RequestMapping("/tagPush")
    public String getTagPush()
    {
        return "tagPush/tagManage";
    }

    @RequestMapping("/GZGtagPush")
    public String getGZGTagPush()
    {
        return "tagPush/GZGtagManage";
    }


    @RequestMapping(value = "/getAllTagInfo", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getAllTagInfo(@RequestParam("tagTypeId") String tagTypeId, @RequestParam("isValid") String isValid, @RequestParam(required = false, value = "operator") String operator)
    {

        String data = HttpRequest.sendPost(tagPushUrl + "getData", "tagTypeId=" + tagTypeId + "&isValid=" + isValid , "application/x-www-form-urlencoded");
        return JSONObject.parseObject(data, JsonResponse.class);
//        try {
//            OkHttpClient client = new OkHttpClient().newBuilder()
//                    .build();
//            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                    .addFormDataPart("tagTypeId", tagTypeId)
//                    .addFormDataPart("isValid", isValid)
//                    .build();
//
//            Request request = new Request.Builder()
//                    .url(tagPushUrl + "getData")
//                    .method("POST", body)
//                    .build();
//            Response response = client.newCall(request).execute();
//            String data = response.body().string();
//            JsonResponse jsonResponse = JSONObject.parseObject(data, JsonResponse.class);
//            return jsonResponse;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new JsonResponse("查询失败", false, null);
//        }
    }

    @RequestMapping(value = "/getOldValue", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse getOldValue(@RequestParam("sqlId") String sqlId, @RequestParam("tagId") String tagId)
    {

        String data = HttpRequest.sendPost(tagPushUrl + "/getOneData", "sqlId=" + sqlId + "&tagId=" + tagId, "application/x-www-form-urlencoded");
        return JSONObject.parseObject(data, JsonResponse.class);
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("sqlId", sqlId)
//                .addFormDataPart("tagId", tagId)
//                .build();
//        Request request = new Request.Builder()
//                .url(tagPushUrl + "/getOneData")
//                .method("POST", body)
//                .build();
//        try {
//            Response response = client.newCall(request).execute();
//            String data = response.body().string();
//            jsonResponse = JSONObject.parseObject(data, JsonResponse.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return jsonResponse;
//    }
    }

    @RequestMapping(value = "/checkSqlId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse checkSqlId(@RequestParam("sqlId") String sqlId)
    {

        String data = HttpRequest.sendPost(tagPushUrl + "/getSqlComment", "sqlId=" + sqlId, "application/x-www-form-urlencoded");
        return JSONObject.parseObject(data, JsonResponse.class);
//        JsonResponse jsonResponse = null;
//        try {
//            OkHttpClient client = new OkHttpClient().newBuilder()
//                    .build();
//            MediaType mediaType = MediaType.parse("text/plain");
//            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                    .addFormDataPart("sqlId", sqlId)
//                    .build();
//            Request request = new Request.Builder()
//                    .url(tagPushUrl + "/getSqlComment")
//                    .method("POST", body)
//                    .build();
//            Response response = client.newCall(request).execute();
//
//            String data = response.body().string();
//            //  System.out.println("11  " + data);
//            jsonResponse = JSONObject.parseObject(data, JsonResponse.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        return jsonResponse;
    }

    @RequestMapping(value = "/repalceDimMan", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse repalceDimMan111(@org.springframework.web.bind.annotation.RequestBody TagDimManager tdm)
    {

        if (tdm == null) {
            return new JsonResponse("参数为空", false, "请检查参数或联系管理员");
        }

        if (tdm.getTagTypeId() == null) {
            tdm.setTagTypeId("auth_info");
        }

        if (tdm.getTagTypeId() != null) {
            String tagTypeId = tdm.getTagTypeId().trim().toLowerCase();
            switch (tagTypeId) {
                case "auth_info":
                    tdm.setTagType("权限信息");
                    break;
                case "basic_info":
                    tdm.setTagType("基础信息");
                    break;
                case "source_info":
                    tdm.setTagType("来源信息");
                    break;
                case "lifecycle_info":
                    tdm.setTagType("生命周期");
                    break;
                case "operate_info":
                    tdm.setTagType("操作行为");
                    break;
                default:
                    tdm.setTagType("其他信息");
            }
        }
        // String flag = tdm.getUpdateTime();  // 0代表修改  ，1 代表新增
        // System.out.println("flag : "+flag);

        tdm.setUpdateTime(getTodayTime());
        JsonResponse jsonResponse = new JsonResponse();
        String data = HttpRequest.sendPost(tagPushUrl + "/repalceDimMan", JSONObject.toJSONString(tdm), "application/json");
        return JSONObject.parseObject(data, JsonResponse.class);
//        try {
//
//            OkHttpClient client = new OkHttpClient().newBuilder()
//                    .build();
//            MediaType mediaType = MediaType.parse("application/json");
//            RequestBody body = RequestBody.create(mediaType, JSONObject.toJSONString(tdm));
//            Request request = new Request.Builder()
//                    .url(tagPushUrl + "/repalceDimMan")
//                    .method("POST", body)
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//            Response response = client.newCall(request).execute();
//            String data = response.body().string();
//            //  System.out.println("11  " + data);
//            jsonResponse = JSONObject.parseObject(data, JsonResponse.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return jsonResponse;

       /* if(flag.equalsIgnoreCase("0")){
            System.out.println("修改");
            try {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, JSONObject.toJSONString(tdm));
                Request request = new Request.Builder()
                        .url(tagPushUrl+"/repalceDimMan")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String data = response.body().string();
                System.out.println("11  " + data);
                jsonResponse = JSONObject.parseObject(data, JsonResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonResponse;
        }else{
            System.out.println("新增");
            try {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, JSONObject.toJSONString(tdm));
                Request request = new Request.Builder()
                        .url(tagPushUrl+"/checkSqlId")
                        .method("POST", body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String data = response.body().string();
                System.out.println("11  " + data);
                jsonResponse = JSONObject.parseObject(data, JsonResponse.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonResponse;
        }*/

    }

    @RequestMapping(value = "/checkPermission", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse checkPermission(@RequestParam("user") String user)
    {

        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.setSuccess(false);
        try {
            String[] admins = tagAdmin.split(",");
            for (int i = 0; i < admins.length; i++) {
                if (admins[i].trim().equalsIgnoreCase(user)) {
                    jsonResponse.setSuccess(true);
                    break;
                }
            }
        }
        catch (Exception e) {
            jsonResponse.setSuccess(false);
            e.printStackTrace();
        }
        return jsonResponse;
    }
}




