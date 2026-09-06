package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.QSecRole;
import bg.latona.santa.entities.security.SecRole;

public interface SecRoleRepository extends CommonRepository<SecRole, QSecRole, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SecRole findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<SecRole> findByCompanyAndDeleted(ManagedCompany company, boolean deleted);
}