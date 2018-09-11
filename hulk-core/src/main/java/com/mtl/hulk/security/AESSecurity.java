package com.mtl.hulk.security;

import com.alibaba.fastjson.JSONObject;
import com.mtl.hulk.HulkSecurity;
import com.mtl.hulk.util.CryptUtil;

import java.security.SecureRandom;

public class AESSecurity implements HulkSecurity {

    public static final int KEY_LEN = 128/8;
    public static final int SALT_LEN = 16;

    private byte[] key;
    private byte[] salt;

    @Override
    public byte[] encrypt(Object data) {
        SecureRandom random = new SecureRandom();
        this.key = random.generateSeed(KEY_LEN);
        this.salt = random.generateSeed(SALT_LEN);
        byte[] keyCandidate = CryptUtil.hmacSha256(salt, JSONObject.toJSONString(data));
        return CryptUtil.aesEncrypt(key, keyCandidate);
    }

    @Override
    public Object decrypt(byte[] data, Class<?> clazz) {
        return JSONObject.parseObject(new String(CryptUtil.aesDecrypt(this.key, data)), clazz);
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

}
