package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCfgDocumentPattern;
import bg.latona.santa.entities.santa.common.QCCfgDocumentPattern;

public interface CCfgDocumentPatternRepository extends CommonRepository<CCfgDocumentPattern, QCCfgDocumentPattern, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCfgDocumentPattern findFirstByDocumentCodeAndOutCodeAndCompanyAndDeleted(String documentCode, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
