package com.hoaxify.hoxaxify.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.UserRepository;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public boolean isValid(String username, ConstraintValidatorContext context) {
		User inDB = userRepository.findByUsername(username);
		if (inDB == null) {
			return true;
		}
		return false;
	}

}
