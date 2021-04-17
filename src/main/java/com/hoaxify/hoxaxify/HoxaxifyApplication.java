package com.hoaxify.hoxaxify;

import java.util.stream.IntStream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.service.UserService;

@SpringBootApplication
public class HoxaxifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HoxaxifyApplication.class, args);
	}
	
	@Bean
	@Profile("!test")
	CommandLineRunner run(UserService userService) {
		return (args) -> {
			IntStream.rangeClosed(1, 15).mapToObj(i -> {
				User user = new User();
				user.setUsername("user" + i);
				user.setDisplayName("displayName" + i);
				user.setPassword("P4ssword");
				return user;
			}).forEach(userService::createUser);
		};
	}
}
