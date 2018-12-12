package net.dloud.platform.common.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * 用户详情
 *
 * @author QuDasheng
 * @create 2018-08-23 18:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MemberFullInfo extends MemberInfo {
    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 积分
     */
    private Long point;

    /**
     * 上一次登录时间
     */
    private Timestamp loginAt;

    /**
     * 注册时间
     */
    private Timestamp createdAt;
}
