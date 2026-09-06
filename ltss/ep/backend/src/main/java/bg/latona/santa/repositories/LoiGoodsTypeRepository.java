package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiGoodsType;
import bg.latona.santa.entities.santa.common.QLoiGoodsType;

public interface LoiGoodsTypeRepository extends CommonRepository<LoiGoodsType, QLoiGoodsType, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiGoodsType findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
