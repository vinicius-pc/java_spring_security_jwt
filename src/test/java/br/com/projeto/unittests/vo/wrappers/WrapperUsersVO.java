package br.com.projeto.unittests.vo.wrappers;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WrapperUsersVO implements Serializable{

	private static final long serialVersionUID = 1L;

	@JsonProperty("_embedded")
	private UsersEmbeddedVO embedded;

	@Override
	public int hashCode() {
		return Objects.hash(embedded);
	}

	public WrapperUsersVO() {
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WrapperUsersVO other = (WrapperUsersVO) obj;
		return Objects.equals(embedded, other.embedded);
	}

	public WrapperUsersVO(UsersEmbeddedVO embedded) {
		this.embedded = embedded;
	}

	public UsersEmbeddedVO getEmbedded() {
		return embedded;
	}

	public void setEmbedded(UsersEmbeddedVO embedded) {
		this.embedded = embedded;
	}

	
}
