package common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YukeSeko
 */

public class AuthPhoneNumber {

    /**
     * 验证手机号是否符合要求
     * @param phone
     * @return
     */
    public boolean isPhoneNum(String phone){
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(16[5,6])|(17[0-8])|(18[0-9])|(19[1、5、8、9]))\\d{8}$";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    /**
     * 验证输入的图形验证码是否正确
     * @param captcha
     * @return
     */
    public boolean isCaptcha(String captcha){
        String regex = "/^[0-9]\\d{3}$/";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(captcha);
        return m.matches();
    }
}
