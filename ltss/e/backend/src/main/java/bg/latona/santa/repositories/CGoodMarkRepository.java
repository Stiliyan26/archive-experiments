package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CGoodMark;
import bg.latona.santa.entities.santa.common.QCGoodMark;

public interface CGoodMarkRepository extends CommonRepository<CGoodMark, QCGoodMark, Long>{
          
    @RestResource(exported = false) //don't expose methods that are not checking permissions
	CGoodMark findFirstByMarkCodeAndCompany(String markCode, ManagedCompany company);
    @RestResource(exported = false) //don't expose methods that are not checking permissions
	CGoodMark findFirstByMarkCodeAndOutCodeAndCompanyAndDeleted(String markCode, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
