package com.mtl.hulk;

public interface HulkSerializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);

    <T> T copy(T origin, Class<T> clazz);

}
