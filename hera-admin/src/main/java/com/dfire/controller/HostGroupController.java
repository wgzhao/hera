package com.dfire.controller;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.service.HeraHostGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/4/20
 */
@RestController
@RequestMapping("/hostGroup/")
public class HostGroupController {

    @Autowired
    private HeraHostGroupService heraHostGroupService;

    @GetMapping(value = "list")
    public TableResponse<List<HeraHostGroup>> getAll() {

        List<HeraHostGroup> groupList = heraHostGroupService.getAll();

        if (groupList == null) {
            return new TableResponse<>(-1, "查询失败");
        }
        return new TableResponse<>(groupList.size(), 0, groupList);

    }

    @PostMapping(value = "add")
    public JsonResponse add(@RequestBody HeraHostGroup heraHostGroup) {
        boolean res = heraHostGroupService.insert(heraHostGroup) > 0;
        return new JsonResponse(res, res ? "新增成功" : "新增失败");
    }

    @PutMapping(value = "update")
    public JsonResponse update(@RequestBody HeraHostGroup heraHostGroup) {
        boolean update = heraHostGroupService.update(heraHostGroup) > 0;
        return new JsonResponse(update, update ? "更新成功" : "更新失败");
    }

    @DeleteMapping(value = "del/{id}")
    public JsonResponse del(@PathVariable int id) {
        int res = heraHostGroupService.delete(id);
        return new JsonResponse(res > 0, res > 0 ? "删除成功" : "删除失败");
    }
}
