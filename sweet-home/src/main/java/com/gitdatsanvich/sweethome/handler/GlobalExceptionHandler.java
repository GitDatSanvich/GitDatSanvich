package com.gitdatsanvich.sweethome.handler;

import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.common.util.DingDingAlert;
import com.gitdatsanvich.common.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author TangChen
 * @date 2021/6/29 16:52
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler({Exception.class})
    public R<Boolean> handleException(Exception e) {
        log.error("发生未知异常", e);
        DingDingAlert.pushAlert("异常！", e.getMessage());
        if (e instanceof BizException) {
            return R.failed(e.getMessage());
        } else {
            return R.failed("未知错误");
        }
    }
}