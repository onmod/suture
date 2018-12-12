package net.dloud.platform.common.domain;

/**
 * @author QuDasheng
 * @create 2018-10-14 01:46
 **/
public class BaseTask {
    /**
     * 任务id
     */
    private String id;

    /**
     * 任务类型
     * 0只执行一次(start)、1间隔时间循环执行(interval)、2指定间隔时间并对应次数执行(design)、9根据cron表达式执行(cron)
     */
    private Byte type;

    /**
     * 任务执行表达式
     */
    private String express;

    /**
     * 在某个任务之后执行、此时表达式无效
     */
    private String after;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getExpress() {
        return express;
    }

    public void setExpress(String express) {
        this.express = express;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    @Override
    public String toString() {
        return "BaseTask{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", express='" + express + '\'' +
                ", after='" + after + '\'' +
                '}';
    }
}
