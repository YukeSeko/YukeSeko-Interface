package common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YukeSeko
 */
@Data
public class LoginUserVo implements Serializable{
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * token
     */
    private String token;

}
