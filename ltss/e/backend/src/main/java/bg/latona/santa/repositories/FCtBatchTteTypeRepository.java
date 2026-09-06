package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCtTransitionType;
import bg.latona.santa.entities.santa.finance.FCtBatchTteType;
import bg.latona.santa.entities.santa.finance.QFCtBatchTteType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtBatchTteTypeRepository extends CommonRepository<FCtBatchTteType, QFCtBatchTteType, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    FCtBatchTteType findFirstByOutCodeAndTteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, ManagedCompany company, boolean deleted);
}
