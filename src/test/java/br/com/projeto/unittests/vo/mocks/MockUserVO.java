package br.com.projeto.unittests.vo.mocks;

import java.util.ArrayList;
import java.util.List;

import br.com.projeto.model.security.User;
import br.com.projeto.vo.user.UserVO;

public class MockUserVO {
	
	public UserVO mockEntityVO() {
		return mockEntityVO(0);
	}

	public UserVO mockEntityVO(Integer number) {
		UserVO user = new UserVO();
		user.setFullName("FULL NAME TEST "+number);
		user.setUserName("USER NAME TEST "+number);
		user.setPassword("PASSWORD TEST "+number);
		return user;
	}
	
	public User mockEntity() {
		return mockEntity(0);
	}

	public User mockEntity(Integer number) {
		User user = new User();
		user.setId(number.longValue());
		user.setFullName("FULL NAME TEST "+number);
		user.setUserName("USER NAME TEST "+number);
		user.setPassword("PASSWORD TEST "+number);
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);		
		return user;
	}

	public List<User> mockEntityList() {
		List<User> users = new ArrayList<User>();
		for (int i = 0; i < 14; i++) {
			users.add(mockEntity(i));
		}
		return users;
	}
	
	public List<UserVO> mockEntityListVO() {
		List<UserVO> users = new ArrayList<UserVO>();
		for (int i = 0; i < 14; i++) {
			users.add(mockEntityVO(i));
		}
		return users;
	}
	
}
