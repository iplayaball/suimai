package com.study.suimai.product.exception;


import com.study.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;


@Slf4j
@RestControllerAdvice(basePackages = "com.study.suimai.product.controller")
public class SuimaiExpectionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(400, "数据校验出现问题").put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handlerException(Throwable e) {
        log.error("未知错误 {}, {}", e.getMessage(), e.getClass());
        return R.error(400, "未知错误");
    }
}
