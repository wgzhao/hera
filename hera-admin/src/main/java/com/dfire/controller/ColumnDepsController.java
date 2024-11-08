package com.dfire.controller;

import com.dfire.common.entity.ColumnDeps;
import com.dfire.common.entity.FieldInfo;
import com.dfire.common.entity.Node;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.DepsGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/columnDeps")
public class ColumnDepsController {

    @Autowired
    private DepsGraphService depsGraphService;

    @RequestMapping(value = "/getSource", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getSource(@RequestParam("tableName")  String tableName, @RequestParam(value = "field", defaultValue = "-1", required = false) String field){
        List<Node> nodes = depsGraphService.selectFieldBefore(tableName, field);
        if(nodes != null){
            return new JsonResponse(true, "success", nodes);
        }else{
            return new JsonResponse(true, "未查询到数据", null);
        }
    }

    @RequestMapping(value = "/getTarget", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getTarget(@RequestParam("tableName") String tableName,@RequestParam("field") String field){
        List<Node> nodes = depsGraphService.selectFieldAfter(tableName, field);
        if(nodes != null){
            return new JsonResponse(true, "success", nodes);
        }else{
            return new JsonResponse(true, "未查询到数据", null);
        }
    }


    @RequestMapping(value = "/getFieldInfo", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getFieldInfo(@RequestParam("tableName") String tableName,@RequestParam("field") String field){
        FieldInfo fieldInfo = depsGraphService.getFieldInfo(tableName, field);
        if(fieldInfo != null){
            return new JsonResponse(true, "success", fieldInfo);
        }else{
            return new JsonResponse(true, "未查询到数据", null);
        }
    }

}
