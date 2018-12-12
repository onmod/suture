package net.dloud.platform.common.domain.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.domain.BaseResult;

/**
 * 资源信息
 *
 * @author QuDasheng
 * @create 2018-08-30 16:46
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class KeystoreResult extends BaseResult {
    /**
     * 连接地址
     */
    private String urls;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 驱动名
     */
    private String driveClass;
}
