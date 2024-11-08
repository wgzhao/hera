package com.dfire.controller;

import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraUserVo;
import com.dfire.common.service.*;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.logs.HeraLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:10 2018/6/14
 * @desc
 */
@Controller
@RequestMapping("/userManage")
public class UserManageController {

    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private HeraJobService heraJobService;

    @Autowired
    private HeraGroupService heraGroupService;

    @Autowired
    private HeraPermissionService heraPermissionService;

    @Autowired
    private HeraJobMonitorService heraJobMonitorService;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("heraFileMemoryService")
    private HeraFileService heraFileService;

    @RequestMapping(value = "/initUser", method = RequestMethod.GET)
    @ResponseBody
    public TableResponse<List<HeraUserVo>> initUser() {
        List<HeraUser> users = heraUserService.getAll();
        List<HeraUserVo> res;
        if (users != null) {
            res = new ArrayList<>(users.size());
            for (HeraUser user : users) {
                HeraUserVo userVo = new HeraUserVo();
                BeanUtils.copyProperties(user, userVo);
                userVo.setCreateTime(ActionUtil.getDefaultFormatterDate(user.getGmtCreate()));
                userVo.setOpTime(ActionUtil.getDefaultFormatterDate(user.getGmtModified()));
                res.add(userVo);
            }
        } else {
            res = new ArrayList<>(0);
        }
        res.sort((o1, o2) -> -(o1.getCreateTime().compareTo(o2.getCreateTime())));
        return new TableResponse<>(res.size(), 0, res);
    }

    @RequestMapping(value = "/editUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse editUser(@RequestBody HeraUser user) {
        int result = heraUserService.update(user);
        JsonResponse jsonResponse = new JsonResponse(true, "更新成功");
        if (result <= 0) {
            jsonResponse.setMessage("更新失败");
            jsonResponse.setSuccess(false);
        }
        return jsonResponse;
    }

    /**
     * operateType: 1,执行删除操作，2，执行审核通过操作，3，执行审核拒绝操作
     *
     * @return
     */

    @RequestMapping(value = "/operateUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse operateUser(Integer id, String operateType) {

        JsonResponse response = new JsonResponse(false, "更新失败");
        int result;

        OperateTypeEnum operateTypeEnum = OperateTypeEnum.parse(operateType);
        if (operateTypeEnum == OperateTypeEnum.Delete) {
            HeraUser user = heraUserService.findById(id);
            if (user != null && !user.getName().isEmpty()) {
                String userName = user.getName();
                String admin = environment.getProperty("hera.admin", "");
                if (admin.equals(userName)) {
                    return new JsonResponse(false, "删除的用户是系统管理员，不可删除");
                }
                heraJobService.updateOwnerToAdmin(admin, userName);
                heraGroupService.updateOwnerToAdmin(admin, userName);
                heraPermissionService.deleteUserPermission(userName);
                Integer count = heraJobMonitorService.removeMonitorByUserId(id);
                HeraLog.info("删除用户取关个数:" + count);
                result = heraUserService.delete(id);
                if (result > 0) {
                    response.setMessage("删除成功");
                    response.setSuccess(true);
                }
            }
        } else if (operateTypeEnum == OperateTypeEnum.Approve) {
            result = heraUserService.updateEffective(id, "1");
            if (result > 0) {
                HeraUser user = heraUserService.findById(id);
                if (user != null) {
                    HeraFile file = heraFileService.findDocByOwner(user.getName());
                    if (file == null) {
                        Integer integer = heraFileService.insert(HeraFile.builder().name("个人文档").gmtModified(new Date()).gmtCreate(new Date()).owner(user.getName()).type(1).build());
                        if (integer <= 0) {
                            return new JsonResponse(false, "新增文档失败，请联系管理员");
                        }
                    }
                }
                response.setMessage("审核通过");
                response.setSuccess(true);
            }
        } else if (operateTypeEnum == OperateTypeEnum.Refuse) {
            result = heraUserService.updateEffective(id, "0");
            if (result > 0) {
                response.setMessage("审核拒绝");
                response.setSuccess(true);
            }
        }
        return response;
    }

    public enum OperateTypeEnum {
        Delete("1"), Approve("2"), Refuse("3");
        private final String operateType;

        OperateTypeEnum(String type) {
            this.operateType = type;
        }

        public static OperateTypeEnum parse(String operateType) {
            Optional<OperateTypeEnum> option = Arrays.stream(OperateTypeEnum.values())
                    .filter(operate -> operate.toString().equals(operateType))
                    .findAny();
            return option.orElse(null);
        }

        @Override
        public String toString() {
            return operateType;
        }
    }
}
