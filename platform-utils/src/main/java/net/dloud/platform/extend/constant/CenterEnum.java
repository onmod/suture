package net.dloud.platform.extend.constant;

/**
 * @author QuDasheng
 * @create 2018-10-06 22:37
 **/
public enum CenterEnum {
    /**
     * 全部系统
     */
    PLATFORM_CENTER(10000, "platform"),
    GATEWAY_CENTER(10001, "gateway"),
    ;

    private int id;

    private String name;


    CenterEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return id + "-" + PlatformConstants.MODE;
    }

    public String consumerGroup() {
        return id + "-" + PlatformConstants.MODE + "-" + PlatformConstants.GROUP;
    }
}
