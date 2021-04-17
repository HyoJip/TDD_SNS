package com.hoaxify.hoxaxify.model.vm;

import com.hoaxify.hoxaxify.model.User;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserVM {
	
	private long id;
	private String username;
	private String displayName;
	private String image;
	
	public UserVM(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.displayName = user.getDisplayName();
		this.image = user.getImage();
	}
}
