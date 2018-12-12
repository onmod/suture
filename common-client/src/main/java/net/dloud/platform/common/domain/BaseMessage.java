package net.dloud.platform.common.domain;

/**
 * @author QuDasheng
 * @create 2018-10-11 09:59
 **/
public class BaseMessage {
    /**
     * 凭证
     */
    private String proof;

    /**
     * 是否允许不同组消费，开发环境强制false
     */
    private Boolean only;

    /**
     * 来自于的系统分组，会强制设置
     */
    private String  group;

    /**
     * 指定用于消费的bean，必填
     */
    private String bean;


    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public Boolean getOnly() {
        return only;
    }

    public void setOnly(Boolean only) {
        this.only = only;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    @Override
    public String toString() {
        return "BaseMessage{" +
                "proof=" + proof +
                ", only=" + only +
                ", group='" + group + '\'' +
                ", bean='" + bean + '\'' +
                '}';
    }
}
