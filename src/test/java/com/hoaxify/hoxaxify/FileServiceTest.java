package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hoaxify.hoxaxify.configuration.AppConfiguration;
import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.repository.FileAttachmentRepository;
import com.hoaxify.hoxaxify.service.FileService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class FileServiceTest {

	FileService fileService;
	AppConfiguration appConfiguration;
	
	@MockBean
	FileAttachmentRepository fileAttachmentRepository;
	
	@BeforeEach
	public void init() {
		appConfiguration = new AppConfiguration();
		appConfiguration.setUploadPath("uploads-test");
		
		fileService = new FileService(appConfiguration, fileAttachmentRepository);
		
		new File(appConfiguration.getUploadPath()).mkdir();
		new File(appConfiguration.getFullProfileImagesPath()).mkdir();
		new File(appConfiguration.getFullAttachmentPath()).mkdir();
	}

	@AfterEach
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentPath()));
	}
	
	
	@Test @DisplayName("[파일 업로드] png파일일 경우, 정상작동")
	public void detectType_whenPngFileProvided_returnsImagePng() throws IOException {
		ClassPathResource resourceFile = new ClassPathResource("test-png.png");
		byte[] fileArr = FileUtils.readFileToByteArray(resourceFile.getFile());
		String fileType = fileService.detectType(fileArr);
		assertThat(fileType).isEqualToIgnoringCase("image/png");
	}
	@Test @DisplayName("[파일 삭제] 피드 없는 한시간 지난 첨부파일, 폴더에서 삭제")
	public void cleanupStorage_whenOldFilesExists_removesFilesFromStorage() throws IOException {
		String fileName = "random-name";
		String filePath = appConfiguration.getFullAttachmentPath() + "/" + fileName;
		File source = new ClassPathResource("profile.png").getFile();
		File target = new File(filePath);
		FileUtils.copyFile(source, target);
		
		FileAttachment fileAttachment = new FileAttachment();
		fileAttachment.setId(5);
		fileAttachment.setName(fileName);
		
		Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
		.thenReturn(Arrays.asList(fileAttachment));
		
		fileService.cleanupStorage();
		File storedImage = new File(filePath);
		assertThat(storedImage.exists()).isFalse();
	}
	@Test @DisplayName("[파일 삭제] 피드 없는 한시간 지난 첨부파일, DB에서 삭제")
	public void cleanupStorage_whenOldFilesExists_removesFilesFromDB() throws IOException {
		String fileName = "random-name";
		String filePath = appConfiguration.getFullAttachmentPath() + "/" + fileName;
		File source = new ClassPathResource("test-png.png").getFile();
		File target = new File(filePath);
		FileUtils.copyFile(source, target);
		
		FileAttachment attachment = new FileAttachment();
		attachment.setId(5L);
		attachment.setName(fileName);
		
		Mockito.when(fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(Mockito.any(Date.class)))
		.thenReturn(List.of(attachment));
		
		fileService.cleanupStorage();
		Mockito.verify(fileAttachmentRepository).deleteById(5L);
	}
}
