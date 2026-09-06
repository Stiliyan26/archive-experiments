package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.QCurrency;
import bg.latona.santa.entities.article.Currency;

public interface CurrencyRepository extends CommonRepository<Currency, QCurrency, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Currency findFirstByNameAndCompany(String name, ManagedCompany company);
}