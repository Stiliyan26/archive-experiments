package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FPtJournal;
import bg.latona.santa.entities.santa.finance.LoiPtJournalStatus;
import bg.latona.santa.entities.santa.finance.QFPtJournal;

public interface FPtJournalRepository extends CommonRepository<FPtJournal, QFPtJournal, Long>{

	@Query(value = "SELECT coalesce(max(journalNo), 0) FROM FPtJournal")
	Long getMaxAutoIncNum();
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<FPtJournal> findByOutCodeAndStatusAndCompanyAndDeleted(CCcOrganizationUnit outCode, LoiPtJournalStatus status, ManagedCompany company, boolean deleted);
}
