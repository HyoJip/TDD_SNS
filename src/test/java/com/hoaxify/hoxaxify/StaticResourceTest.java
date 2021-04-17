package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.hoaxify.hoxaxify.configuration.AppConfiguration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaticResourceTest {
	
	@Autowired
	AppConfiguration appConfiguration;
	
	@Autowired
	MockMvc mockMvc;
	
	@AfterEach
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentPath()));
	}

	@Test @DisplayName("[파일 업로드] 어플 실행 될 때, 미디어 폴더 존재")
	public void checkStaticFolder_whenAppIsInitialized_uploadFolderMustExist() {
		File uploadFolder = new File(appConfiguration.getUploadPath());
		boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();
		assertThat(uploadFolderExist).isTrue();
	}
	@Test @DisplayName("[파일 업로드] 어플 실행 될 때, 프로필이미지 폴더 존재")
	public void checkStaticFolder_whenAppIsInitialized_profileImageFolderMustExist() {
		File profileImageFolder = new File(appConfiguration.getFullProfileImagesPath());
		boolean profileImageFolderExist = profileImageFolder.exists() && profileImageFolder.isDirectory();
		assertThat(profileImageFolderExist).isTrue();
	}
	@Test @DisplayName("[파일 업로드] 어플 실행 될 때, 첨부파일 폴더 존재")
	public void checkStaticFolder_whenAppIsInitialized_attachmentFolderMustExist() {
		File attachmentFolder = new File(appConfiguration.getFullAttachmentPath());
		boolean attachmentFolderExist = attachmentFolder.exists() && attachmentFolder.isDirectory();
		assertThat(attachmentFolderExist).isTrue();
	}
	@Test @DisplayName("[파일 다운로드] 프로필이미지 폴더에 해당 파일이 존재할 경우, 200 받음")
	public void getStaticFile_whenImageExistInProfileUploadFolder_receiveOk() throws Exception {
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
		
		File target = new File(appConfiguration.getFullProfileImagesPath()+ "/" + fileName);
		FileUtils.copyFile(source, target);
		
		mockMvc
			.perform(get("/images/"+ appConfiguration.getProfileImagesFolder()+ "/" + fileName))
			.andExpect(status().isOk());
	}
	@Test @DisplayName("[파일 다운로드] 첨부파일 폴더에 해당 파일이 존재할 경우, 200 받음")
	public void getStaticFile_whenImageExistInAttachmentFolder_receiveOk() throws Exception {
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
		
		File target = new File(appConfiguration.getFullAttachmentPath()+ "/" + fileName);
		FileUtils.copyFile(source, target);
		
		mockMvc
		.perform(get("/images/"+ appConfiguration.getAttachmentFolder()+ "/" + fileName))
		.andExpect(status().isOk());
	}
	@Test @DisplayName("[파일 다운로드] 첨부파일 폴더에 해당 파일이 존재하지 않을 경우, 404 받음")
	public void getStaticFile_whenImageDoesNotExist_receiveNotFound() throws Exception {
		mockMvc
		.perform(get("/images/"+ appConfiguration.getAttachmentFolder()+ "/not-exist-filename.png"))
		.andExpect(status().isNotFound());
	}
	@Test @DisplayName("[파일 다운로드] 첨부파일 폴더에 해당 파일이 존재할 경우, 캐시 헤더 받음")
	public void getStaticFile_whenAttachmentFileExist_receiveOkWithCacheHeaders() throws Exception {
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
		
		File target = new File(appConfiguration.getFullAttachmentPath() + "/" + fileName);
		FileUtils.copyFile(source, target);
		
		MvcResult result = mockMvc.perform(get("/images/"+ appConfiguration.getAttachmentFolder()+ "/" + fileName)).andReturn();
		String cacheControl = result.getResponse().getHeaderValue("Cache-Control").toString();
		assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
	}
}
