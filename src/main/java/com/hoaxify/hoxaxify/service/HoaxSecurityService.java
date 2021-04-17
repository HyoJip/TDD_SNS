package com.hoaxify.hoxaxify.service;

import org.springframework.stereotype.Service;

import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.HoaxRepository;
import com.hoaxify.hoxaxify.validation.NotFoundException;

@Service
public class HoaxSecurityService {

	private final HoaxRepository hoaxRepository;
	
	public HoaxSecurityService(HoaxRepository hoaxRepository) {
		this.hoaxRepository = hoaxRepository;
	}
	
	public boolean isAllowedToDelete(long id, User loggedInUser) {
		Hoax inDB = hoaxRepository.findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 피드"));
		return loggedInUser.getId() == inDB.getUser().getId();
	}
}
