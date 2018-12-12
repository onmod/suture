package net.dloud.platform.parse.redisson;

/**
 * @author QuDasheng
 * @create 2018-10-12 09:45
 **/
public interface SequenceEnum {
    /**
     * 获取生成id用的key
     */
    String getKey();

    /**
     * 获取key对应的描述
     */
    String getDescribe();
}
