package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.Income;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QIncome;

public interface IncomeRepository extends CommonRepository<Income, QIncome, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Income findFirstByNameAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}