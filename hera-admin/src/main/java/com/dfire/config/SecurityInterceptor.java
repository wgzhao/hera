package com.dfire.config;

import com.dfire.controller.BaseHeraController;
import com.dfire.core.util.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecurityInterceptor
        extends HandlerInterceptorAdapter
{
    public final static String TOKEN_NAME = "accessToken";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        UnCheckLogin methodAnnotation = method.getMethodAnnotation(UnCheckLogin.class);
        if (methodAnnotation != null) {
            return true;
        }

        UnCheckLogin declaredAnnotation = method.getBeanType().getDeclaredAnnotation(UnCheckLogin.class);

        if (declaredAnnotation != null) {
            return true;
        }

        String heraToken = JwtUtils.getTokenFromHeader(TOKEN_NAME, request);
        if (StringUtils.isNotBlank(heraToken) && JwtUtils.verifyToken(heraToken)) {
            return true;
        }
        request.getRequestDispatcher("/login").forward(request, response);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception
    {
        BaseHeraController.remove();
        super.postHandle(request, response, handler, modelAndView);
    }
}

