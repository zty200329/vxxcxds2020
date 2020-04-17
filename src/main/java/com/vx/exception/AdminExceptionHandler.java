package com.vx.exception;

import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@ControllerAdvice
public class AdminExceptionHandler {
 
 
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public String getException(Exception e){
        return e.getMessage();
    }
 
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public String getException(MethodArgumentNotValidException e){
        List<ObjectError> errors =e.getBindingResult().getAllErrors();
        StringBuffer sb = new StringBuffer();
        errors.stream().forEach(s->sb.append(s.getDefaultMessage()));
        return sb.toString();
    }
}