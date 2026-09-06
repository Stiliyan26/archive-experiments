package bg.latona.santa.entities.santa.finance;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CCtPartnerGroup;
import bg.latona.santa.entities.santa.common.CGoods;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FDiscount extends CompanyRecord{
	private BigDecimal discount;
	@ManyToOne
	private CGoods god; //not sure about this one
	@ManyToOne
	private CCcPartner parId; // partner
	@ManyToOne
	private CCtPartnerGroup pgpId; //partnerGroup
	@ManyToOne
	private CCcGoodsType gteId; //goodsType
	@ManyToOne
	private CCcOrganizationUnit outCode;
}
