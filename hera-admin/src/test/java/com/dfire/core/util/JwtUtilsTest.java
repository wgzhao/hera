package com.dfire.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by xiaosuda on 2018/7/13.
 */
public class JwtUtilsTest {

    @Test
    public void createToken() {

    }

    @Test
    public void verifyToken() {
        String token = JwtUtils.createToken("小苏打", "2");
        assertTrue(JwtUtils.verifyToken(token));
    }

    @Test
    public void getObjectFromToken() {
        String token = JwtUtils.createToken("小苏打", "1");
        assertEquals(JwtUtils.getObjectFromToken(token, "username"), "小苏打");
        assertEquals(JwtUtils.getObjectFromToken(token, "userId"), "1");
    }
}