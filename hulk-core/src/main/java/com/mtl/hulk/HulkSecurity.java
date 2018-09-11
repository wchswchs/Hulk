package com.mtl.hulk;

public interface HulkSecurity {

    byte[] encrypt(Object data);

    Object decrypt(byte[] data, Class<?> clazz);

}
