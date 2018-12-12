package net.dloud.platform.extend.bucket;

import java.util.List;

/**
 * @author QuDasheng
 * @create 2018-12-10 15:15
 **/
public interface Bucket {

    /**
     * 消费令牌
     * @param numTokens
     * @return
     */
    boolean tryConsume(long numTokens);

    /**
     * 添加令牌
     * @param tokensToAdd
     */
    void addTokens(long tokensToAdd);

    /**
     * 获取存储用的配置
     * @return
     */
    List<Bandwidth> getBandwidths();
}
