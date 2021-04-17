package com.hoaxify.hoxaxify.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.FileAttachmentRepository;
import com.hoaxify.hoxaxify.repository.HoaxRepository;

@Service
public class HoaxService {
	
	private final HoaxRepository hoaxRepository;
	private final UserService userService;
	private final FileAttachmentRepository fileAttachmentRepository;
	private final FileService fileService;

	public HoaxService(HoaxRepository hoaxRepository, UserService userService, FileAttachmentRepository fileAttachmentRepository, FileService fileService) {
		this.hoaxRepository = hoaxRepository;
		this.userService = userService;
		this.fileAttachmentRepository = fileAttachmentRepository;;
		this.fileService = fileService;
	}

	public Hoax postHoax(User user, Hoax hoax) {
		hoax.setTimestamp(new Date());
		hoax.setUser(user);
		if (hoax.getAttachment() != null) {
			FileAttachment inDB = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
			hoax.setAttachment(inDB);
			inDB.setHoax(hoax);
		}
		return hoaxRepository.save(hoax);
	}

	public Page<Hoax> getAllHoaxes(String username, Pageable pageable) {
		if (username == null) {
			return hoaxRepository.findAll(pageable);
		} else {
			User user = userService.getUser(username);
			return hoaxRepository.findByUser(user, pageable);
		}
	}

	public Page<Hoax> getOldHoaxes(String username, long id, Pageable pageable) {
		Specification<Hoax> spec = Specification.where(IdLessThan(id));
		if (username == null) {
			return hoaxRepository.findAll(spec, pageable);			
		} else {
			User user = userService.getUser(username);
			return hoaxRepository.findAll(spec.and(isUser(user)), pageable);
		}
	}

	public List<Hoax> getNewHoaxes(String username, long id, Pageable pageable) {
		Specification<Hoax> spec = Specification.where(IdGreaterThan(id));
		if (username == null) {
			return hoaxRepository.findAll(spec, pageable.getSort());			
		} else {
			User user = userService.getUser(username);		
			return hoaxRepository.findAll(spec.and(isUser(user)), pageable.getSort());
		}
	}

	public Map<String, Long> getCountOfNewHoaxes(String username, long id) {
		Specification<Hoax> spec = Specification.where(IdGreaterThan(id));
		if (username == null) {
			long newHoaxCount = hoaxRepository.count(spec);
			return Collections.singletonMap("count", newHoaxCount);			
		} else {
			User user = userService.getUser(username);
			long newHoaxCount = hoaxRepository.count(spec.and(isUser(user)));
			return Collections.singletonMap("count", newHoaxCount);			
		}
	}
	
	public void removeHoax(long id) {
		Hoax hoax = hoaxRepository.getOne(id);
		FileAttachment attachment = hoax.getAttachment();
		if (attachment != null) {
			fileService.deleteAttachmentImage(attachment.getName());
		}
		hoaxRepository.deleteById(id);
	}
	
	
	
	
	
	private Specification<Hoax> isUser(User user) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
	}
	
	private Specification<Hoax> IdLessThan(long id) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
	}
	
	private Specification<Hoax> IdGreaterThan(long id) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
	}
}
