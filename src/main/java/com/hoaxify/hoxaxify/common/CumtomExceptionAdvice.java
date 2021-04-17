package com.hoaxify.hoxaxify.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CumtomExceptionAdvice {
	
	@ExceptionHandler({MethodArgumentNotValidException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
		
		BindingResult result = exception.getBindingResult();
		Map<String, String> validationErrors = new HashMap<>(); 
		
		for (FieldError error : result.getFieldErrors()) {
			validationErrors.put(error.getField(), error.getDefaultMessage());
		}
		
		return new ApiError(400, "유효하지 않은 데이터", request.getServletPath(), validationErrors);
	}
}
