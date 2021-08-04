package com.github.okra.netty.serilizer;

import com.github.okra.utils.HessianSerializeUtils;
import java.io.Serializable;

public class HessianSerialize implements Serializer {
  @Override
  public <T extends Serializable> byte[] serialize(T obj) {
    return HessianSerializeUtils.serialize(obj);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Serializable> T deserialize(byte[] bytes, Class<T> klass) {
    return HessianSerializeUtils.deserialize(bytes);
  }
}
