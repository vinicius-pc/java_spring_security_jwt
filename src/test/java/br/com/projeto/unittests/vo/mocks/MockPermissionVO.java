package br.com.projeto.unittests.vo.mocks;

import java.util.ArrayList;
import java.util.List;

import br.com.projeto.model.security.Permission;
import br.com.projeto.vo.user.PermissionVO;

public class MockPermissionVO {
	
	public PermissionVO mockEntityVO() {
		return mockEntityVO(0);
	}

	public List<PermissionVO> mockEntityListVO() {
		List<PermissionVO> lista = new ArrayList<PermissionVO>();
		lista.add(mockEntityVO(1));
		lista.add(mockEntityVO(2));
		return lista;
	}
	
	public PermissionVO mockEntityVO(Integer number) {
		PermissionVO permission = new PermissionVO();
		permission.setDescription("ADMIN");	
		if (number==2) {
			permission.setDescription("MANAGER");	
		}
		return permission;
	}
	
	public List<PermissionVO> mockPermissionsVO(Integer number){
		List<PermissionVO> permissions = new ArrayList<>();
		permissions.add(mockEntityVO(number));
		return permissions;
	}
	
	public Permission mockEntity() {
		return mockEntity(0);
	}

	
	public Permission mockEntity(Integer number) {
		Permission permission = new Permission();
		permission.setId(number.longValue());
		permission.setDescription("ADMIN");		
		if (number==2) {
			permission.setDescription("MANAGER");					
		}
		return permission;
	}
	
	public List<Permission> mockPermissions(Integer number){
		List<Permission> permissions = new ArrayList<>();
		permissions.add(mockEntity(number));
		return permissions;
	}
	
}
