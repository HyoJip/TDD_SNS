package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

	@Autowired
	TestEntityManager testEntityManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Test @DisplayName("[findByUsername] DB에 일치하는 회원 있을 때, User 받음")
	public void findByUsername_whenUserIsExists_returnsUser() {
		User user = TestUtil.createValidUser();
		testEntityManager.persist(user);
		User inDB = userRepository.findByUsername(user.getUsername());
		assertThat(inDB).isNotNull();
	}
	@Test @DisplayName("[findByUsername] DB에 일치하는 회원 없을 때, Null 받음")
	public void findByUsername_whenUserIsNotExists_returnsNull() {
		User inDB = userRepository.findByUsername("anonymous");
		assertThat(inDB).isNull();
	}
}
