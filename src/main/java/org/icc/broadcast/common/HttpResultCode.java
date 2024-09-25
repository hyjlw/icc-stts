package org.icc.broadcast.common;

public enum HttpResultCode {
    // 成功
    SUCCESS("200", "success"),
    // 失败
    FAIL("10000", "fail"),
    // 请求方式不支持
    REQUEST_METHOD_NOT_SUPPORTED("10002", "请求方式不支持"),
    // 方法参数不正确
    REQUEST_METHOD_ARGUMENT_NOT_VALID("10003", "方法参数不正确"),
    // 用户名密码解密异常
    USERNAME_PASSWORD_DECRYPT_ERROR("10004", "用户名密码解密异常"),
    // service层错误
    SERVICE_EXCEPTION("10005", "service层错误"),
    // 数据源访问错误
    DATA_SOURCE_ACCESS_ERROR("10006", "数据源访问错误"),
    // 文件内容错误
    INVALID_FILE_DATA("10007", "文件内容错误"),
    // 系统内部错误
    SYSTEM_ERROR("50000", "系统内部错误"),


    WS_INIT_ERROR("70100", "WS init "),


    ;
 
    /**
     * 返回状态码
     */
    private String code;
    /**
     * 返回状态信息
     */
    private String msg;
 
    HttpResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
 
    public String getCode() {
        return code;
    }
 
    public void setCode(String code) {
        this.code = code;
    }
 
    public String getMsg() {
        return msg;
    }
 
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
