package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-03 17:02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentInfo implements Serializable {
    private static final long serialVersionUID = 3663819337664142892L;

    /**
     * 全名
     */
    private String author;

    /**
     * 展示
     */
    private String title;

    /**
     * 标题
     */
    private String detail;

    /**
     * 参数
     */
    private Map<String, String> params;

    /**
     * 返回
     */
    private String returned;

    /**
     * 添加时间
     */
    private String time;

    /**
     * 异常
     */
    private List<String> exception;
}
