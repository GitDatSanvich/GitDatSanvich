package com.gitdatsanvich.common.exception;

/**
 * @author zhen.peng
 * @description 业务异常类
 * @date 2018/3/28 10:44
 */
public class BizException extends BaseException {

    public BizException newInstance(Object... params) {
        BizException ex = new BizException(this.defineCode, this.message);
        ex.setMessage(this.message, params);
        return ex;
    }

    public BizException newInstance(String msgPattern, Object... params) {
        this.message = msgPattern;
        BizException ex = new BizException(this.defineCode, this.message);
        ex.setMessage(this.message, params);
        return ex;
    }
    public BizException newInstance(String message) {
        this.message = message;
        BizException ex = new BizException(this.defineCode, this.message);
        ex.setMessage(this.message);
        return ex;
    }

    public BizException(String defineCode, String msg) {
        super(defineCode, msg);
    }

    /**
     * sweetHome信息异常
     */
    public static final BizException USER_INFO_EXCEPTION = new BizException("E1000001", "{0}");
    /**
     * 文件类异常
     */
    public static final BizException FILE_EXCEPTION = new BizException("E1000002", "{0}");

    /**
     * 法律相关异常
     */
    public static final BizException LAW_EXCEPTION = new BizException("E1000003", "{0}");

    /**
     * CHAT_GPT相关异常
     */
    public static final BizException CHAT_GPT_EXCEPTION = new BizException("E1000004", "{0}");
}
