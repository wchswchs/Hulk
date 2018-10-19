package com.mtl.hulk.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mtl.hulk.HulkSerializer;

public class KryoSerializer implements HulkSerializer {

    @Override
    public boolean write(Object obj, Class<?> clazz, Output output) throws Exception {
        //获取kryo对象
        Kryo kryo = new Kryo();
        kryo.register(clazz);
        kryo.writeObject(output, obj);
        output.flush();
        return true;
    }

    @Override
    public <T> T read(Class<T> clazz, Input input) throws Exception {
        T object;
        Kryo kryo = new Kryo();
        object = kryo.readObject(input, clazz);
        input.close();
        return object;
    }

}
