package common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户可调用次数to
 * @author YukeSeko
 */
@Data
public class LeftNumUpdateTo implements Serializable {

    /**
     * 调用用户 id
     */
    private Long userId;

    /**
     * 接口 id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Long lockNum;
}
