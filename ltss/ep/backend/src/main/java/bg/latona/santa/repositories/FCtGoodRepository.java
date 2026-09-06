package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCtGood;
import bg.latona.santa.entities.santa.finance.QFCtGood;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtGoodRepository extends CommonRepository<FCtGood, QFCtGood, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtGood findFirstByCodeAndOutCodeAndGteIdAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, CCcGoodsType gteId, ManagedCompany company, boolean deleted);
}
