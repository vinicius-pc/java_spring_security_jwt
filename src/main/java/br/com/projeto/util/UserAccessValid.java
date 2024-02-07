package br.com.projeto.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.projeto.model.security.Permission;
import br.com.projeto.model.security.User;
import br.com.projeto.repositories.UserRepository;
import br.com.projeto.securityJWT.JwtTokenProvider;

@Service
public class UserAccessValid {
	
	// classe responsavel pela validação de acesso as API´s por usuario!
	// sobre o usuario, podemos obter de 2 formas : Na sessão Spring security OU pegar pela chave authorization do JWT!
	// para buscar pela sessão, utilizamos SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	// para buscar pela chave JWT, utilizamos o tokenProvider.decodedJWT(tokenJWT) para decodificar!
	// temos as duas formas implementadas, mas vou comentar a sessão e utilizar a chave JWT.
	
	@Autowired
	UserRepository repository;
	
	@Autowired
	JwtTokenProvider tokenProvider;
	
	public boolean userAccess(String tokenJWT, String acessAPI, String method, String userNameAccess) {
		return verifyUser(tokenJWT, acessAPI, method, userNameAccess);
	}
	public boolean userAccess(String tokenJWT, String acessAPI, String method) {
		return verifyUser(tokenJWT, acessAPI, method, "");
	}
	
	private boolean verifyUser(String tokenJWT, String acessAPI, String method, String userNameAccess) {
		// verifica se o usuario do tokenJWT é valido!
		DecodedJWT decodedJWT = tokenProvider.decodedJWT(tokenJWT);
		String userName = decodedJWT.getSubject();
		if (userName == null) return false;
		User user = repository.findByUsername(userName);
		if ((user == null) || (user.getEnabled() == false)) return false;
		return verifyAccess(user, acessAPI, method, userNameAccess);		
	}
	
	private boolean verifyAccess(User user, String acessAPI, String method, String userNameAccess) {
		// verifica se o usuario pode acessar a API
		// method pode ser : GET, PUT, POST, DELETE
		// irei mockar de inicio!!!
		if (acessAPI == "user") {
			// API usuario! - a principio, apenas ADMIN pode acessar usuarios
			// a exceção é o proprio usuario acessar dados dele mesmo! (GET)
			if ((method.equals("GET")) && (user.getUsername().equals(userNameAccess))) return true;
			
			List<Permission> permitions = user.getPermissions();
			for (Permission permition : permitions) {
				if (permition.getDescription().equals("ADMIN")) return true;
			}
			return false;
		}
		if (acessAPI == "username") {
			// API usuario! - a principio, apenas ADMIN pode acessar usuarios
			// a exceção é o proprio usuario acessar dados dele mesmo!
			if (user.getUsername().equals(userNameAccess)) return true;
			
			List<Permission> permitions = user.getPermissions();
			for (Permission permition : permitions) {
				if (permition.getDescription().equals("ADMIN")) return true;
			}
			return false;
		}
		if (acessAPI == "userpassword") {
			// API alterar senha - apenas o proprio usuario pode alterar a sua senha!
			if (user.getUsername().equals(userNameAccess)) return true;
			return false;
		}
		if (acessAPI == "admupdatepassword") {
			// API alterar senha de um usuario qualquer - apenas ADMIN!
			List<Permission> permitions = user.getPermissions();
			for (Permission permition : permitions) {
				if (permition.getDescription().equals("ADMIN")) return true;
			}
			return false;
		}
		
		
		// se chegou até aqui, então pode acessar!
		return true;
	}
	
	//private String userNameLogIn() {
	//	// obter o usuario na sessão!
	//	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	//	if (principal instanceof UserDetails) {
	//		return ((UserDetails)principal).getUsername();
	//	} 
	//	return principal.toString();
	//}

}
