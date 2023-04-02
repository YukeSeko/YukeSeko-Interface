package com.wzy.api.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * 
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}