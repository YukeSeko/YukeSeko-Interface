package common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 */
@Data
public class OrderInterfaceInfoVo implements Serializable {
    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;
}
