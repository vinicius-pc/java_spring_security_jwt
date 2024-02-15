package br.com.projeto.unittests.vo.mocks;

import br.com.projeto.vo.user.UserPasswordVO;

public class MockUserPasswordVO {
	
	
	public UserPasswordVO mockEntityVO() {
		return mockEntityVO(0);
	}
	
	public UserPasswordVO mockEntityVO(Integer number) {
		UserPasswordVO userPasswordVO = new UserPasswordVO();
		userPasswordVO.setUserName("USER NAME TEST "+number);
		userPasswordVO.setOldPassword("OLD PASSWORD TEST "+number);
		userPasswordVO.setNewPassword("NEW PASSWORD TEST "+number);
		return userPasswordVO;
	}
		
}
