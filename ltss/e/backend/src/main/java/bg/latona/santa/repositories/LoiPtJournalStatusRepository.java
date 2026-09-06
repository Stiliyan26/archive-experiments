package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtJournalStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtJournalStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtJournalStatusRepository extends CommonRepository<LoiPtJournalStatus, QLoiPtJournalStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtJournalStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
