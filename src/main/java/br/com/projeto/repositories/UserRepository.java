package br.com.projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.projeto.model.security.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	// JPQL por padrão. se quiser usar sql nativo, adicionar nativeQuery = true na anotação!
	
	@Query("SELECT u FROM User u WHERE u.userName =:userName")
	User findByUsername(@Param("userName") String userName);

}
