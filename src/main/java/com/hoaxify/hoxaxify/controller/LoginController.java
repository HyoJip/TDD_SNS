package com.hoaxify.hoxaxify.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.model.Views;
import com.hoaxify.hoxaxify.validation.CurrentUser;

@RestController
public class LoginController {
	
	@PostMapping("/api/1.0/login")
	@JsonView(Views.Base.class)
	public User login(@CurrentUser User user) {
		return user;
	}
}
