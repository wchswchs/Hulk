package com.mtl.hulk.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HulkSerializer<T> {

    protected ThreadLocal<T> _Serializer = new ThreadLocal<T>();

    private final Logger logger = LoggerFactory.getLogger(HulkSerializer.class);

    public T getSerializer(Class<T> clazz) {
        T obj = null;

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            logger.error("Init Error", e);
        } catch (IllegalAccessException e) {
            logger.error("Access Error", e);
        }
        if (_Serializer.get() == null) {
            init(obj);
            _Serializer.set(obj);
        }
        return _Serializer.get();
    }

    public abstract void init(T serializer);

    public abstract byte[] serialize(Object obj);

    public abstract <R> R deserialize(byte[] bytes, Class<R> clazz);

    public abstract  <R> R copy(R origin, Class<R> clazz);

}
