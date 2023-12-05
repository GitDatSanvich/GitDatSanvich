package com.gitdatsanvich.common.exception;

import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author zhen.peng
 * @description 异常基类
 * @date 2018/3/28 11:22
 */
public class BaseException extends Exception {

    private static final long serialVersionUID = -5097768787801034398L;
    protected String id;
    protected String message;
    protected String defineCode;
    protected String realClassName;

    protected BaseException(String defineCode) {
        this.defineCode = defineCode;
        initId();
    }

    protected BaseException(String defineCode, String msg) {
        this.defineCode = defineCode;
        this.message = msg;
        initId();
    }

    private void initId() {
        this.id = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message, Object... args) {
        this.message = MessageFormat.format(message, args);
    }

    public String getDefineCode() {
        return this.defineCode;
    }

    public static <T extends BaseException> T newException(T exception, String message, Object... args) {
        if (exception == null) {
            throw new RuntimeException("no exception instance specified");
        }
        try {
            Constructor constructor = exception.getClass().getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            T newException = (T) constructor.newInstance(exception.getDefineCode());
            newException.setMessage(message, args);
            return newException;
        } catch (Throwable e) {
            throw new RuntimeException("create exception instance fail : " + e.getMessage(), e);
        }
    }

    public boolean codeEquals(BaseException e) {
        if (e == null) {
            return false;
        }
        if (!e.getClass().equals(getClass())) {
            return false;
        }
        if (!e.getDefineCode().equals(getDefineCode())) {
            return false;
        }
        return true;
    }

    public BaseException upcasting() {
        if (getClass().equals(BaseException.class)) {
            return this;
        }
        BaseException superexception = new BaseException(this.defineCode);
        superexception.message = this.message;
        superexception.realClassName = getClass().getName();
        superexception.id = this.id;
        superexception.setStackTrace(getStackTrace());
        return superexception;
    }

    public BaseException downcasting() {
        if ((this.realClassName == null) || (BaseException.class.getName().equals(this.realClassName))) {
            return this;
        }
        Class clz = null;
        try {
            clz = Class.forName(this.realClassName);
        } catch (Exception e) {
        }
        if (clz == null) {
            return this;
        }
        try {
            Constructor constructor = clz.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            BaseException newException = (BaseException) constructor.newInstance(new Object[]{this.defineCode});
            newException.message = this.message;
            newException.id = this.id;
            newException.setStackTrace(getStackTrace());
            return newException;
        } catch (Throwable e) {
        }
        return this;
    }

    public String getRealClassName() {
        if (this.realClassName == null) {
            return getClass().getName();
        }
        return this.realClassName;
    }

    public void mergeStackTrace(StackTraceElement[] stackTrace) {
        setStackTrace((StackTraceElement[]) ArrayUtils.addAll(getStackTrace(), stackTrace));
    }

    public StackTraceElement[] getCoreStackTrace() {
        List<StackTraceElement> list = new ArrayList<>();
        for (StackTraceElement traceEle : getStackTrace()) {
            if (traceEle.getClassName().startsWith("com.boot")) {
                list.add(traceEle);
            }
        }
        StackTraceElement[] stackTrace = new StackTraceElement[list.size()];
        return (StackTraceElement[]) list.toArray(stackTrace);
    }

    public String getCoreStackTraceStr() {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement traceEle : getCoreStackTrace()) {
            sb.append("\n").append(traceEle.toString());
        }
        return sb.toString();
    }
}

