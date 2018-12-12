package net.dloud.platform.parse.dubbo.serialization;


import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author QuDasheng
 * @create 2018-12-10 10:06
 **/
public class KryoSerializationImpl implements Serialization {

    @Override
    public byte getContentTypeId() {
        return 8;
    }

    @Override
    public String getContentType() {
        return "x-application/kryo";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new KryoObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new KryoObjectInput(is);
    }
}
