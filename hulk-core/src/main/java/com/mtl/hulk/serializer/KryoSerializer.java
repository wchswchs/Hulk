package com.mtl.hulk.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mtl.hulk.serializer.kryo.BitSetSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.BitSet;

public class KryoSerializer {

    private static ThreadLocal<Kryo> _Kryo = new ThreadLocal<>();

    public static byte[] serialize(Object obj) {
        Kryo kryo = getKryo();
        Output output = new Output(1024, 8 * 1024 * 1024);
        _Kryo.get().writeObject(output, obj);
        return output.toBytes();
    }

    public static  <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = getKryo();
        Input input = new Input(bytes);
        return kryo.readObject(input, clazz);
    }

    public <T> T copy(T origin, Class<T> clazz) {
        byte[] bytes = serialize(origin);
        return deserialize(bytes, clazz);
    }

    public static Kryo getKryo() {
        if (_Kryo.get() == null) {
            Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            kryo.register(BitSet.class, new BitSetSerializer());
            _Kryo.set(kryo);
        }
        return _Kryo.get();
    }

}
