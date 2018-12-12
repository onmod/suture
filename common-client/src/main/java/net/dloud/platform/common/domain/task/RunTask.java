package net.dloud.platform.common.domain.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseTask;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author QuDasheng
 * @create 2018-10-14 17:37
 **/
@Data
@ToString(callSuper = true, exclude = {"param"})
@EqualsAndHashCode(callSuper = true)
public class RunTask extends BaseTask implements Delayed {
    /**
     * 开始时间
     */
    private Instant start;

    /**
     * 总需执行次数，-1为无限制
     */
    private Integer total = -1;

    /**
     * 当前执行次数
     */
    private Integer current = 0;

    /**
     * 附加参数
     */
    private byte[] param;


    public RunTask(String id, LocalDateTime start) {
        super.setId(id);
        this.start = start.atZone(ZoneId.systemDefault()).toInstant();
    }

    public RunTask(String id, int delayTime, TimeUnit timeUnit) {
        super.setId(id);
        this.start = LocalDateTime.now().plusSeconds(timeUnit.toSeconds(delayTime))
                .atZone(ZoneId.systemDefault()).toInstant();
    }

    public static void main(String[] args) {
        final DelayQueue<RunTask> runTasks = new DelayQueue<RunTask>();
    }

    public Instant getStart() {
        return start;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    /**
     * 用来判断是否到了截止时间
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return (start.toEpochMilli() - System.currentTimeMillis()) * 1000_000;
    }

    /**
     * 相互批较排序用
     */
    @Override
    public int compareTo(Delayed delayed) {
        RunTask other = (RunTask) delayed;
        return this.getStart().compareTo(other.getStart());
    }
}
