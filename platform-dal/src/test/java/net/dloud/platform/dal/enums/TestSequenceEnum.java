package net.dloud.platform.dal.enums;

import net.dloud.platform.parse.redisson.SequenceEnum;

/**
 * @author QuDasheng
 * @create 2018-10-27 00:49
 **/
public enum  TestSequenceEnum implements SequenceEnum {
    /**
     * 序列生成key
     */
    MEMBER_ID("memberId", "用于生成会员id"),
    TRADE_ID("tradeId", "用于生成订单id"),
    ;

    private String key;

    private String describe;

    TestSequenceEnum(String key, String describe) {
        this.key = key;
        this.describe = describe;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDescribe() {
        return describe;
    }
}
