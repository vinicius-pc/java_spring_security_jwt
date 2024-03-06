package br.com.projeto.integrationtests.auth;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import br.com.projeto.configs.TestConfigs;
import br.com.projeto.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.projeto.vo.security.AccountCredentialsVO;
import br.com.projeto.vo.security.TokenVO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8888"})
@TestMethodOrder(OrderAnnotation.class)
class AuthControllerTest  extends AbstractIntegrationTest {

	// para testarmos o login, não podemos mockar o usuario, temos que fazer via container por causa do spring security
	// pode ser feito diretamente ao banco de dados, retirando o " extends AbstractIntegrationTest ", mas isso ira expor uma conta usuario e senha!
	private static TokenVO tokenVO;
	
	@Test
	@Order(1)
	void testSignin() throws JsonMappingException, JsonProcessingException {
		AccountCredentialsVO user = new AccountCredentialsVO("leandro", "admin123");
		tokenVO = given()
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
								.as(TokenVO.class);
		
		assertNotNull(tokenVO.getAccessToken());
		assertNotNull(tokenVO.getRefreshToken());
	}

	@Test
	@Order(2)
	void testRefreshToken() throws JsonMappingException, JsonProcessingException {
		var newTokenVO = given()
				.basePath("/auth/refresh")
				.port(TestConfigs.SERVER_PORT)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.pathParam("username", tokenVO.getUsername())
					.header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenVO.getRefreshToken())
				.when()
					.put("{username}")
				.then()
					.statusCode(200)
				.extract()
					.body()
						.as(TokenVO.class);
		
		assertNotNull(newTokenVO.getAccessToken());
		assertNotNull(newTokenVO.getRefreshToken());
	}

}
