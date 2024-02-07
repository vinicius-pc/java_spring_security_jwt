package br.com.projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.projeto.model.security.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

	// JPQL por padrão. se quiser usar sql nativo, adicionar nativeQuery = true na anotação!
	
	@Query("SELECT u FROM Permission u WHERE u.description =:description")
	Permission findByDescription(@Param("description") String description);

}
