package com.hoaxify.hoxaxify.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hoaxify.hoxaxify.configuration.AppConfiguration;
import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.repository.FileAttachmentRepository;

@Service
@EnableScheduling
public class FileService {
	
	private final AppConfiguration appConfiguration;
	private final FileAttachmentRepository fileAttachmentRepository;
	
	private Tika tika;
	
	public FileService(AppConfiguration appConfiguration, FileAttachmentRepository fileAttachmentRepository) {
		this.appConfiguration = appConfiguration;
		this.fileAttachmentRepository = fileAttachmentRepository;
		tika = new Tika();
	}
	
	public String saveProfileImage(String base64Image) {
		String imageName = generateRandomString();
		
		File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);
		byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
		try {
			FileUtils.writeByteArrayToFile(target, decodedBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageName;
	}
	
	public FileAttachment saveAttachment(MultipartFile file) {
		FileAttachment fileAttachment = new FileAttachment();
		fileAttachment.setDate(new Date());
		String randomName = generateRandomString();
		fileAttachment.setName(randomName);
		
		File target = new File(appConfiguration.getFullAttachmentPath() + "/" + randomName);
		try {
			byte[] fileAsByte = file.getBytes();
			fileAttachment.setFileType(detectType(fileAsByte));
			FileUtils.writeByteArrayToFile(target, fileAsByte);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileAttachmentRepository.save(fileAttachment);
	}

	public void deleteProfileImage(String image) {
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath() + "/" + image));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String detectType(byte[] fileArr) {
		return tika.detect(fileArr);
	}
	
	private String generateRandomString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@Scheduled(fixedRate = 60 * 60 * 1000)
	public void cleanupStorage() {
		Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
		List<FileAttachment> oldFiles = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
		for (FileAttachment file : oldFiles) {
			deleteAttachmentImage(file.getName());
			fileAttachmentRepository.deleteById(file.getId());
		}
	}

	public void deleteAttachmentImage(String image) {
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullAttachmentPath() + "/" + image));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
