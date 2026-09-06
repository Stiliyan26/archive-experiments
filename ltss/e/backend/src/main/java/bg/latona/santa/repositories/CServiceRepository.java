package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CService;
import bg.latona.santa.entities.santa.common.QCService;

public interface CServiceRepository extends CommonRepository<CService, QCService, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CService findFirstByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
