package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QDirectionCategory;
import bg.latona.santa.entities.person.DirectionCategory;


public interface DirectionCategoryRepository extends CommonRepository<DirectionCategory, QDirectionCategory, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	DirectionCategory findFirstByCodeAndCompany(String code, ManagedCompany company);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	DirectionCategory findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}