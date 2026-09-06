package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.QCCcPartner;

public interface CCcPartnerRepository extends CommonRepository<CCcPartner, QCCcPartner, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCcPartner findFirstByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
