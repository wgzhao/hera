package com.dfire.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dfire.core.event.LogHelpUtil;
import com.dfire.common.logs.ErrorLog;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaosuda
 * @date 2018/7/13
 */
public class JwtUtils {

    private final static String secret = "WWW.TWO_D_FIRE.COM/INFRASTRUCTURE";

    private static Algorithm algorithm = null;

    static {
        try {
            algorithm = Algorithm.HMAC256(secret);
        } catch (UnsupportedEncodingException e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            e.printStackTrace();
        }
    }

    public static String createToken(String username, String userId) {
        Map<String, Object> header = new HashMap<>(2);
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        Date expireDate = calendar.getTime();
        return JWT.create().withHeader(header)
                .withClaim("iss", "hera")
                .withClaim("aud", "2dfire")
                .withClaim("username", username)
                .withClaim("userId", userId)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    public static boolean verifyToken(String token) {
        return getClaims(token) != null;
    }


    private static Map<String, Claim> getClaims(String token) {
        DecodedJWT jwt;
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            jwt = verifier.verify(token);
        } catch (Exception e) {
            ErrorLog.error(LogHelpUtil.getSpecificTrace(e));
            ErrorLog.error("token 过期");
            return null;
        }
        return jwt.getClaims();
    }

    public static String getObjectFromToken(String token, String key) {
        Map<String, Claim> claimMap = getClaims(token);
        if (claimMap != null && claimMap.get(key) != null) {
            return claimMap.get(key).asString();
        }
        return null;
    }

    public static String getObjectFromToken(String tokenName, HttpServletRequest request, String key) {
        String token = getValFromCookies(tokenName, request);
        return getObjectFromToken(token, key);
    }

    public static String getValFromCookies(String tokenName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(tokenName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static String getTokenFromHeader(String tokenName, HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth.startsWith("Bearer ")) {
            return auth.substring(7);
        } else {
            return request.getHeader(tokenName);
        }
    }
}
