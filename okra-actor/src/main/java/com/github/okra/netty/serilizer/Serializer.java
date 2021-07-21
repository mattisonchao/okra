package com.github.okra.netty.serilizer;

import java.io.Serializable;

public interface Serializer {

  <T extends Serializable> byte[] serialize(T obj);

  <T extends Serializable> T deserialize(byte[] bytes, Class<T> klass);
}
