package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.security.QSecUserRole;
import bg.latona.santa.entities.security.SecRole;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.security.SecUserRole;

public interface SecUserRoleRepository extends CommonRepository<SecUserRole, QSecUserRole, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecUserRole findFirstByUserAndRoleAndDeleted(SecUser user, SecRole role, boolean deleted);
}