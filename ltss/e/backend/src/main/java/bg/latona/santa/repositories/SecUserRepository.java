package bg.latona.santa.repositories;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.QSecUser;
import bg.latona.santa.entities.security.SecUser;

public interface SecUserRepository extends CommonRepository<SecUser, QSecUser, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecUser findFirstByName(@Param("name") String name);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecUser findFirstByNameAndDeleted(String name, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecUser findFirstByFullNameAndCompany(String fullName, ManagedCompany company);
}