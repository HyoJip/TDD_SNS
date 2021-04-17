package com.hoaxify.hoxaxify.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hoaxify.hoxaxify.common.GeneralResponse;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.model.vm.UserUpdateVM;
import com.hoaxify.hoxaxify.model.vm.UserVM;
import com.hoaxify.hoxaxify.service.UserService;
import com.hoaxify.hoxaxify.validation.CurrentUser;

@RestController
@RequestMapping("/api/1.0")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/users")
	public GeneralResponse signup(@Valid @RequestBody User user) {
		userService.createUser(user);
		return new GeneralResponse("회원 생성 성공");
	}
	
	@GetMapping("/users")
	public Page<UserVM> getUsers(@CurrentUser User user, Pageable pageable) {
		return userService.getUsers(user, pageable).map(UserVM::new);
	}
	
	@GetMapping("/users/{username}")
	public UserVM getUser(@PathVariable String username) {
		return new UserVM(userService.getUser(username));
	}
	
	@PutMapping("/users/{id:[\\d]+}")
	@PreAuthorize("#id == principal.id")
	public UserVM putUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate) {
		User user = userService.update(id, userUpdate);
		return new UserVM(user);
	}
}
