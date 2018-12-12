package net.dloud.platform.common.gateway.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author QuDasheng
 * @create 2018-09-25 18:40
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvokeKey {
    private String group;

    private String invoke;

    private int length;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeKey invokeKey = (InvokeKey) o;
        return getLength() == invokeKey.getLength() &&
                Objects.equals(getGroup(), invokeKey.getGroup()) &&
                Objects.equals(getInvoke(), invokeKey.getInvoke());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getInvoke(), getLength());
    }
}
