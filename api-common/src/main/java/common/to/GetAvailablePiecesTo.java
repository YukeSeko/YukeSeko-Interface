package common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * @author YukeSeko
 */
@Data
public class GetAvailablePiecesTo implements Serializable {
    /**
     * 接口id
     */
    private Long interfaceId;
}
