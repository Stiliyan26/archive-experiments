package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QGeneralCategory;
import bg.latona.santa.entities.person.GeneralCategory;

public interface GeneralCategoryRepository extends CommonRepository<GeneralCategory, QGeneralCategory, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	GeneralCategory findFirstByCodeAndCompany(String code, ManagedCompany company);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	GeneralCategory findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}