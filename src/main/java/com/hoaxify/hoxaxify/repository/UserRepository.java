package com.hoaxify.hoxaxify.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hoaxify.hoxaxify.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	public User findByUsername(String username);

	public Page<User> findAllByUsernameNot(String username, Pageable pageable);
}
