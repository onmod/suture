package net.dloud.platform.common.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class MemberInfo extends MemberIdInfo {
    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Byte gender;

    /**
     * 年龄
     */
    private Short age;

    /**
     * 城市
     */
    private String city;
}
