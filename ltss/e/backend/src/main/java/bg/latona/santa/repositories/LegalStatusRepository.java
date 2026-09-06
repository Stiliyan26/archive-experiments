package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QLegalStatus;
import bg.latona.santa.entities.person.LegalStatus;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LegalStatusRepository extends CommonRepository<LegalStatus, QLegalStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<LegalStatus> findByCode(@Param("code") Long code);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LegalStatus findFirstByCodeAndCompany(Long code, ManagedCompany company);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LegalStatus findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}