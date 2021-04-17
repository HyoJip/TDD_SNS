package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hoaxify.hoxaxify.common.ApiError;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.repository.UserRepository;
import com.hoaxify.hoxaxify.service.UserService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

	private static final String API_1_0_LOGIN = "/api/1.0/login";
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@BeforeEach
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}
	
	@Test @DisplayName("[postLogin] 인증없을 경우, 401 UNAUTHORIZED")
	public void postLogin_withoutUserCredentials_receiveUnauthorized() {
		ResponseEntity<Object> response = postLogin(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test @DisplayName("[postLogin] 인증 실패할 경우, 401 UNAUTHORIZED")
	public void postLogin_withInCorrectCredentials_receiveUnauthorized() {
		authenticate();
		ResponseEntity<Object> response = postLogin(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test @DisplayName("[postLogin] 인증없을 경우, ApiError")
	public void postLogin_withoutUserCredentials_receiveApiError() {
		ResponseEntity<ApiError> response = postLogin(ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_LOGIN);
	}
	@Test @DisplayName("[postLogin] 인증없을 경우, ValidationErrors 없는 ApiError")
	public void postLogin_withoutUserCredentials_receiveApiErrorWithoutValidationErrors() {
		ResponseEntity<String> response = postLogin(String.class);
		assertThat(response.getBody().contains("validationErrors")).isFalse();
	}
	@Test @DisplayName("[postLogin] 인증없을 경우, Header에 WWW-Authenticate 없음")
	public void postLogin_withoutUserCredentials_receiveUnauthorizedWithoudWWWAuthenticationHeader() {
		ResponseEntity<Object> response = postLogin(Object.class);
		assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
	}
	@Test @DisplayName("[postLogin] 올바른 인증, 200 OK")
	public void postLogin_withValidCredentials_receiveOk() {
		User user = TestUtil.createValidUser();
		userService.createUser(user);
		authenticate();
		ResponseEntity<Object> response = postLogin(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[postLogin] 올바른 인증, 회원 ID 받음")
	public void postLogin_withValidCredentials_receiveLoggedInUser() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = postLogin(new ParameterizedTypeReference<Map<String, Object>>() {});
		Map<String, Object> body = response.getBody();
		assertThat((Integer) body.get("id")).isEqualTo(user.getId());
	}
	@Test @DisplayName("[postLogin] 올바른 인증, 회원 이미지 받음")
	public void postLogin_withValidCredentials_receiveLoggedInUsersImage() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = postLogin(new ParameterizedTypeReference<Map<String, Object>>() {});
		Map<String, Object> body = response.getBody();
		assertThat((String) body.get("image")).isEqualTo(user.getImage());
	}
	@Test @DisplayName("[postLogin] 올바른 인증, 회원 displayName 받음")
	public void postLogin_withValidCredentials_receiveLoggedInUsersDisplayName() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = postLogin(new ParameterizedTypeReference<Map<String, Object>>() {});
		Map<String, Object> body = response.getBody();
		assertThat((String) body.get("displayName")).isEqualTo(user.getDisplayName());
	}
	@Test @DisplayName("[postLogin] 올바른 인증, 회원 username 받음")
	public void postLogin_withValidCredentials_receiveLoggedInUsersUsername() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = postLogin(new ParameterizedTypeReference<Map<String, Object>>() {});
		Map<String, Object> body = response.getBody();
		assertThat((String) body.get("username")).isEqualTo(user.getUsername());
	}
	@Test @DisplayName("[postLogin] 올바른 인증, 회원 password 받지않음")
	public void postLogin_withValidCredentials_notReceiveLoggedInUsersPassword() {
		userService.createUser(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = postLogin(new ParameterizedTypeReference<Map<String, Object>>() {});
		Map<String, Object> body = response.getBody();
		assertThat((String) body.get("password")).isNull();
	}
	
	
	
	
	
	
	
	
	private void authenticate() {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor("username", "P4ssword"));
	}
	public <T> ResponseEntity<T> postLogin(Class<T> responseType) {
		return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
	}
	public <T> ResponseEntity<T> postLogin(ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(API_1_0_LOGIN, HttpMethod.POST, null, responseType);
	}
}
