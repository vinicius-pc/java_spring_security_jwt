package br.com.projeto.vo.user;

import java.io.Serializable;
import java.util.Objects;

public class UserPasswordVO implements Serializable {

	private static final long serialVersionUID = 1L;


	private String userName;
	
	private String newPassword;
	private String oldPassword;
	

	public UserPasswordVO() {}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getNewPassword() {
		return newPassword;
	}


	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}


	public String getOldPassword() {
		return oldPassword;
	}


	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}


	@Override
	public int hashCode() {
		return Objects.hash(newPassword, oldPassword, userName);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPasswordVO other = (UserPasswordVO) obj;
		return Objects.equals(newPassword, other.newPassword) && Objects.equals(oldPassword, other.oldPassword)
				&& Objects.equals(userName, other.userName);
	}


	public UserPasswordVO(String userName, String newPassword, String oldPassword) {
		this.userName = userName;
		this.newPassword = newPassword;
		this.oldPassword = oldPassword;
	}



}