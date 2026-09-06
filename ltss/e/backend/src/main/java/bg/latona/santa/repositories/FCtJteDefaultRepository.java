package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCtJteDefault;
import bg.latona.santa.entities.santa.finance.FJournalType;
import bg.latona.santa.entities.santa.finance.QFCtJteDefault;

public interface FCtJteDefaultRepository extends CommonRepository<FCtJteDefault, QFCtJteDefault, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtJteDefault findFirstByJteAndOutCodeAndCompanyAndDeleted(FJournalType jte, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
