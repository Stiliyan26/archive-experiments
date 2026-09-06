package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCtStornoType;
import bg.latona.santa.entities.santa.finance.LoiCtStornoTypeType;
import bg.latona.santa.entities.santa.finance.QFCtStornoType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtStornoTypeRepository extends CommonRepository<FCtStornoType, QFCtStornoType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtStornoType findFirstByOutCodeAndTypeAndCompanyAndDeleted(CCcOrganizationUnit outCode, LoiCtStornoTypeType type, ManagedCompany company, boolean deleted);
}
