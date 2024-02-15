package br.com.projeto.unittests.mockito.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.projeto.exceptions.RequiredObjectIsNullException;
import br.com.projeto.model.security.Permission;
import br.com.projeto.model.security.User;
import br.com.projeto.repositories.PermissionRepository;
import br.com.projeto.repositories.UserRepository;
import br.com.projeto.services.user.UsersVOServices;
import br.com.projeto.unittests.vo.mocks.MockPermissionVO;
import br.com.projeto.unittests.vo.mocks.MockUserEnabledVO;
import br.com.projeto.unittests.vo.mocks.MockUserPasswordVO;
import br.com.projeto.unittests.vo.mocks.MockUserVO;
import br.com.projeto.vo.user.UserEnabledVO;
import br.com.projeto.vo.user.UserPasswordVO;
import br.com.projeto.vo.user.UserVO;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class UsersVOServicesTest {
	
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

	@BeforeEach
	void setUpMocks() throws Exception {
		mockUserPassword = new MockUserPasswordVO();
		mockPermission = new MockPermissionVO();
		mockUserEnabledVO = new MockUserEnabledVO();
		input = new MockUserVO();
		MockitoAnnotations.openMocks(this);
	}

	@Test
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
	void testLoadUsersVO() {
		List<User> users = input.mockEntityList();
		users.forEach(user -> {
			user.setPermissions(mockPermission.mockPermissions(1));	// utilizando um laço foreach lambda para incluir as permissoes.		
		});
		when(repository.findAll()).thenReturn(users);
		var results = service.loadUsersVO();
		///System.out.println(result.toString());  // para ver o valor do link hateoas
		var resultTEN = results.get(10);
		User user  = input.mockEntity(10);
		assertTrue(resultTEN.toString().contains("links: [</api/user/USER%20NAME%20TEST%2010>;rel=\"self\"]"));
		assertEquals(user.getFullName(), resultTEN.getFullName());
		assertEquals(user.getUserName(), resultTEN.getUserName());
		assertEquals(null, resultTEN.getPassword());		
	}

	@Test
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
	void testUpdatePassword() {
		UserPasswordVO userPasswordVO = mockUserPassword.mockEntityVO(1);
		User user = input.mockEntity(1);
		user.setPermissions(mockPermission.mockPermissions(1));
		when(repository.findByUsername(userPasswordVO.getUserName())).thenReturn(user);
		lenient().when(repository.save(user)).thenReturn(user);
		service.updatePassword(userPasswordVO);
	}

	@Test
	void testDisableOrEnableUser() {
		UserEnabledVO userEnabledVO = mockUserEnabledVO.mockEntityVO(1);
		User user = input.mockEntity(1);
		user.setPermissions(mockPermission.mockPermissions(1));
		when(repository.findByUsername(userEnabledVO.getUserName())).thenReturn(user);
		lenient().when(repository.save(user)).thenReturn(user);
		service.disableOrEnableUser(userEnabledVO);
	}

	@Test
	void testCreateNullUser() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {
			service.createUser(null);
		});
		String expectedMessage = "It is not allowed to persist a null object!";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

	@Test
	void testUpdateNullUser() {
		Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> {
			service.updateUser(null);
		});
		String expectedMessage = "It is not allowed to persist a null object!";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
	
	@Test
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
