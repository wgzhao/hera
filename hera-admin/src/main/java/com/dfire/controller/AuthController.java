package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.vo.BaseRestVo;
import com.dfire.common.util.StringUtil;
import com.dfire.common.service.HeraUserService;
import com.dfire.core.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    @Autowired private HeraUserService heraUserService;

    @PostMapping("/login")
    public BaseRestVo login(@RequestBody String username, @RequestBody String password)
    {
        HeraUser user = heraUserService.findByName(username);

        if (user == null) {
            return new BaseRestVo(false, "用户不存在", 400, null);
        }
        String pwd = user.getPassword();
        if (! password.isEmpty()) {
            password = StringUtil.EncoderByMd5(password);
            if (pwd.equals(password)) {
                if (user.getIsEffective() == 0) {
                    return new BaseRestVo(false, "审核未通过,请联系管理员", 400, null);
                }
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("accessToken", JwtUtils.createToken(username, String.valueOf(user.getId())));
                // the token expires will 5 days, set the expires datetime to the cookie
                // the datetime format is 'yyyy/MM/dd HH:mm:ss'
                Timestamp ts = new Timestamp(System.currentTimeMillis() + Constants.JWT_TIME_OUT * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String expires = sdf.format(ts);
                userInfo.put("expires", expires);
                userInfo.put("username", username);
                userInfo.put("nickname", username);
                userInfo.put("avatar", null);
                userInfo.put("roles", Collections.emptyList());
                userInfo.put("permissions", Collections.emptyList());
                return new BaseRestVo(true, "登录成功", 200, userInfo);
            }
            else {
                return new BaseRestVo(false, "密码错误，请重新输入", 400, null);
            }
        }
        return new BaseRestVo(false, "请输入密码", 400, null);
    }
}
