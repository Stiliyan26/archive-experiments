package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QAreaCategory;
import bg.latona.santa.entities.person.AreaCategory;

public interface AreaCategoryRepository extends CommonRepository<AreaCategory, QAreaCategory, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AreaCategory findFirstByCodeAndCompany(String code, ManagedCompany company);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AreaCategory findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}