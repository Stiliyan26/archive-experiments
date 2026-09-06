package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtJournalCcStatus;
import bg.latona.santa.entities.santa.finance.QLoiPtJournalCcStatus;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtJournalCcStatusRepository extends CommonRepository<LoiPtJournalCcStatus, QLoiPtJournalCcStatus, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtJournalCcStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
