package com.hoaxify.hoxaxify;

import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.model.User;

public class TestUtil {

	public static User createValidUser() {
		User user = new User();
		user.setUsername("username");
		user.setDisplayName("displayName");
		user.setPassword("P4ssword");
		user.setImage("test-image");
		return user;
	}
	
	public static User createValidUser(String username) {
		User user = createValidUser();
		user.setUsername(username);
		return user;
	}

	public static Hoax createValidHoax() {
		Hoax hoax = new Hoax();
		hoax.setContent("test-content");
		return hoax;
	}
}
