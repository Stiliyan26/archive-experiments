package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.FJournalType;
import bg.latona.santa.entities.santa.finance.QFJournalType;

public interface FJournalTypeRepository extends CommonRepository<FJournalType, QFJournalType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FJournalType findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);

}
