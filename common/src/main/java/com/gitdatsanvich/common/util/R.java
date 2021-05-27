package com.gitdatsanvich.common.util;

import com.gitdatsanvich.common.exception.BizException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author pengzhen
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class R<T> implements Serializable {

    private static final int SUCCESS = 0;

    private static final int FAIL = -1;

    private static final int OTHER = 1;

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;


    @Getter
    @Setter
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, null);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, null);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> R<T> failed() {
        return restResult(null, FAIL, null);
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> R<T> failed(T data) {
        return restResult(data, FAIL, null);
    }

    public static <T> R<T> failed(T data, String msg) {
        return restResult(data, FAIL, msg);
    }

    public static <T> R<T> other() {
        return restResult(null, OTHER, null);
    }

    public static <T> R<T> other(T data) {
        return restResult(data, OTHER, null);
    }

    public static <T> R<T> other(T data, String msg) {
        return restResult(data, OTHER, msg);
    }

    public static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    /**
     * 判断返回result是否成功
     *
     * @param r r
     * @return boolean
     */
    public static boolean checkResult(R r) {
        return r.getCode() == 0;
    }

    /**
     * 判断result是否成功 失败抛出指定异常
     *
     * @param r            r
     * @param bizException bizException
     * @param errorInfo    远程调用失败的原因
     */
    public static void checkAndThrow(R r, BizException bizException, String errorInfo) throws BizException {
        if (!checkResult(r)) {
            throw bizException.newInstance(errorInfo);
        }
    }
}
