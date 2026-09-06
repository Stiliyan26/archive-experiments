package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCtBankAccount;
import bg.latona.santa.entities.santa.common.QCCtBankAccount;
import org.springframework.data.rest.core.annotation.RestResource;

public interface CCtBankAccountRepository extends CommonRepository<CCtBankAccount, QCCtBankAccount, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCtBankAccount findFirstByIbanAndOutCodeAndCompanyAndDeleted(String iban, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCtBankAccount findFirstByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted);
}
