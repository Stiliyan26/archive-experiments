package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CCtPartnerGroup;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.finance.FDiscount;
import bg.latona.santa.entities.santa.finance.QFDiscount;

public interface FDiscountRepository extends CommonRepository<FDiscount, QFDiscount, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FDiscount findFirstByGodAndCompanyAndDeleted(CGoods god, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FDiscount findFirstByGteIdAndCompanyAndDeleted(CCcGoodsType gteId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FDiscount findFirstByParIdAndCompanyAndDeleted(CCcPartner parId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FDiscount findFirstByPgpIdAndCompanyAndDeleted(CCtPartnerGroup pgpId, ManagedCompany company, boolean deleted);
}
