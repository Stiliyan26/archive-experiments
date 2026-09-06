package bg.latona.santa.repositories;

import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.QBankAccount;

import org.springframework.data.rest.core.annotation.RestResource;

public interface BankAccountRepository extends CommonRepository<BankAccount, QBankAccount, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	BankAccount findFirstByIbanAndCompanyAndDeleted(String iban, ManagedCompany company, boolean deleted);
}