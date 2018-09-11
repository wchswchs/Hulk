package com.mtl.hulk;

public interface HulkSerializer {

    byte[] serialize(Object obj) throws Exception;

    <T> T deSerialize(byte[] param, Class<T> clazz) throws Exception;

}
