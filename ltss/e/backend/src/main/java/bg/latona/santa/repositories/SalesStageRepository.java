package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.QSalesStage;
import bg.latona.santa.entities.person.SalesStage;

public interface SalesStageRepository extends CommonRepository<SalesStage, QSalesStage, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	SalesStage findFirstByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted);
}