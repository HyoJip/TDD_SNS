package com.hoaxify.hoxaxify.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeneralResponse {

	String message;
	
	public GeneralResponse(String message) {
		this.message = message;
	}
}
