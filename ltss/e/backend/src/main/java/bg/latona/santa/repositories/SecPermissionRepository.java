package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.QSecPermission;
import bg.latona.santa.entities.security.SecPermission;

public interface SecPermissionRepository extends CommonRepository<SecPermission, QSecPermission, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecPermission findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<SecPermission> findByCompanyAndDeleted(ManagedCompany company, boolean deleted);
}