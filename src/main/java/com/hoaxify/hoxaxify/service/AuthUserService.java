package com.hoaxify.hoxaxify.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.UserRepository;

@Service
public class AuthUserService implements UserDetailsService{
	
	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("일치하는 회원을 찾을 수 없습니다");
		}
		return user;
	}
	
}
