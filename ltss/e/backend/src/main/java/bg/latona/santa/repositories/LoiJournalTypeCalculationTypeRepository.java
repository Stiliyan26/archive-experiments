package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;

import bg.latona.santa.entities.santa.finance.LoiJournalTypeCalculationType;
import bg.latona.santa.entities.santa.finance.QLoiJournalTypeCalculationType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiJournalTypeCalculationTypeRepository extends CommonRepository<LoiJournalTypeCalculationType, QLoiJournalTypeCalculationType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiJournalTypeCalculationType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
