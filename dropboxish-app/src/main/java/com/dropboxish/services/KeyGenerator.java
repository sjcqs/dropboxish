package com.dropboxish.services;

import java.io.UnsupportedEncodingException;

/**
 * Created by satyan on 11/22/17.
 * Generate a key for JWT
 */
public class KeyGenerator {
    public static byte[] getKey() throws UnsupportedEncodingException {
        return "C5EA87A8C891072DCFBC87F82EA6AF878053CE84F598E51AAF0D70B7F26B8DD9".getBytes("UTF-8");
    }
}
