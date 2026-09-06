package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.QCCcOrganizationUnit;

public interface CCcOrganizationUnitRepository extends CommonRepository<CCcOrganizationUnit, QCCcOrganizationUnit, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCcOrganizationUnit findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CCcOrganizationUnit> findByOutIdAndCompanyAndDeleted(CCcOrganizationUnit outId, ManagedCompany company, boolean deleted);
}
