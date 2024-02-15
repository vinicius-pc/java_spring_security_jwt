package br.com.projeto.services.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.stereotype.Service;

import br.com.projeto.controllers.user.UserController;
import br.com.projeto.exceptions.RequiredObjectIsNullException;
import br.com.projeto.model.security.Permission;
import br.com.projeto.model.security.User;
import br.com.projeto.repositories.PermissionRepository;
import br.com.projeto.repositories.UserRepository;
import br.com.projeto.util.PasswordControl;
import br.com.projeto.vo.user.PermissionVO;
import br.com.projeto.vo.user.UserEnabledVO;
import br.com.projeto.vo.user.UserPasswordVO;
import br.com.projeto.vo.user.UserVO;

@Service
public class UsersVOServices {

	PasswordControl password = new PasswordControl();
	
	@Autowired
	UserRepository uRepository;

	@Autowired
	PermissionRepository pRepository;

	public UsersVOServices(UserRepository uRepository, PermissionRepository pRepository) {
		this.uRepository = uRepository;
		this.pRepository = pRepository;
	}


	public UserVO loadUserVOByUsername(String name) throws UsernameNotFoundException {
		if (name == null) throw new RequiredObjectIsNullException();
		var user = uRepository.findByUsername(name);
		if (user != null) {
			List<PermissionVO> permissionVOList = new ArrayList<>();
			List<Permission> permissionUser = (List<Permission>) user.getPermissions();
			for (Permission permission : permissionUser ) {
				permissionVOList.add(new PermissionVO(permission.getDescription()));
			}
			UserVO vo = new UserVO(user.getUsername(),user.getFullName(),null,permissionVOList);
			vo.add(linkTo(methodOn(UserController.class).findByUserName(name,"")).withSelfRel());  //hateoas implement!
			return vo;
		} else {
			throw new UsernameNotFoundException("Username " + name + " not found!");
		}
	}
	
	public List<UserVO> loadUsersVO() {
		List<UserVO> usersVO = new ArrayList<>();
		List<User> users = uRepository.findAll();
		if (users != null) {
			for (User user : users) {
				List<PermissionVO> permissionVOList = new ArrayList<>();
				List<Permission> permissionUser = (List<Permission>) user.getPermissions();
				for (Permission permission : permissionUser ) {
					permissionVOList.add(new PermissionVO(permission.getDescription()));
				}
				UserVO vo = new UserVO(user.getUsername(),user.getFullName(),null,permissionVOList);
				vo.add(linkTo(methodOn(UserController.class).findByUserName(user.getUsername(),"")).withSelfRel());  //hateoas implement!				
				usersVO.add(vo);
			}
		}
		return usersVO;
	}
	
	public UserVO createUser(UserVO userVO) throws UsernameNotFoundException {
		if (userVO == null) throw new RequiredObjectIsNullException();
		
		var existingUser = uRepository.findByUsername(userVO.getUserName());
		if (existingUser == null) {
			List<Permission> permissionUser = new ArrayList<>();
			List<PermissionVO> permissionVOList = userVO.getPermissions();
			for (PermissionVO permissionVO : permissionVOList) {
				Permission permission = pRepository.findByDescription(permissionVO.getDescription());
				permissionUser.add(permission);
			}
			User user = new User();
			user.setUserName(userVO.getUserName());
			user.setFullName(userVO.getFullName());
			user.setPassword(password.EncoderPassword(userVO.getPassword()));
			user.setPermissions(permissionUser);
			// default values
			user.setAccountNonExpired(true);
			user.setAccountNonLocked(true);
			user.setCredentialsNonExpired(true);
			user.setEnabled(true);
			if (user.getPassword() == null) {
				throw new UsernameNotFoundException("Password is invalid!");
			} else {
				// insert
				user = uRepository.save(user);
				userVO.add(linkTo(methodOn(UserController.class).create(userVO,"")).withSelfRel());  //hateoas implement!
				return userVO;
			}
		} else {
			throw new UsernameNotFoundException("Username " + userVO.getUserName() + " already exists!");
		}
		
	}

	public UserVO updateUser(UserVO userVO) throws UsernameNotFoundException {
		if (userVO == null) throw new RequiredObjectIsNullException();
		var existingUser = uRepository.findByUsername(userVO.getUserName());
		if (existingUser != null) {
			List<Permission> permissionUser = new ArrayList<>();
			List<PermissionVO> permissionVOList = userVO.getPermissions();
			for (PermissionVO permissionVO : permissionVOList) {
				Permission permission = pRepository.findByDescription(permissionVO.getDescription());
				permissionUser.add(permission);
			}
			User user = new User();
			user.setId(existingUser.getId());
			user.setUserName(userVO.getUserName());
			user.setFullName(userVO.getFullName());
			user.setPassword(existingUser.getPassword());
			user.setPermissions(permissionUser);
			// default values
			user.setAccountNonExpired(true);
			user.setAccountNonLocked(true);
			user.setCredentialsNonExpired(true);
			user.setEnabled(true);
			// update
			user = uRepository.save(user);
			userVO.add(linkTo(methodOn(UserController.class).Update(userVO,"")).withSelfRel());  //hateoas implement!
			return userVO;
		} else {
			throw new UsernameNotFoundException("Username " + userVO.getUserName() + " not found!");
		}
	}

	public void updatePassword(UserPasswordVO userPasswordVO) throws UsernameNotFoundException {
		if (userPasswordVO == null) throw new RequiredObjectIsNullException();
		
		var existingUser = uRepository.findByUsername(userPasswordVO.getUserName());
		if (existingUser != null) {
			User user = new User();
			user.setId(existingUser.getId());
			user.setUserName(existingUser.getUserName());
			user.setFullName(existingUser.getFullName());
			user.setPermissions(existingUser.getPermissions());
			user.setAccountNonExpired(existingUser.getAccountNonExpired());
			user.setAccountNonLocked(existingUser.getAccountNonLocked());
			user.setCredentialsNonExpired(existingUser.getCredentialsNonExpired());
			user.setEnabled(existingUser.getEnabled());
			user.setPassword(password.EncoderPassword(userPasswordVO.getNewPassword()));
			if (user.getPassword() == null) {
				throw new UsernameNotFoundException("New Password is invalid!");
			} else {
				// update
				user = uRepository.save(user);
			}
		} else {
			throw new UsernameNotFoundException("Username " + userPasswordVO.getUserName() + " not found!");
		}
	}
	
	public void disableOrEnableUser(UserEnabledVO userEnabledVO) throws UsernameNotFoundException {
		if (userEnabledVO == null) throw new RequiredObjectIsNullException();
		
		var existingUser = uRepository.findByUsername(userEnabledVO.getUserName());
		if (existingUser != null) {
			User user = new User();
			user.setId(existingUser.getId());
			user.setUserName(existingUser.getUserName());
			user.setFullName(existingUser.getFullName());
			user.setPermissions(existingUser.getPermissions());
			user.setAccountNonExpired(existingUser.getAccountNonExpired());
			user.setAccountNonLocked(existingUser.getAccountNonLocked());
			user.setCredentialsNonExpired(existingUser.getCredentialsNonExpired());
			user.setPassword(existingUser.getPassword());
			user.setEnabled(userEnabledVO.getEnabled());
			// update
			user = uRepository.save(user);
		} else {
			throw new UsernameNotFoundException("Username " + userEnabledVO.getUserName() + " not found!");
		}				
	}
}
