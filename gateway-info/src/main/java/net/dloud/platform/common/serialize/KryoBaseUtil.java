package net.dloud.platform.common.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.crypto.tink.subtle.Base64;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author QuDasheng
 * @create 2018-09-18 12:09
 **/
public class KryoBaseUtil {
    /**
     * 每个线程的 Kryo 实例
     */
    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();

            /**
             * 不要轻易改变这里的配置！更改之后，序列化的格式就会发生变化，
             * 上线的同时就必须清除 Redis 里的所有缓存，
             * 否则那些缓存再回来反序列化的时候，就会报错
             */
            //支持对象循环引用（否则会栈溢出）
            //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置
            kryo.setReferences(true);
            //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
            //默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置
            kryo.setRegistrationRequired(false);
            //使用前后兼容的序列化器
            SerializerFactory.CompatibleFieldSerializerFactory defaultFactory = new SerializerFactory.CompatibleFieldSerializerFactory();
            defaultFactory.getConfig().setReadUnknownTagData(true);
            kryo.setDefaultSerializer(defaultFactory);

            //5.0以下修复集合的NPE问题
            //((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
            //        .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());

            return kryo;
        }
    };

    /**
     * 获得当前线程的 Kryo 实例
     *
     * @return 当前线程的 Kryo 实例
     */
    public static Kryo getInstance() {
        return kryoLocal.get();
    }

    //-----------------------------------------------
    //          序列化/反序列化对象，及类型信息
    //          序列化的结果里，包含类型的信息
    //          反序列化时不再需要提供类型
    //-----------------------------------------------

    /**
     * 将对象【及类型】序列化为字节数组
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字节数组
     */
    public static <T> byte[] writeToByteArray(T obj) {
        return writeToByteArray(obj, false, false);
    }

    public static <T> byte[] writeToByteArray(T obj, boolean cut, boolean big) {
        int outputSize = 8 * 1024;
        if (big) {
            outputSize = 256 * 1024;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream, outputSize)) {
            Kryo kryo = getInstance();
            kryo.writeClassAndObject(output, obj);

            if (cut) {
                return Snappy.compress(output.toBytes());
            } else {
                return output.toBytes();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> void writeClassAndObject(Output output, T obj) {
        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, obj);
    }

    /**
     * 将对象【及类型】序列化为 String
     * 利用了 Base64 编码
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字符串
     */
    public static <T> String writeToString(T obj) {
        return Base64.encode(writeToByteArray(obj));
    }

    /**
     * 将字节数组反序列化为原对象
     *
     * @param byteArray writeToByteArray 方法序列化后的字节数组
     * @param <T>       原对象的类型
     * @return 原对象
     */
    public static <T> T readFromByteArray(byte[] byteArray) {
        return readFromByteArray(byteArray, false, false);
    }

    public static <T> T readFromByteArray(byte[] byteArray, boolean big) {
        return readFromByteArray(byteArray, false, big);
    }

    @SuppressWarnings("unchecked")
    public static <T> T readFromByteArray(byte[] byteArray, boolean cut, boolean big) {
        if (null == byteArray) {
            return null;
        }

        int inputSize = 8 * 1024;
        if (big) {
            inputSize = 256 * 1024;
        }
        if (cut) {
            try {
                byteArray = Snappy.uncompress(byteArray);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
             Input input = new Input(byteArrayInputStream, inputSize)) {
            Kryo kryo = getInstance();
            return (T) kryo.readClassAndObject(input);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readClassAndObject(Input input) {
        Kryo kryo = getInstance();
        return (T) kryo.readClassAndObject(input);
    }

    /**
     * 将 String 反序列化为原对象
     * 利用了 Base64 编码
     *
     * @param str writeToString 方法序列化后的字符串
     * @param <T> 原对象的类型
     * @return 原对象
     */
    public static <T> T readFromString(String str) {
        return readFromByteArray(Base64.decode(str));
    }

    //-----------------------------------------------
    //          只序列化/反序列化对象
    //          序列化的结果里，不包含类型的信息
    //-----------------------------------------------

    /**
     * 将对象序列化为字节数组
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字节数组
     */
    public static <T> byte[] writeObjectToByteArray(T obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = getInstance();
            kryo.writeObject(output, obj);

            return output.toBytes();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 将对象序列化为 String
     * 利用了 Base64 编码
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return 序列化后的字符串
     */
    public static <T> String writeObjectToString(T obj) {
        return Base64.encode(writeObjectToByteArray(obj));
    }

    public static <T> void writeObject(Output output, T obj) {
        Kryo kryo = getInstance();
        kryo.writeObject(output, obj);
    }

    /**
     * 将字节数组反序列化为原对象
     *
     * @param byteArray writeToByteArray 方法序列化后的字节数组
     * @param clazz     原对象的 Class
     * @param <T>       原对象的类型
     * @return 原对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T readObjectFromByteArray(byte[] byteArray, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = getInstance();
            return kryo.readObject(input, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 将 String 反序列化为原对象
     * 利用了 Base64 编码
     *
     * @param str   writeToString 方法序列化后的字符串
     * @param clazz 原对象的 Class
     * @param <T>   原对象的类型
     * @return 原对象
     */
    public static <T> T readObjectFromString(String str, Class<T> clazz) {
        return readObjectFromByteArray(Base64.decode(str), clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(Input input, Class<T> clazz) {
        Kryo kryo = getInstance();
        return kryo.readObject(input, clazz);
    }
}