package com.hoaxify.hoxaxify.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.model.vm.UserUpdateVM;
import com.hoaxify.hoxaxify.repository.UserRepository;
import com.hoaxify.hoxaxify.validation.NotFoundException;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final FileService fileService;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileService = fileService;
	}

	public User createUser(User user) {
		String hashedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(hashedPassword);
		return userRepository.save(user);
	}

	public Page<User> getUsers(User user, Pageable pageable) {
		if (user != null) {
			return userRepository.findAllByUsernameNot(user.getUsername(), pageable);
		}
		return userRepository.findAll(pageable);
	}

	public User getUser(String username) {
		User user = userRepository.findByUsername(username);
		if (user == null)
			throw new NotFoundException(username + ": 일치하는 회원이 없습니다");
		return user;
	}

	public User update(long id, UserUpdateVM userUpdate) {
		User inDB = userRepository.getOne(id);
		inDB.setDisplayName(userUpdate.getDisplayName());
		if (userUpdate.getImage() != null) {
			String savedImageName = fileService.saveProfileImage(userUpdate.getImage());
			fileService.deleteProfileImage(inDB.getImage());
			inDB.setImage(savedImageName);
		}
		return userRepository.save(inDB);
	}

}
