package net.dloud.platform.common.gateway;

/**
 * 必须继承 BaseInjectEnum
 *
 * @author QuDasheng
 * @title 注入类型
 * @create 2018-09-14 09:45
 **/
public enum InjectEnum implements BaseInjectEnum {
    /**
     * 会员id 注解参数类型必须是 Long、Map或包含userId属性的Bean
     */
    MEMBER_ID(1, "member.memberService.getUserIdByToken"),

    /**
     * 会员信息 注解参数类型必须是 MemberInfo
     */
    MEMBER_SIMPLE(2, "member.memberService.getUserByToken"),

    /**
     * 会员信息 注解参数类型必须是 MemberFullInfo
     */
    MEMBER_EXTEND(3, "member.memberService.getUserFullByToken");


    private int level;

    private String method;

    InjectEnum(int level, String method) {
        this.level = level;
        this.method = method;
    }

    public static int getLevel(String name) {
        try {
            return valueOf(name).level;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String acquire() {
        return MEMBER_ID.toString();
    }
}
