package com.hoaxify.hoxaxify.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.service.FileService;

@RestController
@RequestMapping("/api/1.0")
public class FileUploadController {

	@Autowired
	FileService fileService;
	
	@PostMapping("/hoaxes/upload")
	public FileAttachment uploadForHoaxes(MultipartFile file) {
		return fileService.saveAttachment(file);
	}
}
