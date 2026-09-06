package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FInvDdsFile;
import bg.latona.santa.entities.santa.finance.QFInvDdsFile;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FInvDdsFileRepository extends CommonRepository<FInvDdsFile, QFInvDdsFile, Long>  {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    FInvDdsFile findFirstByOutCodeAndPeriodAndCompanyAndDeleted(CCcOrganizationUnit outCode, String period, ManagedCompany company, boolean deleted);
}
