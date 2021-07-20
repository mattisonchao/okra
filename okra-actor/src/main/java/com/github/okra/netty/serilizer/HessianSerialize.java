package com.github.okra.netty.serilizer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class HessianSerialize implements Serializer {
    @Override
    public <T extends Serializable> byte[] serialize(T obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(obj);
            hessian2Output.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Serialize fail");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T deserialize(byte[] bytes, Class<T> klass) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
            return (T) hessian2Input.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("deserialize fail");
        }
    }
}
