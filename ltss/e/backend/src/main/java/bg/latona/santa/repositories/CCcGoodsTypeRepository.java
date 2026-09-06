package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.QCCcGoodsType;

public interface CCcGoodsTypeRepository extends CommonRepository<CCcGoodsType, QCCcGoodsType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CCcGoodsType findFirstByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
}
