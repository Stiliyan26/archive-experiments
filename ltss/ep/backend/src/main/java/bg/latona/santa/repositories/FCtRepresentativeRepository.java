package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCtRepresentative;
import bg.latona.santa.entities.santa.finance.QFCtRepresentative;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtRepresentativeRepository extends CommonRepository<FCtRepresentative, QFCtRepresentative, Long> {

    @RestResource(exported = false) //don't expose methods that are not checking permissions
    FCtRepresentative findFirstByOutCodeAndIdeNoAndCompanyAndDeleted(CCcOrganizationUnit outCode, String ideNo, ManagedCompany company, boolean deleted);
}
