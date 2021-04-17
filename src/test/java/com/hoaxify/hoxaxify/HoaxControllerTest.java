package com.hoaxify.hoxaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.apache.commons.io.FileUtils;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import com.hoaxify.hoxaxify.common.ApiError;
import com.hoaxify.hoxaxify.common.GeneralResponse;
import com.hoaxify.hoxaxify.configuration.AppConfiguration;
import com.hoaxify.hoxaxify.model.FileAttachment;
import com.hoaxify.hoxaxify.model.Hoax;
import com.hoaxify.hoxaxify.model.User;
import com.hoaxify.hoxaxify.model.vm.HoaxVM;
import com.hoaxify.hoxaxify.repository.FileAttachmentRepository;
import com.hoaxify.hoxaxify.repository.HoaxRepository;
import com.hoaxify.hoxaxify.repository.UserRepository;
import com.hoaxify.hoxaxify.service.FileService;
import com.hoaxify.hoxaxify.service.HoaxService;
import com.hoaxify.hoxaxify.service.UserService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HoaxControllerTest {

	private static final String API_1_0_HOAXES = "/api/1.0/hoaxes";

	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	HoaxRepository hoaxRepository;
	
	@Autowired
	HoaxService hoaxService;
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@PersistenceUnit
	EntityManagerFactory entityManagerFactory;
	
	@Autowired
	FileService fileService;
	
	@Autowired
	FileAttachmentRepository fileAttachmentRepository;
	
	@Autowired
	AppConfiguration appConfiguration;
	
	
	@BeforeEach
	public void cleanup() throws IOException {
		fileAttachmentRepository.deleteAll();
		hoaxRepository.deleteAll();
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentPath()));
	}
	
	@Test @DisplayName("[피드 생성] 인증된 회원이 유효한 피드, 200 받음")
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<Object> response = postHoax(hoax, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[피드 생성] 인증되지 않은 회원이 유효한 피드, 301 받음")
	public void postHoax_whenUserIsUnauthorized_receiveUnauthorized() {
		userService.createUser(TestUtil.createValidUser());
		
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<Object> response = postHoax(hoax, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test @DisplayName("[피드 생성] 인증되지 않은 회원이 유효한 피드, ApiError 받음")
	public void postHoax_whenUserIsUnauthorized_receiveApiError() {
		userService.createUser(TestUtil.createValidUser());
		
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
		
		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}
	@Test @DisplayName("[피드 생성] 인증된 회원이 유효한 피드, DB에 저장")
	public void postHoax_whenUserIsUnauthorized_hoaxSavedToDB() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		postHoax(hoax, Object.class);
		
		assertThat(hoaxRepository.count()).isEqualTo(1);
	}
	@Test @DisplayName("[피드 생성] 인증된 회원이 유효한 피드, DB에 시간 저장")
	public void postHoax_whenUserIsUnauthorized_hoaxSavedToDBWithTimeStamp() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		postHoax(hoax, Object.class);
		
		Hoax inDB = hoaxRepository.findAll().get(0);
		assertThat(inDB.getTimestamp()).isNotNull();
	}
	@Test @DisplayName("[피드 생성] content Null, 400 받음")
	public void postHoax_whenHoaxContentNullAndUserIsAuthorized_receiveBadRequest() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setContent(null);
		
		ResponseEntity<Object> response = postHoax(hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("[피드 생성] content 10자 이하, 400 받음")
	public void postHoax_whenHoaxContentLessThan10_receiveBadRequest() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setContent("123456789");
		
		ResponseEntity<Object> response = postHoax(hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("[피드 생성] content 5000자, 200 받음")
	public void postHoax_whenHoaxContentIs5000Length_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		String veryLongStr = IntStream.rangeClosed(1, 5000).mapToObj(i -> "x").collect(Collectors.joining());
		hoax.setContent(veryLongStr);
		
		ResponseEntity<Object> response = postHoax(hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[피드 생성] content 5000자 초과, 400 받음")
	public void postHoax_whenHoaxContentMoreThan5000Length_receiveBadRequest() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		String veryLongStr = IntStream.rangeClosed(1, 5001).mapToObj(i -> "x").collect(Collectors.joining());
		hoax.setContent(veryLongStr);
		
		ResponseEntity<Object> response = postHoax(hoax, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	@Test @DisplayName("[피드 생성] content Null, ApiError 받음")
	public void postHoax_whenHoaxContentNullAndUserIsAuthorized_receiveApiError() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setContent(null);
		
		ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("content")).isNotNull();
	}
	@Test @DisplayName("[피드 생성] 인증된 회원이 유효한 피드, 회원정보 저장")
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_savedToDBWithUser() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		postHoax(hoax, Object.class);
		
		Hoax inDB = hoaxRepository.findAll().get(0);
		assertThat(inDB.getUser().getUsername()).isEqualTo("username");
	}
	@Test @DisplayName("[피드 생성] 인증된 회원이 유효한 피드, 회원에서 피드 접근 가능")
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_hoaxCanBeAccessedFromUserEntity() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		postHoax(hoax, Object.class);
		
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		User inDB = entityManager.find(User.class, user.getId());
		assertThat(inDB.getHoaxes().size()).isEqualTo(1);
	}
	@Test @DisplayName("[피드 생성] 인증된 회원이 유효한 피드, HoaxVM 받음")
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveHoaxVM() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		Hoax hoax = TestUtil.createValidHoax();
		ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
		
		assertThat(response.getBody().getUser().getUsername()).isEqualTo(user.getUsername());
	}
	@Test @DisplayName("[피드 조회] 피드 1도 없을 경우, 200 받음")
	public void getHoaxes_whenThereAreNoHoaxes_receiveOk() {
		ResponseEntity<Object> response = getHoaxes(new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[피드 조회] 피드 1도 없을 경우, 빈 페이지 받음")
	public void getHoaxes_whenThereAreNoHoaxes_receivePageWithZeroItems() {
		ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	@Test @DisplayName("[피드 조회] 피드가 3개일 경우, 피드 3개  받음")
	public void getHoaxes_whenThereAreHoaxes_receivePageWithItems() {
		User user = userService.createUser(TestUtil.createValidUser());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<Object>> response = getHoaxes(new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}
	@Test @DisplayName("[피드 조회] 피드 1개 있을 경우, HoaxVM 받음")
	public void getHoaxes_whenThereAreNoHoaxes_receiveHoaxVM() {
		User user = userService.createUser(TestUtil.createValidUser());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<HoaxVM>> response = getHoaxes(new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
		assertThat(response.getBody().getContent().get(0).getUser().getUsername()).isEqualTo(user.getUsername());
	}
	@Test @DisplayName("[회원 피드 조회] 회원 존재할 경우, 200 받음")
	public void getHoaxesOfUser_whenUserExists_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		ResponseEntity<Object> response = getHoaxesOfUser(user, new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[회원 피드 조회] 회원 존재하지 않을 경우, 404 받음")
	public void getHoaxesOfUser_whenUserDoesNotExists_receiveNotFound() {
		User user = userService.createUser(TestUtil.createValidUser());
		user.setUsername("anonymous");
		ResponseEntity<Object> response = getHoaxesOfUser(user, new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@Test @DisplayName("[회원 피드 조회] 회원 존재하지만 피드 0개일 경우, 빈 페이지 받음")
	public void getHoaxesOfUser_whenUserExistsButHoaxDoesNotExist_receivePageWithItemZeroItems() {
		User user = userService.createUser(TestUtil.createValidUser());
		ResponseEntity<TestPage<Object>> response = getHoaxesOfUser(user, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	@Test @DisplayName("[회원 피드 조회] 피드가 3개일 경우, 피드 3개  받음")
	public void getHoaxesOfUser_whenThereAreHoaxes_receivePageWithItems() {
		User user = userService.createUser(TestUtil.createValidUser());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<Object>> response = getHoaxesOfUser(user, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(3);
	}
	@Test @DisplayName("[회원 피드 조회] 피드 1개 있을 경우, HoaxVM 받음")
	public void getHoaxesOfUser_whenThereAreNoHoaxes_receiveHoaxVM() {
		User user = userService.createUser(TestUtil.createValidUser());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		ResponseEntity<TestPage<HoaxVM>> response = getHoaxesOfUser(user, new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
		assertThat(response.getBody().getContent().get(0).getUser().getUsername()).isEqualTo(user.getUsername());
	}
	@Test @DisplayName("[회원 피드 조회] 다룬 회원 피드 3개 본인 피드 1개 있을 경우, 본인 피드 1개 받음")
	public void getHoaxesOfUser_whenThereAreHoaxOf3OthersAndOf1Mine_receiveOnlyMine() {
		User user1 = userService.createUser(TestUtil.createValidUser("user1"));		
		IntStream.rangeClosed(1, 3).forEach(i -> {
			hoaxService.postHoax(user1, TestUtil.createValidHoax());			
		});
		User user2 = userService.createUser(TestUtil.createValidUser("user2"));
		hoaxService.postHoax(user2, TestUtil.createValidHoax());
		ResponseEntity<TestPage<Object>> response = getHoaxesOfUser(user2, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(1);
	}	
	@Test @DisplayName("[지난 피드 조회] 피드가 1도 없을 경우, 200 받음")
	public void getOldHoaxes_whenThereAreNoHoaxes_receiveOk() {
		ResponseEntity<TestPage<Object>> response = getOldHoaxes(5, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[지난 피드 조회] 지난 피드가 3개일 때, 피드 3개 받음")
	public void getOldHoaxes_whenThereAreHoaxes_receivePageWithItemsProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax third = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<Object>> response = getOldHoaxes(third.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}
	@Test @DisplayName("[지난 피드 조회] 지난 피드가 1개일 때, Page<HoaxVM> 1개 받음")
	public void getOldHoaxes_whenThereAreHoaxes_receivePageWithHoaxVMProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax second = hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<HoaxVM>> response = getOldHoaxes(second.getId(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
		assertThat(response.getBody().getContent().get(0).getClass()).isEqualTo(HoaxVM.class);
	}
	@Test @DisplayName("[회원 지난 피드 조회] 피드가 1도 없을 경우, 200 받음")
	public void getOldHoaxesOfUser_whenThereAreNoHoaxes_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(user, 0, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[회원 지난 피드 조회] 지난 피드가 3개일 때, 피드 3개 받음")
	public void getOldHoaxesOfUser_whenThereAreHoaxes_receivePageWithItemsProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax third = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(user, third.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}
	@Test @DisplayName("[회원 지난 피드 조회] 지난 피드가 1개일 때, Page<HoaxVM> 1개 받음")
	public void getOldHoaxesOfUser_whenThereAreHoaxes_receivePageWithHoaxVMProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax second = hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<TestPage<HoaxVM>> response = getOldHoaxesOfUser(user, second.getId(), new ParameterizedTypeReference<TestPage<HoaxVM>>() {});
		assertThat(response.getBody().getContent().get(0).getClass()).isEqualTo(HoaxVM.class);
	}
	@Test @DisplayName("[회원 지난 피드 조회] 회원이 존재하지 않는 경우, 404받음")
	public void getOldHoaxesOfUser_whenThereAreNoHoaxes_receiveNotFound() {
		User user = TestUtil.createValidUser();
		ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(user, 0, new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@Test @DisplayName("[회원 지난 피드 조회] 다른 회원 지난 피드 2개일 때, 빈 페이지 받음")
	public void getOldHoaxesOfUser_whenThereAreHoaxes_receivePageWithZeroItemsProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax third = hoaxService.postHoax(user, TestUtil.createValidHoax());
		User anotherUser = userService.createUser(TestUtil.createValidUser("another"));
		
		ResponseEntity<TestPage<Object>> response = getOldHoaxesOfUser(anotherUser, third.getId(), new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
	}
	@Test @DisplayName("[최신 피드 조회] 피드가 1도 없을 경우, 200 받음")
	public void getNewHoaxes_whenThereAreNoHoaxes_receiveOk() {
		ResponseEntity<List<Object>> response = getNewHoaxes(5, new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[최신 피드 조회] 최근 피드 1개일 때, 피드 1개 받음")
	public void getNewHoaxes_whenThereAreHoaxes_receiveListOfItemsAfterProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax third = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<Object>> response = getNewHoaxes(third.getId(), new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getBody().size()).isEqualTo(1);
	}
	@Test @DisplayName("[최신 피드 조회] 최근 피드 1개일 때, List<HoaxVM> 받음")
	public void getNewHoaxes_whenThereAreHoaxes_receiveListOfHoaxVMAfterProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax third = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<HoaxVM>> response = getNewHoaxes(third.getId(), new ParameterizedTypeReference<List<HoaxVM>>() {});
		assertThat(response.getBody().get(0).getClass()).isEqualTo(HoaxVM.class);
	}
	@Test @DisplayName("[회원 최신 피드 조회] 피드가 1도 없을 경우, 200 받음")
	public void getNewHoaxesOfUser_whenThereAreNoHoaxes_receiveOk() {
		User user = userService.createUser(TestUtil.createValidUser());
		ResponseEntity<List<Object>> response = getNewHoaxesOfUser(user, 0, new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test @DisplayName("[회원 최신 피드 조회] 최신 피드가 3개일 때, 피드 3개 받음")
	public void getNewHoaxesOfUser_whenThereAreHoaxes_receivePageWithItemsProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		Hoax second = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<Object>> response = getNewHoaxesOfUser(user, second.getId(), new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getBody().size()).isEqualTo(3);
	}
	@Test @DisplayName("[회원 최신 피드 조회] 최신 피드가 1개일 때, Page<HoaxVM> 1개 받음")
	public void getNewHoaxesOfUser_whenThereAreHoaxes_receivePageWithHoaxVMProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		Hoax first = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<HoaxVM>> response = getNewHoaxesOfUser(user, first.getId(), new ParameterizedTypeReference<List<HoaxVM>>() {});
		assertThat(response.getBody().get(0).getClass()).isEqualTo(HoaxVM.class);
	}
	@Test @DisplayName("[회원 최신 피드 조회] 회원이 존재하지 않는 경우, 404 받음")
	public void getNewHoaxesOfUser_whenThereAreNoHoaxes_receiveNotFound() {
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = getNewHoaxesOfUser(user, 0, new ParameterizedTypeReference<Object>() {});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@Test @DisplayName("[회원 최신 피드 조회] 다른 회원 최신 피드 2개일 때, 빈 리스트 받음")
	public void getNewHoaxesOfUser_whenThereAreHoaxes_receivePageWithZeroItemsProvidedId() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		User anotherUser = userService.createUser(TestUtil.createValidUser("another"));
		Hoax first = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<List<Object>> response = getNewHoaxesOfUser(anotherUser, first.getId(), new ParameterizedTypeReference<List<Object>>() {});
		assertThat(response.getBody().size()).isEqualTo(0);
	}
	@Test @DisplayName("[최신 피드 조회] 최근 피드 1개일 때, count 1 받음")
	public void getNewHoaxes_whenThereAreHoaxes_receiveCountOfNewHoaxes() {
		User user = userService.createUser(TestUtil.createValidUser());
		
		Hoax first = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<Map<String, Long>> response = getNewHoaxCount(first.getId(), new ParameterizedTypeReference<Map<String, Long>>() {});
		assertThat(response.getBody().get("count")).isEqualTo(1L);
	}
	@Test @DisplayName("[회원 최신 피드 조회] 최근 피드 1개일 때, count 1 받음")
	public void getNewHoaxesOfUser_whenThereAreHoaxes_receiveCountOfNewHoaxesForUser() {
		User user = userService.createUser(TestUtil.createValidUser());
		User anotherUser = userService.createUser(TestUtil.createValidUser("anotherUser"));
		
		Hoax first = hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(user, TestUtil.createValidHoax());
		hoaxService.postHoax(anotherUser, TestUtil.createValidHoax());
		
		ResponseEntity<Map<String, Long>> response = getNewHoaxCountOfUser(user, first.getId(), new ParameterizedTypeReference<Map<String, Long>>() {});
		assertThat(response.getBody().get("count")).isEqualTo(1L);
	}
	@Test @DisplayName("[첨부파일 업로드] 인증된 회원	, 파일에서 피드 접근 가능")
	public void postHoaxAttachment_whenUserIsAuthorized_fileAttachmentHoaxRelationIsUpdatedInDB() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		MultipartFile file = createFile();
		FileAttachment attachment = fileService.saveAttachment(file);
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(attachment);
		
		ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
		FileAttachment inDB = fileAttachmentRepository.findAll().get(0);
		assertThat(inDB.getHoax().getId()).isEqualTo(response.getBody().getId());
	}
	@Test @DisplayName("[첨부파일 업로드] 인증된 회원	, 피드에서 파일 접근 가능")
	public void postHoaxAttachment_whenUserIsAuthorized_hoaxFileAttachmentRelationIsUpdatedInDB() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		MultipartFile file = createFile();
		FileAttachment attachment = fileService.saveAttachment(file);
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(attachment);
		
		ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
		Hoax inDB = hoaxRepository.findById(response.getBody().getId()).get();
		assertThat(inDB.getAttachment().getId()).isEqualTo(attachment.getId());
	}
	@Test @DisplayName("[첨부파일 업로드] 인증된 회원	, 파일과 함께 HoaxVM 받음")
	public void postHoaxAttachment_whenUserIsAuthorized_receivceHoaxVMWithAttachment() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		MultipartFile file = createFile();
		FileAttachment attachment = fileService.saveAttachment(file);
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(attachment);
		
		ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
		assertThat(response.getBody().getAttachment().getName()).isEqualTo(attachment.getName());
	}	
	@Test @DisplayName("[피드 삭제] 인증되지 않은 회원, 401 받음")
	public void deleteHoax_whenUserIsUnauthorized_receivceUnathorized() {
		ResponseEntity<Object> response = deleteHoax(5, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test @DisplayName("[피드 삭제] 인증된 회원, GenericResponse 받음")
	public void deleteHoax_whenUserIsAuthorized_receivceGeneralResponse() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		Hoax hoax = hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		ResponseEntity<GeneralResponse> response = deleteHoax(hoax.getId(), GeneralResponse.class);
		
		assertThat(response.getBody().getMessage()).isNotNull();
	}
	@Test @DisplayName("[피드 삭제] 인증된 회원, DB에서 삭제")
	public void deleteHoax_whenUserIsAuthorized_deletedHoaxInDB() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		Hoax hoax = hoaxService.postHoax(user, TestUtil.createValidHoax());
		
		deleteHoax(hoax.getId(), GeneralResponse.class);
		
		assertThat(hoaxRepository.count()).isEqualTo(0);
	}
	@Test @DisplayName("[피드 삭제] 다른 회원의 피드, 403 받음")
	public void deleteHoax_whenHoaxOwendAnotherUser_receiveForbidden() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		User another = userService.createUser(TestUtil.createValidUser("another"));
		Hoax hoax = hoaxService.postHoax(another, TestUtil.createValidHoax());
		
		ResponseEntity<Object> response = deleteHoax(hoax.getId(), Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	@Test @DisplayName("[피드 삭제] 존재하지 않는 피드, 404 받음")
	public void deleteHoax_whenHoaxDoesNotExists_receiveNotFound() {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		
		ResponseEntity<Object> response = deleteHoax(4444, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	@Test @DisplayName("[피드 삭제] 첨부파일 있을 때, DB에 첨부파일 삭제")
	public void deleteHoax_whenHoaxHasAttachment_deletedAttachmentInDB() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		MultipartFile file = createFile();
		FileAttachment attachment = fileService.saveAttachment(file);
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(attachment);
		
		ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
		deleteHoax(response.getBody().getId(), Object.class);
				
		Optional<FileAttachment> optionalAttachment = fileAttachmentRepository.findById(attachment.getId());
		assertThat(optionalAttachment.isPresent()).isFalse();
	}
	@Test @DisplayName("[피드 삭제] 첨부파일 있을 때, 첨부파일 폴더에서 삭제")
	public void deleteHoax_whenHoaxHasAttachment_deletedAttachmentInFolder() throws IOException {
		User user = userService.createUser(TestUtil.createValidUser());
		authenticate(user.getUsername());
		MultipartFile file = createFile();
		FileAttachment attachment = fileService.saveAttachment(file);
		Hoax hoax = TestUtil.createValidHoax();
		hoax.setAttachment(attachment);
		
		ResponseEntity<HoaxVM> response = postHoax(hoax, HoaxVM.class);
		deleteHoax(response.getBody().getId(), Object.class);

		File storedImage = new File(appConfiguration.getFullAttachmentPath() + "/" + attachment.getName());
		assertThat(storedImage.exists()).isFalse();
	}
	
	
	
	private <T> ResponseEntity<T> deleteHoax(long hoaxId, Class<T> responseType) {
		String path = API_1_0_HOAXES + "/"+ hoaxId;
		return testRestTemplate.exchange(path, HttpMethod.DELETE, null, responseType);
	}
	private MultipartFile createFile() throws IOException {
		ClassPathResource resource = new ClassPathResource("profile.png");
		byte[] fileAsByte = FileUtils.readFileToByteArray(resource.getFile());
		MultipartFile file = new MockMultipartFile("profile.png", fileAsByte);
		return file;
	}
	private <T> ResponseEntity<T> getNewHoaxCountOfUser(User user, long hoaxId, ParameterizedTypeReference<T> responseType) {
		String path = "/api/1.0/users/" + user.getUsername() + "/hoaxes/" + hoaxId + "?direction=after&count=true";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getNewHoaxCount(long hoaxId, ParameterizedTypeReference<T> responseType) {
		String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&count=true";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getNewHoaxesOfUser(User user, long hoaxId, ParameterizedTypeReference<T> responseType) {
		String path = "/api/1.0/users/" + user.getUsername() + "/hoaxes/" + hoaxId + "?direction=after&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getNewHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
		String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=after&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getOldHoaxesOfUser(User user, long hoaxId, ParameterizedTypeReference<T> responseType) {
		String path = "/api/1.0/users/" + user.getUsername() + "/hoaxes/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getOldHoaxes(long hoaxId, ParameterizedTypeReference<T> responseType) {
		String path = API_1_0_HOAXES + "/" + hoaxId + "?direction=before&page=0&size=5&sort=id,desc";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getHoaxesOfUser(User user, ParameterizedTypeReference<T> responseType) {
		String path = "/api/1.0/users/" + user.getUsername() + "/hoaxes";
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> getHoaxes(ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(API_1_0_HOAXES, HttpMethod.GET, null, responseType);
	}
	private <T> ResponseEntity<T> postHoax(Hoax hoax, Class<T> responseType) {
		return testRestTemplate.postForEntity(API_1_0_HOAXES, hoax, responseType);
	}
	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}
}
