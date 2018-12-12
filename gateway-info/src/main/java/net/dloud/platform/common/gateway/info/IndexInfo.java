package net.dloud.platform.common.gateway.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author QuDasheng
 * @create 2018-09-12 14:57
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexInfo implements Serializable {
    private static final long serialVersionUID = 3697157856483496739L;

    /**
     * 名字
     */
    private String name;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 签名
     */
    private String hash;
}
