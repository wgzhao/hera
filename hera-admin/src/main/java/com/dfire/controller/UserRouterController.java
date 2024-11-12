package com.dfire.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.dfire.common.entity.vo.BaseRestVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserRouterController
{
    @GetMapping("/get-async-routes")
    public BaseRestVo getAsyncRoutes()
    {
        String routeStr = "{\n" +
                "  path: \"/permission\",\n" +
                "  meta: {\n" +
                "    title: \"权限管理\",\n" +
                "    icon: \"ep:lollipop\",\n" +
                "    rank: 10\n" +
                "  },\n" +
                "  children: [\n" +
                "    {\n" +
                "      path: \"/permission/page/index\",\n" +
                "      name: \"PermissionPage\",\n" +
                "      meta: {\n" +
                "        title: \"页面权限\",\n" +
                "        roles: [\"admin\", \"common\"]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      path: \"/permission/button\",\n" +
                "      meta: {\n" +
                "        title: \"按钮权限\",\n" +
                "        roles: [\"admin\", \"common\"]\n" +
                "      },\n" +
                "      children: [\n" +
                "        {\n" +
                "          path: \"/permission/button/router\",\n" +
                "          component: \"permission/button/index\",\n" +
                "          name: \"PermissionButtonRouter\",\n" +
                "          meta: {\n" +
                "            title: \"路由返回按钮权限\",\n" +
                "            auths: [\n" +
                "              \"permission:btn:add\",\n" +
                "              \"permission:btn:edit\",\n" +
                "              \"permission:btn:delete\"\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          path: \"/permission/button/login\",\n" +
                "          component: \"permission/button/perms\",\n" +
                "          name: \"PermissionButtonLogin\",\n" +
                "          meta: {\n" +
                "            title: \"登录接口返回按钮权限\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> routes = JSON.parseObject(routeStr, new TypeReference<Map<String, Object>>() {});

        return new BaseRestVo(true, "success", 200, routes);
    }
}
