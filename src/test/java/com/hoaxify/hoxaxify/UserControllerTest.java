package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.hoaxify.hoxaxify.common.ApiError;
import com.hoaxify.hoxaxify.common.GeneralResponse;
import com.hoaxify.hoxaxify.configuration.AppConfiguration;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.model.vm.UserUpdateVM;
import com.hoaxify.hoxaxify.model.vm.UserVM;
import com.hoaxify.hoxaxify.repository.UserRepository;
import com.hoaxify.hoxaxify.service.UserService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

	private static final String API_1_0_USER = "/api/1.0/users";

	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AppConfiguration appConfiguration;
	
	@BeforeEach
	public void setUp() throws Exception {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}
	
	@AfterEach
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagesPath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentPath()));
	}

	@Test @DisplayName("????????? ?????? , POST ????????????, 200 OK")
	public void post_whenUserIsValid_receiveOk() {
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("????????? ??????, POST ????????????, DB??????")
	public void post_whenUserIsValid_userSavedToDB() {
		User user = TestUtil.createValidUser();
		postUser(user, Object.class);
		assertThat(userRepository.count()).isEqualTo(1);
	}
	@Test @DisplayName("????????? ??????, POST ????????????, ?????? ?????????")
	public void post_whenUserIsValid_receiveGeneralResponse() {
		User user = TestUtil.createValidUser();
		ResponseEntity<GeneralResponse> response = postUser(user, GeneralResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}
	@Test @DisplayName("????????? ??????, POST ????????????, ???????????? ????????? ??? ??????")
	public void post_whenUserIsValid_passwordIsHashedInDB() {
		User user = TestUtil.createValidUser();
		postUser(user, Object.class);
		User inDB = userRepository.findAll().get(0);
		assertThat(inDB.getPassword()).isNotEqualTo(user.getPassword());
	}
	@Test @DisplayName("username ??????, POST ???????????? , 400 badRequest")
	public void post_whenUsernameIsNull_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("password ??????, POST ???????????? , 400 badRequest")
	public void post_whenPasswordIsNull_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("displayName ??????, POST ???????????? , 400 badRequest")
	public void post_whenDisplayNameIsNull_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName(null);
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("username 4?????? ??????, POST ???????????? , 400 badRequest")
	public void post_whenUsernameIsLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername("abc");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("displayName 4?????? ??????, POST ???????????? , 400 badRequest")
	public void post_whenDisplayNameIsLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName("abc");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("password 8?????? ??????, POST ???????????? , 400 badRequest")
	public void post_whenPasswordIsLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("P4sswor");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("username 255?????? ??????, POST ???????????? , 400 badRequest")
	public void post_whenUsernameIsMoreThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String veryLongStr = IntStream.rangeClosed(1, 256).mapToObj(i -> "a").collect(Collectors.joining());
		user.setUsername(veryLongStr);
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("displayName 255?????? ??????, POST ???????????? , 400 badRequest")
	public void post_whenDisplayNameIsMoreThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String veryLongStr = IntStream.rangeClosed(1, 256).mapToObj(i -> "a").collect(Collectors.joining());
		user.setDisplayName(veryLongStr);
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("password 255?????? ??????, POST ???????????? , 400 badRequest")
	public void post_whenPasswordIsMoreThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String veryLongStr = IntStream.rangeClosed(1, 256).mapToObj(i -> "a").collect(Collectors.joining());
		user.setPassword(veryLongStr + "1A");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("password ????????????, POST ???????????? , 400 badRequest")
	public void post_whenPasswordIsOnlyLowercase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("lowercase");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("password ????????????, POST ???????????? , 400 badRequest")
	public void post_whenPasswordIsOnlyUppercase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("UPPERCASE");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("password ?????????, POST ???????????? , 400 badRequest")
	public void post_whenPasswordIsOnlyDemical_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("12345678");
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("???????????? ?????? ??????, POST ???????????? , ApiError")
	public void post_whenUserIsInvalid_receiveApiError() {
		User user = new User();
		ResponseEntity<ApiError> response = postUser(user, ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_USER);
	}
	@Test @DisplayName("???????????? ?????? ??????, POST ???????????? , ValidationErrors")
	public void post_whenUserIsInvalid_receive3ValidtaionErrors() {
		User user = new User();
		ResponseEntity<ApiError> response = postUser(user, ApiError.class);
		assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
	}
	@Test @DisplayName("username??? ??????, POST ???????????? , username ???????????????")
	public void post_whenUserIsInvalid_receiveMessageOfNullErrorForUsername() {
		User user = new User();
		ResponseEntity<ApiError> response = postUser(user, ApiError.class);
		String message = response.getBody().getValidationErrors().get("username");
		assertThat(message).isEqualTo("username??? ?????? ???????????????");
	}
	@Test @DisplayName("displayName??? ??????, POST ???????????? , username ???????????????")
	public void post_whenUserIsInvalid_receiveMessageOfNullErrorForDisplayName() {
		User user = new User();
		ResponseEntity<ApiError> response = postUser(user, ApiError.class);
		String message = response.getBody().getValidationErrors().get("displayName");
		assertThat(message).isEqualTo("??? ????????? ?????? ???????????????");
	}
	@Test @DisplayName("password 8?????? ??????, POST ???????????? , size ???????????????")
	public void post_whenPasswordIsLessThanRequired_receiveMessageOfSize() {
		User user = TestUtil.createValidUser();
		user.setPassword("P4ss");
		ResponseEntity<ApiError> response = postUser(user, ApiError.class);
		String message = response.getBody().getValidationErrors().get("password");
		assertThat(message).isEqualTo("?????? 8, ?????? 255??? ????????? ???????????????");
	}
	@Test @DisplayName("password ????????????, POST ???????????? , pattern ???????????????")
	public void post_whenPasswordIsAllLowercase_receiveMessageOfPattern() {
		User user = TestUtil.createValidUser();
		user.setPassword("lowercase");
		ResponseEntity<ApiError> response = postUser(user, ApiError.class);
		String message = response.getBody().getValidationErrors().get("password");
		assertThat(message).isEqualTo("?????????, ?????????, ????????? ??????????????? ?????????");
	}
	@Test @DisplayName("????????? username, POST ???????????? , 400 badRequest")
	public void post_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(TestUtil.createValidUser());
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postUser(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("DB??? user ?????? ???, get ???????????? , 200 Ok")
	public void getUsers_whenThereAreNoUserInDB_receiveOk() {
		ResponseEntity<Object> response = getUsers(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("DB??? user ?????? ???, get ???????????? , ????????????")
	public void getUsers_whenThereAreNoUserInDB_receivePageWithZeroItems() {
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	@Test @DisplayName("DB??? 1 user, get ???????????? , ?????????<??????>")
	public void getUsers_whenThereIsAUserInDB_receivePageWithUser() {
		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(1);
	}
	@Test @DisplayName("DB??? 1 user, get ???????????? , password ?????? X")
	public void getUsers_whenThereIsAUserInDB_receiveUserWithoutPassword() {
		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		Map<String, Object> entity = response.getBody().getContent().get(0);
		assertThat(entity.containsKey("password")).isFalse();
	}
	@Test @DisplayName("DB??? ?????? 20??? ?????? ????????? size 3, page 0??????, get ???????????? , 3 User")
	public void getUsers_whenThereAre20UsersInDBAndPageSizeIs3_receive3Users() {
		IntStream.rangeClosed(1, 20)
			.mapToObj(i -> TestUtil.createValidUser("user" + i))
			.forEach(userRepository::save);
		String path = API_1_0_USER + "?size=3&page=0";
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		assertThat(response.getBody().getContent().size()).isEqualTo(3);
	}
	@Test @DisplayName("????????? ???????????? ?????? ???, get ???????????? , 10 size")
	public void getUsers_whenNotProvidedSizeProperty_receivePagesSize10() {
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		assertThat(response.getBody().getSize()).isEqualTo(10);
	}
	@Test @DisplayName("????????? ???????????? 100 ?????? ???, get ???????????? , 100 size")
	public void getUsers_whenSizeIsGreaterThan100_receivePagesSize100() {
		String path = API_1_0_USER + "?size=500";
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		assertThat(response.getBody().getSize()).isEqualTo(100);
	}
	@Test @DisplayName("???????????? ????????? ???, get ???????????? , 0 page")
	public void getUsers_whenPageIsNegative_receivePage0() {
		String path = API_1_0_USER + "?page=-1";
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		assertThat(response.getBody().getNumber()).isEqualTo(0);
	}
	@Test @DisplayName("[GET]???????????????, ?????? ????????? ?????? ?????? ??????")
	public void getUsers_whenUserLoggedIn_receivePageWithoutLoggedInUser() {
		userService.createUser(TestUtil.createValidUser("user1"));
		userService.createUser(TestUtil.createValidUser("user2"));
		userService.createUser(TestUtil.createValidUser("user3"));
		authenticate("user1");
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}
	@Test @DisplayName("???????????? ??????, ???????????? ????????? ???, 200 ??????")
	public void getUser_whenUserIsExist_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		ResponseEntity<Object> response = getUser(user.getUsername(), Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("???????????? ??????, ???????????? ????????? ???, passowrd ?????? X")
	public void getUser_whenUserIsExist_receiveUserWithoutPassword() {
		User user = userService.createUser(TestUtil.createValidUser());
		ResponseEntity<String> response = getUser(user.getUsername(), String.class);
		assertThat(response.getBody().contains("password")).isFalse();
	}
	@Test @DisplayName("???????????? ??????, ?????????????????? ????????? ???, 404  ??????")
	public void getUser_whenUserIsNotExist_receiveNotFound() {
		ResponseEntity<Object> response = getUser("anonymous", Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@Test @DisplayName("???????????? ??????, ?????????????????? ????????? ???, ApiError ??????")
	public void getUser_whenUserIsNotExist_receiveApiError() {
		ResponseEntity<ApiError> response = getUser("anonymous", ApiError.class);
		assertThat(response.getBody().getMessage().contains("anonymous")).isTrue();
	}
	@Test @DisplayName("?????? ??????, ???????????????, 401 ??????")
	public void putUser_whenUnauthorizedUserSendsRequest_receiveUnauthorized() {
		ResponseEntity<Object> response = putUser(333, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test @DisplayName("?????? ??????, ?????? ??????, 403 ??????")
	public void putUser_whenAnotherUserSendsRequest_receiveForbidden() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		ResponseEntity<Object> response = putUser(333, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	@Test @DisplayName("?????? ??????, ?????? ??????, ApiError ??????")
	public void putUser_whenAnotherUserSendsRequest_receiveApiError() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		ResponseEntity<ApiError> response = putUser(333, ApiError.class);
		assertThat(response.getBody().getUrl()).contains("/333");
	}
	@Test @DisplayName("?????? ??????, ?????? ?????? ??????, 200 ??????")
	public void putUser_whenAuthorizedUserSendsRequest_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), request, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("?????? ??????, ?????? ?????? ??????, displayName ?????????")
	public void putUser_whenAuthorizedUserSendsRequest_UsersDisplayNameUpdated() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		putUser(user.getId(), request, Object.class);
		User inDB = userRepository.findByUsername(user.getUsername());
		assertThat(inDB.getDisplayName()).isEqualTo(updatedUser.getDisplayName());
	}
	@Test @DisplayName("?????? ??????, ?????? ?????? ??????, displayName ????????? UserVM ??????")
	public void putUser_whenAuthorizedUserSendsRequest_receiveUserWithUpdatedDisplayName() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), request, UserVM.class);
		assertThat(response.getBody().getDisplayName()).isEqualTo(updatedUser.getDisplayName());
	}
	@Test @DisplayName("?????? ????????? ??????, ?????? ?????? ??????, ????????? ?????? ??????")
	public void putUser_whenAuthorizedUserSendsRequest_receiveUserWithUpdatedImage() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ClassPathResource imageResource = new ClassPathResource("profile.png");
		
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
		String imageString = Base64.getEncoder().encodeToString(imageArr);
		updatedUser.setImage(imageString);
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), request, UserVM.class);
		
		assertThat(response.getBody().getImage()).isNotEqualTo(updatedUser.getImage());
	}
	@Test @DisplayName("?????? ????????? ??????, ?????? ?????? ??????, ?????????????????? ?????????")
	public void putUser_whenAuthorizedUserSendsRequestWithSupprotedImage_imageIsStoredUnderProfileFolder() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("profile.png");
		updatedUser.setImage(imageString);
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), request, UserVM.class);
		String storedImageName = response.getBody().getImage();
		String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
		File storedImage = new File(profilePicturePath);
		
		assertThat(storedImage.exists()).isTrue();
	}
	@Test @DisplayName("?????? ????????? ??????, displayName null, 400 ??????")
	public void putUser_whenDisplayNameIsNull_receiveBadRequest() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		updatedUser.setDisplayName(null);
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), request, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("?????? ????????? ??????, JPG????????? ??????, 200 ??????")
	public void putUser_whenImageFileTypeIsJpg_receiveOk() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-jpg.jpg");
		updatedUser.setImage(imageString);
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), request, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("?????? ????????? ??????, GIF????????? ??????, 400 ??????")
	public void putUser_whenImageFileTypeIsGif_receiveBadRequest() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-gif.gif");
		updatedUser.setImage(imageString);
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), request, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("?????? ????????? ??????, TXT????????? ??????, ??????????????? ??????")
	public void putUser_whenImageFileTypeIsTxt_receiveValidatorErrorForProfileImage() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-txt.txt");
		updatedUser.setImage(imageString);
		
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		ResponseEntity<ApiError> response = putUser(user.getId(), request, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("image")).isEqualTo("PNG?????? ?????? JPG????????? ???????????????");
	}
	@Test @DisplayName("?????? ????????? ??????, ?????? ??????, ?????? ????????? ??????")
	public void putUser_whenUpdateIsOK_removeOldImageFromStorage() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-jpg.jpg");
		updatedUser.setImage(imageString);
		HttpEntity<UserUpdateVM> request = new HttpEntity<>(updatedUser);
		
		ResponseEntity<UserVM> response = putUser(user.getId(), request, UserVM.class);
		putUser(user.getId(), request, Object.class);
		
		String storedImageName = response.getBody().getImage();
		String profilePicturePath = appConfiguration.getFullProfileImagesPath() + "/" + storedImageName;
		assertThat(new File(profilePicturePath).exists()).isFalse();
	}

	
	
	
	
	private <T> ResponseEntity<T> postUser(User user, Class<T> responseType) {
		return testRestTemplate.postForEntity(API_1_0_USER, user, responseType);
	}
	private <T> ResponseEntity<T> getUsers(Class<T> responseType) {
		return testRestTemplate.getForEntity(API_1_0_USER, responseType);
	}
	private <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(API_1_0_USER, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getUser(String username, Class<T> responseType) {
		String path = API_1_0_USER + "/" + username;
		return testRestTemplate.getForEntity(path, responseType);
	}
	private <T> ResponseEntity<T> putUser(long id, Class<T> responseType) {
		String path = API_1_0_USER + "/" + id;
		return testRestTemplate.exchange(path, HttpMethod.PUT, null, responseType);
	}
	private <T> ResponseEntity<T> putUser(long id, HttpEntity<?> request, Class<T> responseType) {
		String path = API_1_0_USER + "/" + id;
		return testRestTemplate.exchange(path, HttpMethod.PUT, request, responseType);
	}
	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}
	private UserUpdateVM createValidUserUpdateVM() {
		UserUpdateVM updatedUser = new UserUpdateVM();
		updatedUser.setDisplayName("newDisplayName");
		return updatedUser;
	}
	private String readFileToBase64(String fileName) throws IOException {
		ClassPathResource imageResource = new ClassPathResource(fileName);
		byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
		String imageString = Base64.getEncoder().encodeToString(imageArr);
		return imageString;
	}
}
