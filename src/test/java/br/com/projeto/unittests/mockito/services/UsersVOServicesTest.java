package br.com.projeto.unittests.mockito.services;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.projeto.configs.TestConfigs;
import br.com.projeto.integrationtests.testcontainers.AbstractIntegrationTest;
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


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8888"})
@TestMethodOrder(OrderAnnotation.class)
class UsersVOServicesTest extends AbstractIntegrationTest {
	
	private static RequestSpecification userSpecification;
	private static ObjectMapper objectMapper;	
	private static String accessToken;
	
	MockUserVO input;
	MockPermissionVO mockPermission;
	MockUserPasswordVO mockUserPassword;
	MockUserEnabledVO mockUserEnabledVO;
	
	@BeforeAll
	public static void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);		
	}
	
	@BeforeEach
	void setUpMocks() throws Exception {
		mockUserPassword = new MockUserPasswordVO();
		mockPermission = new MockPermissionVO();
		mockUserEnabledVO = new MockUserEnabledVO();
		input = new MockUserVO();
		//MockitoAnnotations.openMocks(this);
	}
	
	@Test
	@Order(0)
	public void authorization() throws JsonMappingException, JsonProcessingException {
		// primeiramente, devemos obter um JWT acces token valido!
		AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");
		
		accessToken = given()
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
		
		userSpecification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
				.setBasePath("/api/user")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
	}
	

	@Test
	@Order(1)
	void testLoadUserVOByUsername() throws JsonMappingException, JsonProcessingException {		
		var content = given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.pathParam("userName", "leandro")
					.when()
					.get("{userName}")
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		UserVO result = objectMapper.readValue(content, UserVO.class);
		assertNotNull(result);
		assertNotNull(result.getFullName());
		assertNotNull(result.getUserName());
		assertEquals("Leandro Costa", result.getFullName());
		assertEquals("leandro", result.getUserName());
	}

	@Test
	@Order(2)
	void testLoadUsersVO() throws JsonMappingException, JsonProcessingException  {

		var content = given().spec(userSpecification)
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
	void testCreateUser() throws JsonMappingException, JsonProcessingException {
		UserVO userToInsert = input.mockEntityVO(1); // novo usuario!
		userToInsert.setPermissions(mockPermission.mockPermissionsVO(1));
		
		var content = given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(userToInsert)
					.when()
					.post()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		UserVO result = objectMapper.readValue(content, UserVO.class);
		assertNotNull(result);
		assertNotNull(result.getFullName());
		assertNotNull(result.getUserName());
		assertEquals(userToInsert.getFullName(), result.getFullName());
		assertEquals(userToInsert.getUserName(), result.getUserName());
		assertEquals(userToInsert.getPassword(), result.getPassword());
		assertEquals(mockPermission.mockPermissionsVO(1), result.getPermissions());		
	}

	@Test
	@Order(4)
	void testUpdateUser() throws JsonMappingException, JsonProcessingException {
		
		UserVO userToUpdate = input.mockEntityVO(1); // usuario incluido no order(3) acima!
		userToUpdate.setPermissions(mockPermission.mockPermissionsVO(2)); // alteramos a permissao
		userToUpdate.setFullName("CHANGE USER NAME"); // alteramos o nome!
		userToUpdate.setPassword(null);
		
		var content = given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(userToUpdate)
					.when()
					.put()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		UserVO result = objectMapper.readValue(content, UserVO.class);
		assertNotNull(result);
		assertNotNull(result.getFullName());
		assertNotNull(result.getUserName());
		assertEquals(userToUpdate.getFullName(), result.getFullName());
		assertEquals(userToUpdate.getUserName(), result.getUserName());
		assertEquals(mockPermission.mockPermissionsVO(2), result.getPermissions());		
		
	}

	@Test
	@Order(5)
	void testUpdatePassword()  throws JsonMappingException, JsonProcessingException {
		UserPasswordVO userPasswordVO = mockUserPassword.mockEntityVO(1);
		userPasswordVO.setUserName("leandro");
		userPasswordVO.setOldPassword("admin123");
		userPasswordVO.setNewPassword("admin123");
		
		RequestSpecification specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
				.setBasePath("/api/user/updatepassword")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();		
		
		given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(userPasswordVO)
					.when()
					.put()
				.then()
					.statusCode(204)
						.extract()
						.body()
							.asString();		
		
	}

	@Test
	@Order(6)
	void testDisableOrEnableUser() {
		UserEnabledVO userEnabledVO = mockUserEnabledVO.mockEntityVO(1);
		userEnabledVO.setUserName("flavio");
		userEnabledVO.setEnabled(false);
		
		RequestSpecification specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
				.setBasePath("/api/user/enabledordisable")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();		
		
		given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(userEnabledVO)
					.when()
					.put()
				.then()
					.statusCode(204)
						.extract()
						.body()
							.asString();		

	}

	@Test
	@Order(7)
	void testCreateNullUser() {
		given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					//.body()  --  sem usuario!
					.when()
					.post()
				.then()
					.statusCode(400)  // tem que retornar erro 400!
						.extract()
						.body()
							.asString();
	}

	@Test
	@Order(8)
	void testUpdateNullUser() {
		given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					// .body(userToUpdate) -- igual ao order 7 (inclusao) mas esse é PUT (Alteracao)! 
					.when()
					.put()
				.then()
					.statusCode(400) // erro esperado!
						.extract()
						.body()
							.asString();
	}
	
	@Test
	@Order(9)
	void testCreateUserFound() {
		// teoricamente, esse usuario já foi incluido no order 3... tem que apresentar erro!
		UserVO userToInsert = input.mockEntityVO(1); // novo usuario!
		userToInsert.setPermissions(mockPermission.mockPermissionsVO(1));
		
		given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(userToInsert)
					.when()
					.post()
				.then()
					.statusCode(500) // erro esperado!
						.extract()
						.body()
							.asString();

	}

	@Test
	@Order(10)
	void testUpdateUserNotFound() {
		UserVO userToUpdate = input.mockEntityVO(2); // usuario não existente!
		
		given().spec(userSpecification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(userToUpdate)
					.when()
					.put()
				.then()
					.statusCode(500) // erro esperado!
						.extract()
						.body()
							.asString();
	}
	
}
