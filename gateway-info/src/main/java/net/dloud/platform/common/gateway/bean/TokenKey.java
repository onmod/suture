package net.dloud.platform.common.gateway.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author QuDasheng
 * @create 2018-10-22 16:48
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenKey {
    private String token;

    private String tenant;

    private String group;

    private String inject;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenKey tokenKey = (TokenKey) o;
        return Objects.equals(getToken(), tokenKey.getToken()) &&
                Objects.equals(getTenant(), tokenKey.getTenant()) &&
                Objects.equals(getGroup(), tokenKey.getGroup()) &&
                Objects.equals(getInject(), tokenKey.getInject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken(), getTenant(), getGroup(), getInject());
    }

    public String getKey() {
        return tenant + ":" + token;
    }
}
