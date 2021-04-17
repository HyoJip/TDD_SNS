package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.hoaxify.hoxaxify.configuration.AppConfiguration;
import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.FileAttachmentRepository;
import com.hoaxify.hoxaxify.repository.UserRepository;
import com.hoaxify.hoxaxify.service.UserService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FileUploadControllerTest {

	private static final String API_1_0_HOAXES_UPLOAD = "/api/1.0/hoaxes/upload";

	@Autowired
	AppConfiguration appConfiguration;
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	FileAttachmentRepository fileAttachmentRepository;
	
	@BeforeEach
	public void init() throws IOException {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentPath()));
	}
	
	
	
	@Test @DisplayName("[파일 업로드] 인증된 회원의 이미지, 200 받음")
	public void uploadFile_whenImageFromAuthenticatedUser_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ResponseEntity<Object> response = upload(getHttpEntity(), Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[파일 업로드] 인증되지 않은 회원의 이미지, 401 받음")
	public void uploadFile_whenImageFromUnauthenticatedUser_receiveUnauthorized() {
		ResponseEntity<Object> response = upload(getHttpEntity(), Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test @DisplayName("[파일 업로드] 인증된 회원의 이미지, 날짜와 함께 파일 받음")
	public void uploadFile_whenImageFromAuthenticatedUser_receiveFileAttachmentWithDate() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ResponseEntity<FileAttachment> response = upload(getHttpEntity(), FileAttachment.class);
		
		assertThat(response.getBody().getDate()).isNotNull();
	}
	@Test @DisplayName("[파일 업로드] 인증된 회원의 이미지, 이름 변경된 파일 받음")
	public void uploadFile_whenImageFromAuthenticatedUser_receiveFileAttachmentWithRandomName() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ResponseEntity<FileAttachment> response = upload(getHttpEntity(), FileAttachment.class);
		
		assertThat(response.getBody().getName()).isNotNull();
		assertThat(response.getBody().getName()).isNotEqualTo("profile.png");
	}
	@Test @DisplayName("[파일 업로드] 인증된 회원의 이미지, 미디어 폴더에 저장됨")
	public void uploadFile_whenImageFromAuthenticatedUser_fileSavedToFolder() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ResponseEntity<FileAttachment> response = upload(getHttpEntity(), FileAttachment.class);
		String imagePath = appConfiguration.getFullAttachmentPath() + "/" + response.getBody().getName();
		
		File storedImage = new File(imagePath);
		assertThat(storedImage.exists()).isTrue();
	}
	@Test @DisplayName("[파일 업로드] 인증된 회원의 이미지, DB에 저장됨")
	public void uploadFile_whenImageFromAuthenticatedUser_savedToDB() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		upload(getHttpEntity(), FileAttachment.class);
		assertThat(fileAttachmentRepository.count()).isEqualTo(1);
	}
	@Test @DisplayName("[파일 업로드] 인증된 회원의 이미지, 파일 타입 저장됨")
	public void uploadFile_whenImageFromAuthenticatedUser_fileAttachmentStoredWithFileType() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ResponseEntity<FileAttachment> response = upload(getHttpEntity(), FileAttachment.class);
		assertThat(response.getBody().getFileType()).isEqualTo("image/png");
	}

	
	
	private HttpEntity<MultiValueMap<String, Object>> getHttpEntity() {
		ClassPathResource imageResource = new ClassPathResource("profile.png");
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
		body.add("file", imageResource);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		return new HttpEntity<>(body, headers);
	}
	private <T> ResponseEntity<T> upload(HttpEntity<?> request, Class<T> responseType) {
		return testRestTemplate.exchange(API_1_0_HOAXES_UPLOAD, HttpMethod.POST, request, responseType);
	}
	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}
}
