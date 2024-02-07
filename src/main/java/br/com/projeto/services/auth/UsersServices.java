package br.com.projeto.services.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.projeto.repositories.UserRepository;

@Service
public class UsersServices implements UserDetailsService {

	@Autowired
	UserRepository repository;
	
	public UsersServices(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
		var user = repository.findByUsername(name);
		if (user != null) {
			return user;
		} else {
			throw new UsernameNotFoundException("Username " + name + " not found!");
		}
	}

}
