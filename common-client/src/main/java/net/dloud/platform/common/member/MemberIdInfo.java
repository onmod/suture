package net.dloud.platform.common.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author QuDasheng
 * @create 2018-10-04 00:13
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdInfo {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 上一次登录时间
     */
    private Timestamp loginAt;
}
