package com.hoaxify.hoxaxify.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties("hoaxify")
@Data
public class AppConfiguration {

	String uploadPath;
	
	String profileImagesFolder = "profile";
	String attachmentFolder = "attachment";
	
	public String getFullProfileImagesPath() {
		return uploadPath + "/" + profileImagesFolder;
	}
	
	public String getFullAttachmentPath() {
		return uploadPath + "/" + attachmentFolder;
	}
}
