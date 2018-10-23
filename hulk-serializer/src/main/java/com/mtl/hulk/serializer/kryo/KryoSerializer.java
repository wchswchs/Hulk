package com.mtl.hulk.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mtl.hulk.serializer.HulkSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.BitSet;

public class KryoSerializer extends HulkSerializer<Kryo> {

    @Override
    public void init(Kryo kryo) {
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.register(BitSet.class, new BitSetSerializer());
    }

    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = getSerializer(Kryo.class);
        Output output = new Output(1024, 8 * 1024 * 1024);
        kryo.writeObject(output, obj);
        return output.toBytes();
    }

    @Override
    public <R> R deserialize(byte[] bytes, Class<R> clazz) {
        Kryo kryo = getSerializer(Kryo.class);
        Input input = new Input(bytes);
        return kryo.readObjectOrNull(input, clazz);
    }

    @Override
    public <R> R copy(R origin, Class<R> clazz) {
        byte[] bytes = serialize(origin);
        return deserialize(bytes, clazz);
    }

}
