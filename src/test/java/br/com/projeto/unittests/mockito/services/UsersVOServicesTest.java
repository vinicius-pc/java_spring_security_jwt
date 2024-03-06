package br.com.projeto.unittests.mockito.services;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.projeto.configs.TestConfigs;
import br.com.projeto.exceptions.RequiredObjectIsNullException;
import br.com.projeto.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.projeto.model.security.Permission;
import br.com.projeto.model.security.User;
import br.com.projeto.repositories.PermissionRepository;
import br.com.projeto.repositories.UserRepository;
import br.com.projeto.services.user.UsersVOServices;
import br.com.projeto.unittests.vo.mocks.MockPermissionVO;
import br.com.projeto.unittests.vo.mocks.MockUserEnabledVO;
import br.com.projeto.unittests.vo.mocks.MockUserPasswordVO;
import br.com.projeto.unittests.vo.mocks.MockUserVO;
import br.com.projeto.unittests.vo.wrappers.WrapperUsersVO;
import br.com.projeto.vo.security.AccountCredentialsVO;
import br.com.projeto.vo.security.TokenVO;
import br.com.projeto.vo.user.UserEnabledVO;
import br.com.projeto.vo.user.UserPasswordVO;
import br.com.projeto.vo.user.UserVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8888"})
@TestMethodOrder(OrderAnnotation.class)
class UsersVOServicesTest extends AbstractIntegrationTest {
	
	private static RequestSpecification specification;
	private static ObjectMapper objectMapper;	
	//private static UserVO user;
	
	MockUserVO input;
	MockPermissionVO mockPermission;
	MockUserPasswordVO mockUserPassword;
	MockUserEnabledVO mockUserEnabledVO;
	
	@InjectMocks
	private UsersVOServices service;

	@Mock
	UserRepository repository;

	@Mock
	PermissionRepository pRepository;
	
	//@Autowired
	//PagedResourcesAssembler<UserVO> UserVOAssembler;	

	@BeforeAll
	public static void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);		
		//user = new UserVO();
	}
	
	@BeforeEach
	void setUpMocks() throws Exception {
		mockUserPassword = new MockUserPasswordVO();
		mockPermission = new MockPermissionVO();
		mockUserEnabledVO = new MockUserEnabledVO();
		input = new MockUserVO();
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	@Order(0)
	public void authorization() throws JsonMappingException, JsonProcessingException {
		// primeiramente, devemos obter um JWT acces token valido!
		AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");
		
		var accessToken = given()
				.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.body(user)
					.when()
				.post()
					.then()
						.statusCode(200)
							.extract()
							.body()
								.as(TokenVO.class)
							.getAccessToken();
		
		specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
				.setBasePath("/api/user")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
	}
	

	@Test
	@Order(1)
	void testLoadUserVOByUsername() {		
		User user = input.mockEntity(1);
		user.setPermissions(mockPermission.mockPermissions(1));

		when(repository.findByUsername(user.getUserName())).thenReturn(user);
		
		var result = service.loadUserVOByUsername(user.getUserName());
		///System.out.println(result.toString());  // para ver o valor do link hateoas
		assertTrue(result.toString().contains("links: [</api/user/USER%20NAME%20TEST%201>;rel=\"self\"]"));
		assertEquals(user.getFullName(), result.getFullName());
		assertEquals(user.getUserName(), result.getUserName());
		assertEquals(null, result.getPassword());
		assertEquals(mockPermission.mockPermissionsVO(1), result.getPermissions());
	}

	@Test
	@Order(2)
	void testLoadUsersVO() throws JsonMappingException, JsonProcessingException  {

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.accept(TestConfigs.CONTENT_TYPE_JSON)
				.queryParams("page", 1, "size", 1, "direction", "asc")
					.when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		// retorna um json no formato pagina da lista de usuarios!
		// temos que converter o json para uma lista de objetos!
		WrapperUsersVO wrapper = objectMapper.readValue(content,WrapperUsersVO.class);
		var usersVO = wrapper.getEmbedded().getUsers();
		UserVO user = usersVO.get(0);
		assertNotNull(user.getFullName());
		assertNotNull(user.getUserName());
		assertNotNull(user.getLinks());
		
	}

	@Test
	@Order(3)
	void testCreateUser() {
		Permission permission = mockPermission.mockEntity(1);
		User userToInsert = input.mockEntity(1);
		userToInsert.setPermissions(mockPermission.mockPermissions(1));
		User user = userToInsert;
		userToInsert.setId(null);
		when(pRepository.findByDescription("DESCRIPTION TEST 1")).thenReturn(permission);
		when(repository.findByUsername(userToInsert.getUserName())).thenReturn(null);
		lenient().when(repository.save(userToInsert)).thenReturn(user);
		UserVO userVO = input.mockEntityVO(1);
		userVO.setPermissions(mockPermission.mockPermissionsVO(1));
		
		var result = service.createUser(userVO);
		///System.out.println(result.toString());  // para ver o valor do link hateoas
		assertTrue(result.toString().contains("links: [</api/user>;rel=\"self\"]"));
		assertEquals(userVO.getFullName(), result.getFullName());
		assertEquals(userVO.getUserName(), result.getUserName());
		assertEquals(userVO.getPassword(), result.getPassword());
		assertEquals(mockPermission.mockPermissionsVO(1), result.getPermissions());		
	}

	@Test
	@Order(4)
	void testUpdateUser() {
		Permission permission = mockPermission.mockEntity(1);
		User user = input.mockEntity(1);
		user.setPermissions(mockPermission.mockPermissions(1));
		when(pRepository.findByDescription("DESCRIPTION TEST 1")).thenReturn(permission);
		when(repository.findByUsername(user.getUserName())).thenReturn(user);
		lenient().when(repository.save(user)).thenReturn(user);
		UserVO userVO = input.mockEntityVO(1);
		userVO.setPermissions(mockPermission.mockPermissionsVO(1));
		
		var result = service.updateUser(userVO);
		assertTrue(result.toString().contains("links: [</api/user>;rel=\"self\"]"));
		assertEquals(userVO.getFullName(), result.getFullName());
		assertEquals(userVO.getUserName(), result.getUserName());
		assertEquals(userVO.getPassword(), result.getPassword());
		assertEquals(mockPermission.mockPermissionsVO(1), result.getPermissions());		
	}

	@Test
	@Order(5)
	void testUpdatePassword() {
		UserPasswordVO userPasswordVO = mockUserPassword.mockEntityVO(1);
		User user = input.mockEntity(1);
		user.setPermissions(mockPermission.mockPermissions(1));
		when(repository.findByUsername(userPasswordVO.getUserName())).thenReturn(user);
		lenient().when(repository.save(user)).thenReturn(user);
		service.updatePassword(userPasswordVO);
	}

	@Test
	@Order(6)
	void testDisableOrEnableUser() {
		UserEnabledVO userEnabledVO = mockUserEnabledVO.mockEntityVO(1);
		User user = input.mockEntity(1);
		user.setPermissions(mockPermission.mockPermissions(1));
		when(repository.findByUsername(userEnabledVO.getUserName())).thenReturn(user);
		lenient().when(repository.save(user)).thenReturn(user);
		service.disableOrEnableUser(userEnabledVO);
	}

	@Test
	@Order(7)
	void testCreateNullUser() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {
			service.createUser(null);
		});
		String expectedMessage = "It is not allowed to persist a null object!";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	@Order(8)
	void testUpdateNullUser() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {
			service.updateUser(null);
		});
		String expectedMessage = "It is not allowed to persist a null object!";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test
	@Order(9)
	void testCreateUserFound() {
		UserVO userVO = input.mockEntityVO(1);
		User user = input.mockEntity(1);
		when(repository.findByUsername(userVO.getUserName())).thenReturn(user);
		
		String name = userVO.getUserName();
		Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
			service.createUser(userVO);
		});
		String expectedMessage = "Username " + name + " already exists!";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	@Order(10)
	void testUpdateUserNotFound() {
		UserVO userVO = input.mockEntityVO(1);
		when(repository.findByUsername(userVO.getUserName())).thenReturn(null);
		
		String name = userVO.getUserName();
		Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
			service.updateUser(userVO);
		});
		String expectedMessage = "Username " + name + " not found!";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
}
