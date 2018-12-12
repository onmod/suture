package net.dloud.platform.parse.redisson.serialization;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.dloud.platform.common.serialize.KryoBaseUtil;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 * @author QuDasheng
 * @create 2018-12-10 14:03
 **/
public class KryoCodec extends BaseCodec {

    @Override
    public Decoder<Object> getValueDecoder() {
        return (buf, state) -> {
            try (final Input in = new Input(new ByteBufInputStream(buf))) {
                return KryoBaseUtil.readClassAndObject(in);
            }
        };
    }

    @Override
    public Encoder getValueEncoder() {
        return in -> {
            try (final ByteBufOutputStream bos = new ByteBufOutputStream(ByteBufAllocator.DEFAULT.buffer());
                 final Output output = new Output(bos)) {
                KryoBaseUtil.writeClassAndObject(output, in);
                return bos.buffer();
            }
        };
    }
}
