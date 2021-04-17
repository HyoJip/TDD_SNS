package com.hoaxify.hoxaxify.configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;

import com.hoaxify.hoxaxify.common.ApiError;

@RestController
public class ErrorHandler implements ErrorController {
	
	@Autowired
	private ErrorAttributes errorAttributes;

	@Override
	public String getErrorPath() {
		return "/error";
	}
	
	@RequestMapping("/error")
	public ApiError handleError(WebRequest webRequest) {
		Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.of(Include.MESSAGE));
		
		String message = (String) attributes.get("message");
		String url = (String) attributes.get("path");
		int status = (Integer) attributes.get("status");
		return new ApiError(status, message, url);
	}
}
