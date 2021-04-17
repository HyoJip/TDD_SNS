package com.hoaxify.hoxaxify.common;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ApiError {
	private int status;
	private String message;
	private String url;
	private Map<String, String> validationErrors;
	
	public ApiError(int status, String message, String url) {
		this.status = status;
		this.message = message;
		this.url = url;
	}
}
