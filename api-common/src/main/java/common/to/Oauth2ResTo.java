package common.to;

import lombok.Data;

/**
 * @author YukeSeko
 */
@Data
public class Oauth2ResTo {

    private String access_token;

    private String token_type;

    private int expires_in;

    private String refresh_token;

    private String scope;

    private int created_at;
}
