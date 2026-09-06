package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;

import bg.latona.santa.entities.santa.finance.LoiTypeOfFinancialAccount;
import bg.latona.santa.entities.santa.finance.QLoiTypeOfFinancialAccount;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiTypeOfFinancialAccountRepository extends CommonRepository<LoiTypeOfFinancialAccount, QLoiTypeOfFinancialAccount, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiTypeOfFinancialAccount findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
