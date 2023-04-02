package common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * echarts需要返回的数据
 * @author YukeSeko
 */
@Data
public class EchartsVo implements Serializable {
    private Long count;

    private String date;
}
