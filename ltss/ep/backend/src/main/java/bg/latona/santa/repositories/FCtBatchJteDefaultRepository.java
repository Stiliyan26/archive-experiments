package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCtBatchJteDefault;
import bg.latona.santa.entities.santa.finance.FCtBatchType;
import bg.latona.santa.entities.santa.finance.FJournalType;
import bg.latona.santa.entities.santa.finance.QFCtBatchJteDefault;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtBatchJteDefaultRepository extends CommonRepository<FCtBatchJteDefault, QFCtBatchJteDefault, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    FCtBatchJteDefault findFirstByOutCodeAndBteIdAndJteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtBatchType bteId, FJournalType jteId, ManagedCompany company, boolean deleted);
}
