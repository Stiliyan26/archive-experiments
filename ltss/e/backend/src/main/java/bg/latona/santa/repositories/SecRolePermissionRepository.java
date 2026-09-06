package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.QSecRolePermission;
import bg.latona.santa.entities.security.SecPermission;
import bg.latona.santa.entities.security.SecRole;
import bg.latona.santa.entities.security.SecRolePermission;

public interface SecRolePermissionRepository extends CommonRepository<SecRolePermission, QSecRolePermission, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecRolePermission findFirstByRoleAndPermissionAndDeleted(SecRole role, SecPermission permission, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecRolePermission findFirstByRoleAndCompanyAndDeleted(SecRole role, ManagedCompany company, boolean deleted);
}