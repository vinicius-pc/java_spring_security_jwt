package br.com.projeto.unittests.vo.wrappers;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.projeto.vo.user.UserVO;

public class UsersEmbeddedVO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("userVOList")
	private List<UserVO> users;

	public UsersEmbeddedVO() {
	}

	@Override
	public int hashCode() {
		return Objects.hash(users);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UsersEmbeddedVO other = (UsersEmbeddedVO) obj;
		return Objects.equals(users, other.users);
	}

	public List<UserVO> getUsers() {
		return users;
	}

	public void setUsers(List<UserVO> users) {
		this.users = users;
	}

	public UsersEmbeddedVO(List<UserVO> users) {
		this.users = users;
	}

}
