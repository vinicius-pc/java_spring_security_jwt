package br.com.projeto.unittests.vo.mocks;

import br.com.projeto.vo.user.UserEnabledVO;

public class MockUserEnabledVO {
	
	
	public UserEnabledVO mockEntityVO() {
		return mockEntityVO(0);
	}
	
	public UserEnabledVO mockEntityVO(Integer number) {
		UserEnabledVO userEnabledVO = new UserEnabledVO();
		userEnabledVO.setUserName("USER NAME TEST "+number);
		userEnabledVO.setEnabled(true);
		return userEnabledVO;
	}
		
}
