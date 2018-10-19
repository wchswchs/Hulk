package com.mtl.hulk;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public interface HulkSerializer {

    boolean write(Object obj, Class<?> clazz, Output output) throws Exception;

    <T> T read(Class<T> clazz, Input input) throws Exception;

}
