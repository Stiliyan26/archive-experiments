package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import bg.latona.santa.entities.santa.common.QCCtCurrency;

public interface CCtCurrencyRepository extends CommonRepository<CCtCurrency, QCCtCurrency, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCtCurrency findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCtCurrency findFirstByCode(String code);
}

