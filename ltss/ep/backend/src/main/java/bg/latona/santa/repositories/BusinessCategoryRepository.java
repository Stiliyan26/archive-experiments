package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QBusinessCategory;
import bg.latona.santa.entities.person.BusinessCategory;

public interface BusinessCategoryRepository extends CommonRepository<BusinessCategory, QBusinessCategory, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	BusinessCategory findFirstByCodeAndCompany(String code, ManagedCompany company);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	BusinessCategory findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}