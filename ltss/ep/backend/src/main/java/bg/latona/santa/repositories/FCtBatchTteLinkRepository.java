package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.*;
import bg.latona.santa.entities.santa.finance.FCtInvDealType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtBatchTteLinkRepository extends CommonRepository<FCtBatchTteLink, QFCtBatchTteLink, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    FCtBatchTteLink findFirstByOutCodeAndBteIdAndTteIdAndIdeIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtBatchType bteId, FCtTransitionType tteId, FCtInvDealType ideId, ManagedCompany company, boolean deleted);
}
