package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCtPartnerGroup;
import bg.latona.santa.entities.santa.common.QCCtPartnerGroup;

public interface CCtPartnerGroupRepository extends CommonRepository<CCtPartnerGroup, QCCtPartnerGroup, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCtPartnerGroup findFirstByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCtPartnerGroup findFirstByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted);
}
