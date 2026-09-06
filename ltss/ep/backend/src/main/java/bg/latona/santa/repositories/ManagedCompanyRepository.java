package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QManagedCompany;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface ManagedCompanyRepository extends CommonRepository<ManagedCompany, QManagedCompany, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ManagedCompany> findByCode(@Param("code") Long code);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	ManagedCompany findFirstByCodeAndDeleted(Long code, boolean deleted);
}