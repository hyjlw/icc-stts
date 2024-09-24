package org.icc.broadcast.common;

import org.icc.broadcast.exception.BaseErrorInfoInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
public class HttpResult {
    private String code;
 
    private String msg;
 
    private Object data;
 
    public HttpResult() {
        this.code = HttpResultCode.SUCCESS.getCode();
        this.msg = HttpResultCode.SUCCESS.getMsg();
    }
 
    public HttpResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
 
    public HttpResult(HttpResultCode restResultCode) {
        this.code = restResultCode.getCode();
        this.msg = restResultCode.getMsg();
    }
 
    public HttpResult(Object data) {
        this.code = HttpResultCode.SUCCESS.getCode();
        this.msg = HttpResultCode.SUCCESS.getMsg();
        this.setData(data);
    }
 
    public static HttpResult success() {
        return new HttpResult();
    }
 
    public static HttpResult fail(String msg) {
        return new HttpResult(HttpResultCode.FAIL.getCode(), msg);
    }

    /**
     * 失败
     */
    public static HttpResult error(BaseErrorInfoInterface errorInfo) {
        HttpResult rb = new HttpResult();
        rb.setCode(errorInfo.getResultCode());
        rb.setMsg(errorInfo.getResultMsg());
        rb.setData(null);
        return rb;
    }

    /**
     * 失败
     */
    public static HttpResult error(String code, String message) {
        HttpResult rb = new HttpResult();
        rb.setCode(code);
        rb.setMsg(message);
        rb.setData(null);
        return rb;
    }

    /**
     * 失败
     */
    public static HttpResult error(String message) {
        HttpResult rb = new HttpResult();
        rb.setCode("-1");
        rb.setMsg(message);
        rb.setData(null);
        return rb;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
